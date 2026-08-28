import { env } from "cloudflare:workers";

const DAILY_LIMIT = 100;

function dayKey(token: string): string {
  const day = new Date().toISOString().slice(0, 10); // YYYY-MM-DD (UTC)
  return `ratelimit-day-${token}-${day}`;
}

function endOfDayEpoch(): number {
  const d = new Date();
  d.setUTCHours(23, 59, 59, 0);
  return Math.floor(d.getTime() / 1000);
}

async function getDailyCount(token: string): Promise<number> {
  const value = await env.KV.get(dayKey(token));
  return value ? parseInt(value, 10) || 0 : 0;
}

async function incDaily(token: string): Promise<void> {
  const key = dayKey(token);
  const now = Math.floor(Date.now() / 1000);
  const ttl = Math.max(60, endOfDayEpoch() - now);
  const current = await getDailyCount(token);
  await env.KV.put(key, String(current + 1), { expirationTtl: ttl });
}

export interface RateLimitResult {
  allowed: boolean;
  retryAfter?: number;
  error?: string;
}

/**
 * Enforces per-session rate limits for `/api/chat`:
 *  - a per-day quota via a KV counter (the native `simple` Rate Limit binding only supports
 *    10s/60s windows, not a daily one, on this wrangler version), and
 *  - a per-minute limit via the native Cloudflare Rate Limit binding (RATE_LIMITER_MIN), which is
 *    far cheaper and more accurate than a KV read/write on every request.
 */
export async function checkChatRateLimit(token: string): Promise<RateLimitResult> {
  const dailyCount = await getDailyCount(token);
  if (dailyCount >= DAILY_LIMIT) {
    const retryAfter = Math.max(1, endOfDayEpoch() - Math.floor(Date.now() / 1000));
    return { allowed: false, retryAfter, error: "Daily request limit exceeded" };
  }

  const minute = (await env.RATE_LIMITER_MIN.limit({
    key: token,
  })) as { success: boolean; reset?: number };
  if (!minute.success) {
    return {
      allowed: false,
      retryAfter: Math.max(1, (minute.reset ?? 0) - Math.floor(Date.now() / 1000)),
      error: "Rate limit exceeded",
    };
  }

  await incDaily(token);
  return { allowed: true };
}
