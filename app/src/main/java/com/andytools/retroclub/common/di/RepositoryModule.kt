package com.andytools.retroclub.common.di

import com.andytools.retroclub.data.remote.api.MediaApiService
import com.andytools.retroclub.data.remote.api.SettingsApiService
import com.andytools.retroclub.data.repository.MediaRepositoryImpl
import com.andytools.retroclub.data.repository.SettingsRepositoryImpl
import com.andytools.retroclub.domain.repository.MediaRepository
import com.andytools.retroclub.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideMediaApiService(okHttpClient: OkHttpClient): MediaApiService {
        return MediaApiService(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideMediaRepository(apiService: MediaApiService): MediaRepository {
        return MediaRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideSettingsApiService(okHttpClient: OkHttpClient): SettingsApiService {
        return SettingsApiService(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(apiService: SettingsApiService): SettingsRepository {
        return SettingsRepositoryImpl(apiService)
    }
}