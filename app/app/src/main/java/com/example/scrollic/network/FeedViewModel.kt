package com.example.scrollic.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scrollic.network.FeedManager
import com.example.scrollic.network.NewsItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FeedUiState {
    object Idle : FeedUiState()
    object Loading : FeedUiState()
    data class Success(val news: List<NewsItem>, val total: Int) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}

class FeedViewModel(
    private val feedManager: FeedManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Idle)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private val pageSize = 20
    private var allNews = mutableListOf<NewsItem>()
    private var hasMore = true
    private var isLoading = false

    fun loadFeed() {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            val result = feedManager.getFeed(pageSize)

            if (result.isSuccess) {
                val feedResponse = result.getOrNull()!!
                allNews = feedResponse.feed.toMutableList()
                hasMore = feedResponse.feed.size >= pageSize
                _uiState.value = FeedUiState.Success(feedResponse.feed, feedResponse.total)
            } else {
                _uiState.value = FeedUiState.Error(result.exceptionOrNull()?.message ?: "Ошибка загрузки")
            }
            isLoading = false
        }
    }

    fun loadMore() {
        if (isLoading || !hasMore) return
        isLoading = true

        viewModelScope.launch {
            val currentSize = allNews.size
            val result = feedManager.getFeed(currentSize + pageSize)

            if (result.isSuccess) {
                val feedResponse = result.getOrNull()!!
                allNews = feedResponse.feed.toMutableList()
                hasMore = feedResponse.feed.size >= currentSize + pageSize
                _uiState.value = FeedUiState.Success(feedResponse.feed, feedResponse.total)
            }
            isLoading = false
        }
    }

    fun refresh() {
        currentPage = 0
        allNews.clear()
        hasMore = true
        loadFeed()
    }
}