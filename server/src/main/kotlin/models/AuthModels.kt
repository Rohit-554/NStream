package models

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AccountCreatedResponse(
    val accountId : String,
    val email: String,
    val verificationRequired: Boolean = true
)

@Serializable
data class LoginAcceptedResponse(
    val accountId: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class EmailRequest(val email: String)

@Serializable
data class TokenRequest(val token: String)

@Serializable
data class ResetPasswordRequest(val token: String, val password: String)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class MessageResponse(val message: String)