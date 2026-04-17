package com.example.scrollic.network

/// МОДЕЛИ ДЛЯ REQUEST

data class RegisterRequest(
    val name: String,
    val password: String
)

data class LoginRequest(
    val name: String,
    val password: String
)

data class ChangeNameRequest(
    val name: String
)

data class ChangePasswordRequest(
    val old_password: String,
    val new_password: String
)

/// МОДЕЛИ ДЛЯ RESPONSE

data class AuthResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val user: UserInfo
) {
    data class UserInfo(
        val id: Int,
        val name: String,
        val created_at: String
    )
}

data class UserResponse(
    val id: Int,
    val name: String,
    val created_at: String
)





