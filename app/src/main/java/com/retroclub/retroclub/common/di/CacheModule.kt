package com.retroclub.retroclub.common.di

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.database.StandaloneDatabaseProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CacheModule {

    @UnstableApi
    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, "exoplayer_cache")
        val databaseProvider = StandaloneDatabaseProvider(context)
        // Use NoOpCacheEvictor to prevent automatic cache eviction
        // This keeps cached segments even when they're no longer in the HLS manifest
        return SimpleCache(cacheDir, NoOpCacheEvictor(), databaseProvider)
    }
}