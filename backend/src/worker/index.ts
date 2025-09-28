import { env } from "cloudflare:workers";
import { Hono } from "hono";
import { bearerAuth } from "hono/bearer-auth";
import { createRemoteJWKSet, jwtVerify } from "jose";
import crypto from "node:crypto";
import OpenAI from "openai";

const {
  EXPECTED_HD_DOMAIN,
  GOOGLE_ANDROID_CLIENT_ID,
  GOOGLE_WEB_CLIENT_ID,
  AI_GATEWAY_ID,
  AI_GATEWAY_ACCOUNT_ID,
  AI_GATEWAY_TOKEN,
  AI_GATEWAY_MODEL,
  SESSION_TTL: sessionTtl,
} = process.env;

let SESSION_TTL = parseInt(sessionTtl ?? "2592000");
if (isNaN(SESSION_TTL)) {
  SESSION_TTL = 2592000; // 30 days in seconds
}

const JWKS = createRemoteJWKSet(
  new URL("https://www.googleapis.com/oauth2/v3/certs")
);

const app = new Hono<{ Bindings: Env }>();

app.get("/api/config", async (c) => {
  checkEnvVars();
  c.header("Cache-Control", "max-age=86400"); // Allow this response to be cached for up to a day
  return c.json({ clientId: GOOGLE_WEB_CLIENT_ID }, 200);
});

app.post("/api/sign-in", async (c) => {
  checkEnvVars();
  const body = await c.req.json();
  const googleToken = body["googleIdToken"] as string;

  const { payload } = await jwtVerify(googleToken, JWKS, {
    issuer: "https://accounts.google.com",
  });

  if (
    !crypto.timingSafeEqual(
      Buffer.from(payload.azp as string),
      Buffer.from(GOOGLE_ANDROID_CLIENT_ID!)
    ) ||
    !crypto.timingSafeEqual(
      Buffer.from(payload.aud as string),
      Buffer.from(GOOGLE_WEB_CLIENT_ID!)
    )
  ) {
    return c.json({ error: "Bad request" }, 400);
  }

  if (payload?.hd !== EXPECTED_HD_DOMAIN) {
    return c.json({ error: "Invalid domain" }, 403);
  } else if (payload?.email_verified !== true) {
    return c.json({ error: "Email not verified" }, 403);
  }

  const sessionID = crypto.randomBytes(64).toString("base64url");

  env.KV.put(`session-${sessionID}`, "", {
    expirationTtl: SESSION_TTL,
    metadata: payload,
  });

  return c.json({ sessionID }, 200);
});

app.post(
  "/api/chat",
  bearerAuth({
    verifyToken: async (token) => {
      if (token.length !== 86) {
        // 64 bytes in base64 is 86 characters
        return false; // This token can't be valid; it's the wrong length
      }
      const value = await env.KV.get(`session-${token}`);
      return value !== null;
    },
  }),
  async (c) => {
    checkEnvVars();
    const body = await c.req.json();
    const messages = body["messages"];

    if (
      !Array.isArray(messages) ||
      messages.length === 0 ||
      messages.length > 100
    ) {
      return c.json({ error: "Bad request" }, 400);
    }

    const openai = new OpenAI({
      apiKey: AI_GATEWAY_TOKEN,
      baseURL: `https://gateway.ai.cloudflare.com/v1/${AI_GATEWAY_ACCOUNT_ID}/${AI_GATEWAY_ID}/compat`,
    });

    const response = await openai.chat.completions.create({
      model: AI_GATEWAY_MODEL!,
      messages,
    });

    const message = response.choices[0].message.content;

    return c.json({ message });
  }
);

function checkEnvVars() {
  if (
    typeof GOOGLE_WEB_CLIENT_ID !== "string" ||
    GOOGLE_WEB_CLIENT_ID.length === 0
  ) {
    throw new Error("Google web client ID not found");
  }

  if (
    typeof GOOGLE_ANDROID_CLIENT_ID !== "string" ||
    GOOGLE_ANDROID_CLIENT_ID.length === 0
  ) {
    throw new Error("Google web client ID not found");
  }

  if (
    typeof EXPECTED_HD_DOMAIN !== "string" ||
    EXPECTED_HD_DOMAIN.trim().length === 0
  ) {
    throw new Error("Expected `hd` domain not found");
  }

  if (
    !AI_GATEWAY_ACCOUNT_ID ||
    !AI_GATEWAY_ID ||
    !AI_GATEWAY_MODEL ||
    !AI_GATEWAY_TOKEN
  ) {
    throw new Error("AI Gateway configuration incomplete");
  }
}

export default app;
