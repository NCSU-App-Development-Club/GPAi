import crypto from "node:crypto";

/**
 * Constant-time string comparison that returns `false` when the two values
 * differ in length, instead of throwing an error like `crypto.timingSafeEqual` does.
 */
export function safeEqual(a: string, b: string): boolean {
  const bufA = Buffer.from(a);
  const bufB = Buffer.from(b);
  if (bufA.length !== bufB.length) {
    return false;
  }
  return crypto.timingSafeEqual(bufA, bufB);
}
