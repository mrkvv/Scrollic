package com.example.scrollic.network

import android.util.Log
import com.example.scrollic.database.LocalDatabaseManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking

class RetrofitInstance(private val dbManager: LocalDatabaseManager) {

    init {
        Log.d("RetrofitInstance", "=== INITIALIZING RETROFIT ===")
        Log.d("RetrofitInstance", "Base URL: http://10.0.2.2:8080/")
    }

    private val authInterceptor = Interceptor { chain ->
        val token = runBlocking { dbManager.getToken() }
        Log.d("Retrofit", "Interceptor: token = ${token?.take(20)}...")

        val request = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            request.addHeader("Authorization", "Bearer $token")
            Log.d("Retrofit", "Added Authorization header")
        }

        val finalRequest = request.build()
        Log.d("Retrofit", "Request URL: ${finalRequest.url}")
        Log.d("Retrofit", "Request method: ${finalRequest.method}")

        val response = chain.proceed(finalRequest)
        Log.d("Retrofit", "Response code: ${response.code}")

        response
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor { chain ->
            val request = chain.request()
            Log.d("OkHttp", "→ ${request.method} ${request.url}")
            val response = chain.proceed(request)
            Log.d("OkHttp", "← ${response.code}")
            response
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}