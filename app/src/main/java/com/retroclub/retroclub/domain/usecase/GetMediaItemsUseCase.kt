package com.retroclub.retroclub.domain.usecase

import com.retroclub.retroclub.domain.model.MediaItem
import com.retroclub.retroclub.domain.repository.MediaRepository
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