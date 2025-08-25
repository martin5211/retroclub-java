package com.andytools.retroclub.domain.usecase

import com.andytools.retroclub.domain.repository.SettingsRepository
import javax.inject.Inject

class AuthenticateSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend fun execute(username: String, password: String): String {
        return repository.authenticateUser(username, password)
    }
}