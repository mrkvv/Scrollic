package com.example.scrollic.network

import android.util.Log
import com.example.scrollic.database.LocalDatabaseManager
import com.example.scrollic.localDb.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthManager(
    private val apiService: ApiService,
    private val dbManager: LocalDatabaseManager
) {

    suspend fun register(name: String, password: String): Result<User> {
        return try {
            Log.d("AuthManager", "=== REGISTER START ===")
            val response = apiService.register(RegisterRequest(name, password))
            Log.d("AuthManager", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val authResponse = response.body()!!
                val user = User(
                    id = authResponse.user.id.toLong(),
                    name = authResponse.user.name,
                    token = authResponse.access_token,
                    avatar_url = null
                )

                withContext(Dispatchers.IO) {
                    // При регистрации сначала удаляем старого пользователя (если есть)
                    dbManager.deleteUser()
                    // Затем сохраняем нового
                    dbManager.saveUser(
                        id = user.id,
                        name = user.name,
                        token = user.token,
                        avatarUrl = null
                    )
                }
                Log.d("AuthManager", "✅ Registration successful!")
                Result.success(user)
            } else {
                val errorMsg = when (response.code()) {
                    409 -> "Пользователь с таким именем уже существует"
                    else -> "Ошибка регистрации: ${response.code()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Log.e("AuthManager", "Network error", e)
            Result.failure(Exception("Нет соединения с сервером: ${e.message}"))
        } catch (e: Exception) {
            Log.e("AuthManager", "Error", e)
            Result.failure(Exception("Ошибка: ${e.message}"))
        }
    }

    suspend fun login(name: String, password: String): Result<User> {
        return try {
            Log.d("AuthManager", "=== LOGIN START ===")
            val response = apiService.login(LoginRequest(name, password))
            Log.d("AuthManager", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val authResponse = response.body()!!
                Log.d("AuthManager", "✅ Login successful!")

                val user = User(
                    id = authResponse.user.id.toLong(),
                    name = authResponse.user.name,
                    token = authResponse.access_token,
                    avatar_url = null
                )

                withContext(Dispatchers.IO) {
                    // Получаем текущего пользователя из БД
                    val currentUser = dbManager.getCurrentUser()

                    if (currentUser != null) {
                        // Пользователь уже есть в БД - просто обновляем токен
                        Log.d("AuthManager", "Updating existing user token")
                        dbManager.updateUserToken(currentUser.id.toInt(), user.token)

                        // Если имя изменилось на сервере, тоже обновляем
                        if (currentUser.name != user.name) {
                            dbManager.updateUserName(currentUser.id.toInt(), user.name)
                        }
                    } else {
                        // Пользователя нет в БД - сохраняем нового
                        Log.d("AuthManager", "Saving new user to DB")
                        dbManager.saveUser(
                            id = user.id,
                            name = user.name,
                            token = user.token,
                            avatarUrl = null
                        )
                    }
                }

                Result.success(user)
            } else {
                Result.failure(Exception("Неверное имя пользователя или пароль"))
            }
        } catch (e: IOException) {
            Log.e("AuthManager", "Network error", e)
            Result.failure(Exception("Нет соединения с сервером"))
        } catch (e: Exception) {
            Log.e("AuthManager", "Error", e)
            Result.failure(Exception("Ошибка: ${e.message}"))
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val response = apiService.logout()
            if (response.isSuccessful) {
                withContext(Dispatchers.IO) {
                    dbManager.deleteUser()  // Очищаем данные пользователя при выходе
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка выхода"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Flow<Boolean> = flow {
        emit(dbManager.isLoggedIn())
    }

    suspend fun getCurrentUser(): User? = dbManager.getCurrentUser()
}