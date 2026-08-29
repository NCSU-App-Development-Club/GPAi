import { bearerAuth } from "hono/bearer-auth";
import { getSession } from "./session";

const SESSION_TOKEN_LENGTH = 86; // 64 bytes base64url-encoded

/**
 * Shared bearer-auth middleware that verifies the session token exists in KV and
 * stashes `token` and `userId` on the Hono context for downstream handlers.
 */
export const requireAuth = bearerAuth({
  verifyToken: async (token, c) => {
    if (token.length !== SESSION_TOKEN_LENGTH) return false;

    const env = c.env as Env;
    const data = await getSession(token, env);
    if (data === null) return false;

    c.set("token", token);
    c.set("userId", data.sub);
    return true;
  },
});
