package com.example.scrollic.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.scrollic.network.FeedManager

class FeedViewModelFactory(
    private val feedManager: FeedManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FeedViewModel(feedManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}