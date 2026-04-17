package com.example.scrollic.network

import com.example.scrollic.network.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("/api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("/api/auth/logout")
    suspend fun logout()

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
}