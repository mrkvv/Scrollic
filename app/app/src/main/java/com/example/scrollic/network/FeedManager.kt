package com.example.scrollic.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.*

class FeedManager(
    private val apiService: ApiService
) {

    suspend fun getFeed(limit: Int = 20): Result<FeedResponse> {
        return try {
            Log.d("FeedManager", "Getting feed with limit: $limit")
            val response = apiService.getFeed(limit)

            if (response.isSuccessful) {
                val feedResponse = response.body()!!
                Log.d("FeedManager", "Received ${feedResponse.feed.size} news items")
                Result.success(feedResponse)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Не авторизован"
                    422 -> "Неверный параметр limit"
                    429 -> "Слишком много запросов"
                    else -> "Ошибка: ${response.code()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Log.e("FeedManager", "Network error", e)
            Result.failure(Exception("Нет соединения с сервером"))
        } catch (e: HttpException) {
            Log.e("FeedManager", "HTTP error", e)
            Result.failure(Exception("Серверная ошибка: ${e.code()}"))
        } catch (e: Exception) {
            Log.e("FeedManager", "Unexpected error", e)
            Result.failure(Exception("Ошибка: ${e.message}"))
        }
    }
}