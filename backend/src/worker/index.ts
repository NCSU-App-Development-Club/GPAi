import { Hono } from "hono";
import { createRemoteJWKSet, jwtVerify } from "jose";
import crypto from "node:crypto";
import OpenAI from "openai";
import { z } from "zod";
import { requireAuth } from "./auth";
import { safeEqual } from "./crypto";
import { checkChatRateLimit } from "./ratelimit";
import { createSession, deleteSession, googlePayloadSchema, sessionDataSchema } from "./session";
import { signMessage, verifyMessage } from "./signing";

declare module "hono" {
  interface ContextVariableMap {
    /** The user's session token. */
    token: string;
    /** The user's unique ID from their Google account. */
    userId: string;
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
  SESSION_TTL: z.coerce.number().optional().default(2592000), // 30 days in seconds
});

const cfg = envSchema.parse(process.env);

const SYSTEM_PROMPT =
  "You are an academic assistant. You want to help students with any questions they have. " +
  "Keep discussion focused around school. Avoid inappropriate discussions.";

const MAX_MESSAGES = 100;
const MAX_LLM_MESSAGES = 20;
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
          isContext: z.boolean().optional(),
        }),
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
  new URL("https://www.googleapis.com/oauth2/v3/certs"),
);

const app = new Hono<{ Bindings: Env }>();

app.get("/api/config", (c) => {
  c.header("Cache-Control", "max-age=86400"); // Allow this response to be cached for up to a day
  return c.json({ clientId: cfg.GOOGLE_WEB_CLIENT_ID }, 200);
});

app.post("/api/sign-in", async (c) => {
  const body: { googleIdToken: string } = await c.req.json();
  const googleToken = body.googleIdToken;

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
    // Invalid or expired Google ID token
    return c.json({ error: "Invalid or expired token" }, 401);
  }

  const googlePayload = googlePayloadSchema.safeParse(payload);
  if (!googlePayload.success) {
    return c.json({ error: "Bad request" }, 400);
  }

  const { azp, aud, hd, email_verified } = googlePayload.data;

  if (
    !safeEqual(azp, cfg.GOOGLE_ANDROID_CLIENT_ID) ||
    !safeEqual(aud, cfg.GOOGLE_WEB_CLIENT_ID)
  ) {
    return c.json({ error: "Bad request" }, 400);
  }

  if (hd !== cfg.EXPECTED_HD_DOMAIN) {
    return c.json({ error: "Invalid domain" }, 403);
  }
  if (!email_verified) {
    return c.json({ error: "Email not verified" }, 403);
  }

  const parsed = sessionDataSchema.safeParse({
    sub: payload.sub,
    email: payload.email,
    name: payload.name,
  });
  if (!parsed.success) {
    return c.json({ error: "Bad request" }, 400);
  }

  const sessionID = crypto.randomBytes(64).toString("base64url");

  await createSession(sessionID, parsed.data, cfg.SESSION_TTL, c.env);

  return c.json({ sessionID }, 200);
});

app.delete("/api/session", requireAuth, async (c) => {
  const token = c.get("token");
  await deleteSession(token, c.env);
  return c.json({ ok: true }, 200);
});

app.post("/api/chat", requireAuth, async (c) => {
  const userId = c.get("userId");

  // Rate limiting
  const rl = await checkChatRateLimit(userId);
  if (!rl.allowed) {
    return c.json({ error: rl.error }, 429, {
      "Retry-After": String(rl.retryAfter ?? 60),
    });
  }

  // Validate the input
  const body: unknown = await c.req.json();
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

  // Keep only the most recent messages in the conversation to reduce the number of tokens used
  // (except for the context message at the beginning; that one is always kept)
  const contextMsgs = messages.filter((m) => m.isContext);
  const conversationMsgs = messages.filter((m) => !m.isContext);
  const trimmed = [
    ...contextMsgs,
    ...conversationMsgs.slice(-MAX_LLM_MESSAGES),
  ];

  const response = await openai.chat.completions.create({
    model: cfg.AI_GATEWAY_MODEL,
    messages: [
      { role: "system", content: SYSTEM_PROMPT },
      ...trimmed.map((m) => ({ role: m.role, content: m.content })),
    ],
  });

  const message = response.choices[0].message.content ?? "";
  const signature = signMessage(
    "assistant",
    message,
    cfg.MESSAGE_SIGNING_SECRET,
  );

  return c.json({ message, signature });
});

export default app;
