package com.example.scrollic.database

import com.example.Scrollic.localDb.User
import com.example.scrollic.localDb.LocalDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class LocalDatabaseManager(private val database: LocalDatabase) {

    private val query = database.localDatabaseQueries

    /**
     * Сохраняет пользователя после регистрации или входа
     */
    suspend fun saveUser(
        id: Long,
        name: String,
        token: String,
        avatarUrl: String? = null
    ) {
        withContext(Dispatchers.IO) {
            // Сохраняем нового
            query.insertUser(
                id = id,
                name = name,
                token = token,
                avatar_url = avatarUrl
            )
        }
    }

    /**
     * Получает текущего пользователя
     */
    suspend fun getCurrentUser(): User? {
        return withContext(Dispatchers.IO) {
            query.selectCurrentUser()
                .executeAsOneOrNull()
                ?.let { user ->
                    User(
                        id = user.id,
                        name = user.name,
                        token = user.token,
                        logged_in_at = user.logged_in_at,
                        avatar_url = user.avatar_url
                    )
                }
        }
    }

    /**
     * Получает пользователя по ID
     */
    suspend fun getUserById(id: Int): User? {
        return withContext(Dispatchers.IO) {
            query.selectUserById(id.toLong())
                .executeAsOneOrNull()
                ?.let { user ->
                    User(
                        id = user.id,
                        name = user.name,
                        token = user.token,
                        logged_in_at = user.logged_in_at,
                        avatar_url = user.avatar_url
                    )
                }
        }
    }

    /**
     * Обновляет имя пользователя
     */
    suspend fun updateUserName(userId: Int, newName: String) {
        withContext(Dispatchers.IO) {
            query.updateUserName(
                name = newName,
                id = userId.toLong()
            )
        }
    }

    /**
     * Обновляет токен
     */
    suspend fun updateUserToken(userId: Int, newToken: String) {
        withContext(Dispatchers.IO) {
            query.updateUserToken(
                token = newToken,
                id = userId.toLong()
            )
        }
    }

    /**
     * Обновляет аватарку
     */
    suspend fun updateUserAvatar(userId: Int, avatarUrl: String?) {
        withContext(Dispatchers.IO) {
            query.updateUserAvatar(
                avatar_url = avatarUrl,
                id = userId.toLong()
            )
        }
    }

    /**
     * Проверяет, авторизован ли пользователь
     */
    suspend fun isLoggedIn(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = query.selectCurrentUser().executeAsOneOrNull()
            user != null && user.token.isNotBlank()
        }
    }

    /**
     * Получает токен текущего пользователя
     */
    suspend fun getToken(): String? {
        return getCurrentUser()?.token
    }
}