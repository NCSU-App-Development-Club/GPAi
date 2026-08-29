package org.appdevncsu.gpai.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Intercepts HTTP requests to add the user's authorization token.
 * The token is updated in [org.appdevncsu.gpai.viewmodel.AuthViewModel].
 */
class AuthorizationInterceptor : Interceptor {

    @Volatile
    private var token: String? = null

    fun setToken(newToken: String) {
        token = newToken
    }

    fun clearToken() {
        token = null
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return if (token != null) {
            chain.proceed(
                chain.request().newBuilder().header("Authorization", "Bearer $token").build()
            )
        } else {
            chain.proceed(chain.request())
        }
    }
}
