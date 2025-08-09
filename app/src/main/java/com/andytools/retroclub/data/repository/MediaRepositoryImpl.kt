package com.andytools.retroclub.data.repository

import com.andytools.retroclub.common.utils.Logger
import com.andytools.retroclub.data.remote.api.MediaApiService
import com.andytools.retroclub.data.remote.dto.MediaItemDto
import com.andytools.retroclub.domain.model.MediaItem
import com.andytools.retroclub.domain.repository.MediaRepository
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val apiService: MediaApiService
) : MediaRepository {
    override suspend fun authenticateUser(username: String, password: String): String {
        return try {
            apiService.authenticateUser(username, password)
        } catch (e: Exception) {
            Logger.e("Authentication failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getMediaItems(token: String): List<MediaItem> {
        return try {
            apiService.loadMediaItems(token).map {
                MediaItem(it.title, it.thumbnailUrl)
            }
        } catch (e: Exception) {
            Logger.e("Failed to load media items: ${e.message}", e)
            throw e
        }
    }

    override fun getSampleMediaItems(): List<MediaItem> {
        return listOf(
            MediaItem("Sting", "https://www.theaudiodb.com/images/media/album/thumb/ysqysy1558955451.jpg/medium"),
            MediaItem("INXS", "https://www.theaudiodb.com/images/media/album/thumb/xttuts1341508831.jpg/medium"),
            // ... other sample items as in original code
        )
    }
}