import { z } from "zod";

const KEY_PREFIX = "session-";

export const googlePayloadSchema = z.object({
  azp: z.string().min(1),
  aud: z.string().min(1),
  hd: z.string().min(1),
  email_verified: z.literal(true).transform(() => true as boolean),
});

export const sessionDataSchema = z.object({
  sub: z.string().min(1),
  email: z.string().min(1),
  name: z.string().min(1),
});

export type SessionData = z.infer<typeof sessionDataSchema>;

function key(token: string): string {
  return `${KEY_PREFIX}${token}`;
}

export async function createSession(
  token: string,
  data: SessionData,
  ttlSeconds: number,
  env: Env,
): Promise<void> {
  await env.KV.put(key(token), JSON.stringify(data), {
    expirationTtl: ttlSeconds,
  });
}

export async function getSession(
  token: string,
  env: Env,
): Promise<SessionData | null> {
  return env.KV.get<SessionData>(key(token), {
    type: "json",
    cacheTtl: 60,
  });
}

export async function deleteSession(token: string, env: Env): Promise<void> {
  await env.KV.delete(key(token));
}
