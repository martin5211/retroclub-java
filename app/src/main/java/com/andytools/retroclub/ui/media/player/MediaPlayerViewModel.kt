package com.andytools.retroclub.ui.media.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andytools.retroclub.common.constants.Constants
import com.andytools.retroclub.common.utils.Logger
import com.andytools.retroclub.domain.model.MediaItem
import com.andytools.retroclub.domain.usecase.AuthenticateUserUseCase
import com.andytools.retroclub.domain.usecase.GetMediaItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaPlayerViewModel @Inject constructor(
    private val authenticateUserUseCase: AuthenticateUserUseCase,
    private val getMediaItemsUseCase: GetMediaItemsUseCase
) : ViewModel() {
    private var accessToken: String? = null
    private val _mediaItems = MutableLiveData<List<MediaItem>>(emptyList())
    val mediaItems: LiveData<List<MediaItem>> get() = _mediaItems
    private val _mediaItemsVersion = MutableLiveData<Int>(0)
    val mediaItemsVersion: LiveData<Int> get() = _mediaItemsVersion
    private var refreshJob: Job? = null
    private var isRefreshLoopActive = false

    fun initialize() {
        viewModelScope.launch {
            try {
                authenticate()
                loadMediaItems()
                startRefreshLoop()
            } catch (e: Exception) {
                Logger.e("Failed to initialize API: ${e.message}", e)
                loadSampleData() // Now a suspend function, called within coroutine
                startRefreshLoop()
            }
        }
    }

    private suspend fun authenticate() {
        accessToken = authenticateUserUseCase.execute("admin", "admin123")
        Logger.d("Authentication successful")
    }

    private suspend fun loadMediaItems() {
        val items = getMediaItemsUseCase.execute(accessToken)
        _mediaItems.postValue(items)
        _mediaItemsVersion.postValue(_mediaItemsVersion.value!! + 1)
        Logger.d("Loaded ${items.size} media items")
    }

    private suspend fun loadSampleData() { // Changed to suspend
        val items = getMediaItemsUseCase.execute(null)
        _mediaItems.postValue(items)
        _mediaItemsVersion.postValue(_mediaItemsVersion.value!! + 1)
    }

    private fun startRefreshLoop() {
        if (isRefreshLoopActive) return
        isRefreshLoopActive = true
        refreshJob = viewModelScope.launch {
            while (isRefreshLoopActive) {
                delay(Constants.REFRESH_INTERVAL_MS)
                try {
                    if (accessToken == null) authenticate()
                    loadMediaItems()
                } catch (e: Exception) {
                    Logger.e("Error during refresh: ${e.message}", e)
                    if (e.message?.contains("Authentication") == true || e.message?.contains("401") == true) {
                        accessToken = null
                    }
                }
            }
        }
    }

    fun stopRefreshLoop() {
        isRefreshLoopActive = false
        refreshJob?.cancel()
        refreshJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopRefreshLoop()
    }
}