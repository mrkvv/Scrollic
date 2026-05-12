package com.example.scrollic.network

import com.example.scrollic.network.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth endpoints
    @POST("/api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("/api/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("/api/users/me")
    suspend fun getCurrentUser(): Response<UserResponse>

    @PUT("/api/users/me")
    suspend fun changeUserName(
        @Body request: ChangeNameRequest
    ): Response<UserResponse>

    @PUT("/api/users/me/password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<Unit>

    // Feed endpoints
    @GET("/api/feed")
    suspend fun getFeed(
        @Query("limit") limit: Int
    ): Response<FeedResponse>

    @POST("/api/actions/batch")
    suspend fun sendActionBatch(
        @Body request: ActionBatchRequest
    ): Response<ActionBatchResponse>

    @POST("/api/actions/like")
    suspend fun likeNews(
        @Body request: ActionRequest
    ): Response<ActionBatchResponse>

    @DELETE("/api/actions/like")
    suspend fun unlikeNews(
        @Body request: ActionRequest
    ): Response<ActionBatchResponse>

    @POST("/api/actions/seen")
    suspend fun markAsSeen(
        @Body request: ActionRequest
    ): Response<ActionBatchResponse>

    @GET("/api/actions/status/{news_id}")
    suspend fun getActionStatus(
        @Path("news_id") newsId: String
    ): Response<ActionStatusResponse>

}