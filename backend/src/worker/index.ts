import { env } from "cloudflare:workers";
import { Hono } from "hono";
import { bearerAuth } from "hono/bearer-auth";
import { createRemoteJWKSet, jwtVerify } from "jose";
import crypto from "node:crypto";
import OpenAI from "openai";
import { z } from "zod";
import { safeEqual } from "./crypto";
import { checkChatRateLimit } from "./ratelimit";
import { signMessage, verifyMessage } from "./signing";

declare module "hono" {
  interface ContextVariableMap {
    /** The user's session token. */
    token: string;
  }
}

const envSchema = z.object({
  EXPECTED_HD_DOMAIN: z.string().min(1),
  GOOGLE_ANDROID_CLIENT_ID: z.string().min(1),
  GOOGLE_WEB_CLIENT_ID: z.string().min(1),
  AI_GATEWAY_ID: z.string().min(1),
  AI_GATEWAY_ACCOUNT_ID: z.string().min(1),
  AI_GATEWAY_TOKEN: z.string().min(1),
  AI_GATEWAY_MODEL: z.string().min(1),
  MESSAGE_SIGNING_SECRET: z.string().min(1),
  SESSION_TTL: z.string().optional(),
});

const cfg = envSchema.parse(process.env);

let SESSION_TTL = parseInt(cfg.SESSION_TTL ?? "2592000");
if (isNaN(SESSION_TTL)) {
  SESSION_TTL = 2592000; // 30 days in seconds
}

const SYSTEM_PROMPT =
  "You are an academic assistant. You want to help students with any questions they have. " +
  "Keep discussion focused around school. Avoid inappropriate discussions.";

const MAX_MESSAGES = 100;
const MAX_MESSAGE_CHARS = 8000;
const MAX_TOTAL_CHARS = 30000;

const chatRequestSchema = z
  .object({
    messages: z
      .array(
        z.object({
          role: z.enum(["user", "assistant"]),
          content: z.string().min(1).max(MAX_MESSAGE_CHARS),
          signature: z.string().optional(),
        })
      )
      .min(1)
      .max(MAX_MESSAGES),
  })
  .superRefine((val, ctx) => {
    const total = val.messages.reduce((n, m) => n + m.content.length, 0);
    if (total > MAX_TOTAL_CHARS) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Request too long",
      });
    }
  });

const JWKS = createRemoteJWKSet(
  new URL("https://www.googleapis.com/oauth2/v3/certs")
);

const app = new Hono<{ Bindings: Env }>();

app.get("/api/config", async (c) => {
  c.header("Cache-Control", "max-age=86400"); // Allow this response to be cached for up to a day
  return c.json({ clientId: cfg.GOOGLE_WEB_CLIENT_ID }, 200);
});

app.post("/api/sign-in", async (c) => {
  const body = await c.req.json();
  const googleToken = body["googleIdToken"] as string;

  if (typeof googleToken !== "string" || googleToken.length === 0) {
    return c.json({ error: "Bad request" }, 400);
  }

  let payload;
  try {
    const result = await jwtVerify(googleToken, JWKS, {
      issuer: "https://accounts.google.com",
    });
    payload = result.payload;
  } catch {
    // Invalid or expired Google ID token — not a server error.
    return c.json({ error: "Invalid or expired token" }, 401);
  }

  const azp = payload.azp as string | undefined;
  const aud = payload.aud as string | undefined;

  if (
    typeof azp !== "string" ||
    typeof aud !== "string" ||
    !safeEqual(azp, cfg.GOOGLE_ANDROID_CLIENT_ID) ||
    !safeEqual(aud, cfg.GOOGLE_WEB_CLIENT_ID)
  ) {
    return c.json({ error: "Bad request" }, 400);
  }

  if (payload?.hd !== cfg.EXPECTED_HD_DOMAIN) {
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
    verifyToken: async (token, c) => {
      if (token.length !== 86) {
        // 64 bytes in base64 is 86 characters
        return false; // This token can't be valid; it's the wrong length
      }
      const value = await env.KV.get(`session-${token}`);
      if (value === null) return false;
      // Stash the verified token on the context so the handler can use it without re-parsing the
      // Authorization header.
      c.set("token", token);
      return true;
    },
  }),
  async (c) => {
    const token = c.get("token");

    // Rate limiting
    const rl = await checkChatRateLimit(token);
    if (!rl.allowed) {
      return c.json(
        { error: rl.error },
        429,
        { "Retry-After": String(rl.retryAfter ?? 60) }
      );
    }

    // Validate the input
    const body = await c.req.json();
    const parsed = chatRequestSchema.safeParse(body);
    if (!parsed.success) {
      return c.json({ error: "Bad request" }, 400);
    }
    const messages = parsed.data.messages;

    // Reject any assistant message whose signature we did not produce
    // to prevent the user from modifying the assistant's past messages
    for (const m of messages) {
      if (
        m.role === "assistant" &&
        !verifyMessage(m.role, m.content, m.signature, cfg.MESSAGE_SIGNING_SECRET)
      ) {
        return c.json({ error: "Invalid message" }, 400);
      }
    }

    const openai = new OpenAI({
      apiKey: cfg.AI_GATEWAY_TOKEN,
      baseURL: `https://gateway.ai.cloudflare.com/v1/${cfg.AI_GATEWAY_ACCOUNT_ID}/${cfg.AI_GATEWAY_ID}/compat`,
    });

    const response = await openai.chat.completions.create({
      model: cfg.AI_GATEWAY_MODEL,
      messages: [
        { role: "system", content: SYSTEM_PROMPT },
        ...messages.map((m) => ({ role: m.role, content: m.content })),
      ],
    });

    const message = response.choices[0].message.content ?? "";
    const signature = signMessage("assistant", message, cfg.MESSAGE_SIGNING_SECRET);

    return c.json({ message, signature });
  }
);

export default app;
