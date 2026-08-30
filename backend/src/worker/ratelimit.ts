const DAILY_LIMIT = 100;
const FLAG_DAILY_LIMIT = 3;

function dayKey(userId: string, prefix = ""): string {
  const day = new Date().toISOString().slice(0, 10); // YYYY-MM-DD (UTC)
  return `ratelimit-day-${prefix}${userId}-${day}`;
}

function endOfDayEpoch(): number {
  const d = new Date();
  d.setUTCHours(23, 59, 59, 0);
  return Math.floor(d.getTime() / 1000);
}

async function getDailyCount(userId: string, env: Env, prefix = ""): Promise<number> {
  const value = await env.KV.get(dayKey(userId, prefix));
  return value ? parseInt(value, 10) || 0 : 0;
}

async function incDaily(userId: string, env: Env, prefix = ""): Promise<void> {
  const key = dayKey(userId, prefix);
  const now = Math.floor(Date.now() / 1000);
  const ttl = Math.max(60, endOfDayEpoch() - now);
  const current = await getDailyCount(userId, env, prefix);
  await env.KV.put(key, String(current + 1), { expirationTtl: ttl });
}

export interface RateLimitResult {
  allowed: boolean;
  retryAfter?: number;
  error?: string;
}

/**
 * Enforces per-session rate limits for `/api/chat`:
 *  - a per-day quota via a KV counter (the native `simple` Rate Limit binding only supports 10s/60s windows), and
 *  - a per-minute limit via the native Cloudflare Rate Limit binding (RATE_LIMITER_MIN), which is
 *    far cheaper and more accurate than a KV read/write on every request.
 */
export async function checkChatRateLimit(userId: string, env: Env): Promise<RateLimitResult> {
  const dailyCount = await getDailyCount(userId, env);
  if (dailyCount >= DAILY_LIMIT) {
    const retryAfter = Math.max(1, endOfDayEpoch() - Math.floor(Date.now() / 1000));
    return { allowed: false, retryAfter, error: "Daily request limit exceeded" };
  }

  const minute = await env.RATE_LIMITER_MIN.limit({
    key: userId,
  });
  if (!minute.success) {
    return {
      allowed: false,
      retryAfter: 60,
      error: "Rate limit exceeded",
    };
  }

  await incDaily(userId, env, "chat-");
  return { allowed: true };
}

/**
 * Enforces a per-day quota for `/api/flag` via a KV counter, keyed separately from
 * chat rate limits using the "flag-" prefix.
 */
export async function checkFlagRateLimit(userId: string, env: Env): Promise<RateLimitResult> {
  const dailyCount = await getDailyCount(userId, env, "flag-");
  if (dailyCount >= FLAG_DAILY_LIMIT) {
    const retryAfter = Math.max(1, endOfDayEpoch() - Math.floor(Date.now() / 1000));
    return { allowed: false, retryAfter, error: "Daily report limit exceeded" };
  }

  await incDaily(userId, env, "flag-");
  return { allowed: true };
}
