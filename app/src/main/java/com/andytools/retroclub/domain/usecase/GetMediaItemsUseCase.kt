package com.andytools.retroclub.domain.usecase

import com.andytools.retroclub.domain.model.MediaItem
import com.andytools.retroclub.domain.repository.MediaRepository
import javax.inject.Inject

class GetMediaItemsUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    suspend fun execute(token: String?): List<MediaItem> {
        return if (token != null) {
            repository.getMediaItems(token)
        } else {
            repository.getSampleMediaItems()
        }
    }
}