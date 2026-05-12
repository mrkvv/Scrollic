package com.example.scrollic.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.*

class ActionManager(
    private val apiService: ApiService
) {

    suspend fun sendActionBatch(batchId: String, actions: List<Action>): Result<ActionBatchResponse> {
        return try {
            val request = ActionBatchRequest(
                batch_id = batchId,
                client_timestamp = System.currentTimeMillis(),
                actions = actions
            )

            val response = apiService.sendActionBatch(request)

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to send actions: ${response.code()}"))
            }
        } catch (e: IOException) {
            Log.e("ActionManager", "Network error", e)
            Result.failure(Exception("Нет соединения с сервером"))
        } catch (e: Exception) {
            Log.e("ActionManager", "Error", e)
            Result.failure(e)
        }
    }

    suspend fun like(newsId: String): Result<ActionBatchResponse> {
        return try {
            val response = apiService.likeNews(ActionRequest(newsId))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to like: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlike(newsId: String): Result<ActionBatchResponse> {
        return try {
            val response = apiService.unlikeNews(ActionRequest(newsId))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to unlike: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsSeen(newsId: String): Result<ActionBatchResponse> {
        return try {
            val response = apiService.markAsSeen(ActionRequest(newsId))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to mark as seen: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActionStatus(newsId: String): Result<ActionStatusResponse> {
        return try {
            val response = apiService.getActionStatus(newsId)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}