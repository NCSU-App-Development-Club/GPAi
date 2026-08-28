import crypto from "node:crypto";
import { safeEqual } from "./crypto";

/**
 * Server-side signing of assistant messages.
 *
 * The server signs each assistant message with an HMAC key that never leaves the backend. Clients
 * store the returned signature alongside the message and send it back on subsequent requests. The
 * server verifies the signature, so a user cannot tamper with assistant messages locally (e.g. to
 * jailbreak the conversation) without the signature check failing.
 */

function canonical(role: string, content: string): string {
  return `${role}:${content}`;
}

export function signMessage(role: string, content: string, secret: string): string {
  return crypto
    .createHmac("sha256", secret)
    .update(canonical(role, content))
    .digest("base64url");
}

export function verifyMessage(
  role: string,
  content: string,
  signature: string | undefined,
  secret: string
): boolean {
  if (!signature) return false;
  const expected = signMessage(role, content, secret);
  return safeEqual(signature, expected);
}
