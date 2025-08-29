package com.retroclub.retroclub.domain.repository

import com.retroclub.retroclub.domain.model.Settings

interface SettingsRepository {
    suspend fun authenticateUser(username: String, password: String): String
    suspend fun getSettings(token: String): List<Settings>
}