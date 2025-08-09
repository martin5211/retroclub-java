package com.andytools.retroclub.common.di

import com.andytools.retroclub.data.remote.api.MediaApiService
import com.andytools.retroclub.data.repository.MediaRepositoryImpl
import com.andytools.retroclub.domain.repository.MediaRepository
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
}