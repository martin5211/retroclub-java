package com.retroclub.retroclub.domain.usecase

import com.retroclub.retroclub.common.constants.Constants
import com.retroclub.retroclub.domain.repository.SettingsRepository
import javax.inject.Inject

class GetStreamUrlUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend fun execute(token: String?): String {
        return if (token != null) {
            repository.getSettings(token)
                .stream()
                .filter { it.setting_key == "stream_url" }
                .map { it.setting_value }
                .findFirst()
                .orElse(Constants.VIDEO_URL)
        } else {
            Constants.VIDEO_URL
        }
    }
}