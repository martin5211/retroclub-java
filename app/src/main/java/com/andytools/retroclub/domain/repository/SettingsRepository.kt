package com.andytools.retroclub.domain.repository

import com.andytools.retroclub.domain.model.Settings

interface SettingsRepository {
    suspend fun authenticateUser(username: String, password: String): String
    suspend fun getSettings(token: String): List<Settings>
}