package com.andytools.retroclub.domain.usecase

import com.andytools.retroclub.domain.repository.MediaRepository
import javax.inject.Inject

class AuthenticateUserUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    suspend fun execute(username: String, password: String): String {
        return repository.authenticateUser(username, password)
    }
}