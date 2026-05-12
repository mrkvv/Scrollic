package com.example.scrollic.network

/// МОДЕЛИ ДЛЯ REQUEST
// -- UserService --
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

// -- FeedService --
data class ActionBatchRequest(
    val batch_id: String,
    val client_timestamp: Long,
    val actions: List<Action>
)

data class Action(
    val action_id: String,
    val news_id: String,
    val action: String, // "like", "seen", "unlike"
    val timestamp: Long
)

data class ActionRequest(
    val news_id: String
)

data class ActionStatusResponse(
    val news_id: String,
    val liked: Boolean,
    val seen: Boolean
)

data class ActionBatchResponse(
    val status: String,
    val message: String,
    val batch_id: String,
    val received: Int,
    val processed: Int
)
data class ChangePasswordRequest(
    val old_password: String,
    val new_password: String
)

/// МОДЕЛИ ДЛЯ RESPONSE
// -- UserService --
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

// -- FeedService --
data class FeedResponse(
    val feed: List<NewsItem>,
    val total: Int,
    val limit: Int
)

data class NewsItem(
    val id: String,
    val head: String,
    val summary: String?,
    val text: String,
    val url: String,
    val url_picture: String?,
    val popularity: Int,
    val theme_id: Int,
    val created_at: String
)







