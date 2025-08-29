package com.retroclub.retroclub.data.repository

import com.retroclub.retroclub.common.utils.Logger
import com.retroclub.retroclub.data.remote.api.SettingsApiService
import com.retroclub.retroclub.domain.model.Settings
import com.retroclub.retroclub.domain.repository.SettingsRepository
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val apiService: SettingsApiService
) : SettingsRepository {
    override suspend fun authenticateUser(username: String, password: String): String {
        return try {
            apiService.authenticateUser(username, password)
        } catch (e: Exception) {
            Logger.e("Authentication failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getSettings(token: String): List<Settings> {
        return try {
            apiService.loadSettings(token).map {
                Settings(it.setting_key, it.setting_value)
            }
        } catch (e: Exception) {
            Logger.e("Failed to Settings: ${e.message}", e)
            throw e
        }
    }

}