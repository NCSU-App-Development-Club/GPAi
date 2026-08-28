package org.appdevncsu.gpai.security

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_IV_LENGTH = 12
private const val GCM_TAG_LENGTH = 128

// See https://developer.android.com/privacy-and-security/cryptography#perform-crypto-operations

/**
 * Encrypts [plain] with [key] (AES-256-GCM), prefixing the 12-byte IV to the ciphertext.
 */
fun encryptValue(plain: String, key: SecretKey): String {
    val cipher = Cipher.getInstance(TRANSFORMATION)
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val iv = cipher.iv
    val ciphertext = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
    return Base64.getEncoder().withoutPadding().encodeToString(iv + ciphertext)
}

/**
 * Inverse of [encryptValue]. Throws if the data was tampered with or encrypted under a different key.
 */
fun decryptValue(stored: String?, key: SecretKey): String? {
    if (stored == null) return null
    val bytes = Base64.getDecoder().decode(stored)
    val iv = bytes.copyOfRange(0, GCM_IV_LENGTH)
    val ciphertext = bytes.copyOfRange(GCM_IV_LENGTH, bytes.size)
    val cipher = Cipher.getInstance(TRANSFORMATION)
    cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
    return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
}
