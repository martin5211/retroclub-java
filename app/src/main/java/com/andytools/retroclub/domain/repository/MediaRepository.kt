package com.andytools.retroclub.domain.repository

import com.andytools.retroclub.domain.model.MediaItem

interface MediaRepository {
    suspend fun authenticateUser(username: String, password: String): String
    suspend fun getMediaItems(token: String): List<MediaItem>
    fun getSampleMediaItems(): List<MediaItem>
}