package org.appdevncsu.gpai.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import javax.crypto.AEADBadTagException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Unit tests for the AES-256-GCM encrypt/decrypt logic in [CredentialsStore].
 */
class CredentialsStoreCryptoTest {

    private val key: SecretKey = KeyGenerator.getInstance("AES").generateKey()

    @Test
    fun roundTrip_preservesPlaintext() {
        val plain = "secret-session-token-123"
        assertEquals(plain, decryptValue(encryptValue(plain, key), key))
    }

    @Test
    fun roundTrip_emptyString() {
        assertEquals("", decryptValue(encryptValue("", key), key))
    }

    @Test
    fun roundTrip_unicodeAndLongInput() {
        val plain = "héllo ☺ wørld ".repeat(50)
        assertEquals(plain, decryptValue(encryptValue(plain, key), key))
    }

    @Test
    fun nullInput_returnsNull() {
        assertNull(decryptValue(null, key))
    }

    @Test
    fun tamperedCiphertext_failsAuthentication() {
        val ciphertext = encryptValue("secure-value", key)
        // Flip the final base64 character to corrupt the encrypted bytes.
        val tampered = if (ciphertext.last() == 'A') {
            ciphertext.dropLast(1) + "B"
        } else {
            ciphertext.dropLast(1) + "A"
        }
        assertThrows(AEADBadTagException::class.java) {
            decryptValue(tampered, key)
        }
    }

    @Test
    fun wrongKey_failsAuthentication() {
        val ciphertext = encryptValue("secure-value", key)
        val otherKey = KeyGenerator.getInstance("AES").generateKey()
        assertThrows(AEADBadTagException::class.java) {
            decryptValue(ciphertext, otherKey)
        }
    }

    @Test
    fun differentPlaintexts_produceDifferentCiphertext() {
        // GCM uses a random IV per encryption, so identical plaintexts must not produce identical output.
        val a = encryptValue("same", key)
        val b = encryptValue("same", key)
        assert(a != b)
    }
}
