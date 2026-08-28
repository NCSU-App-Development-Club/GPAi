package org.appdevncsu.gpai.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.appdevncsu.gpai.models.User
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Stores the user's credentials (identity + session token) encrypted at rest using a key held in
 * the Android Keystore.
 */
class CredentialsStore(context: Context) {

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    private fun getKey(): SecretKey {
        val alias = KEY_ALIAS
        if (!keyStore.containsAlias(alias)) {
            val generator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            generator.init(
                KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            generator.generateKey()
        }
        return keyStore.getKey(alias, null) as SecretKey
    }

    private fun encrypt(plain: String): String = encryptValue(plain, getKey())

    private fun decrypt(stored: String?): String? = decryptValue(stored, getKey())

    suspend fun user(): User? = withContext(Dispatchers.IO) {
        val id = prefs.getString(KEY_ID, null) ?: return@withContext null
        val name = prefs.getString(KEY_NAME, null) ?: return@withContext null
        val email = prefs.getString(KEY_EMAIL, null) ?: return@withContext null
        val photoURL = prefs.getString(KEY_PHOTO, null) ?: return@withContext null
        val token = decrypt(prefs.getString(KEY_TOKEN, null)) ?: return@withContext null
        User(
            name = name,
            email = email,
            id = id,
            photoURL = photoURL,
            token = token
        )
    }

    suspend fun save(user: User) = withContext(Dispatchers.IO) {
        prefs.edit().apply {
            putString(KEY_ID, user.id)
            putString(KEY_NAME, user.name)
            putString(KEY_EMAIL, user.email)
            putString(KEY_PHOTO, user.photoURL)
            putString(KEY_TOKEN, encrypt(user.token))
            apply()
        }
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val FILE_NAME = "credentials" // persisted as credentials.xml, excluded from backup
        private const val KEY_ALIAS = "gpai_credentials_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"

        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHOTO = "photo"
        private const val KEY_TOKEN = "token"
    }
}
