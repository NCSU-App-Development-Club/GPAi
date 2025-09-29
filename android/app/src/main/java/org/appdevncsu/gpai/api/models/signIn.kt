package org.appdevncsu.gpai.api.models

data class SignInRequest(
    val googleIdToken: String
)

data class SignInResponse(
    val sessionID: String
)