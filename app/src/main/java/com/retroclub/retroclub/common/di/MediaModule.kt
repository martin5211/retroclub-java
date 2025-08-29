package com.retroclub.retroclub.common.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.LoadControl
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.google.android.gms.cast.framework.CastContext
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(ActivityComponent::class)
object MediaModule {
    @Provides
    fun provideLoadControl(): LoadControl {
        // 30 minutes = 30 * 60 * 1000 = 1,800,000 milliseconds
        val maxBufferDurationMs = 30 * 60 * 1000
        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                DefaultLoadControl.DEFAULT_MIN_BUFFER_MS, // min buffer: 50 seconds
                maxBufferDurationMs, // max buffer: 30 minutes
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS, // buffer for playback: 2.5 seconds
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS // buffer after rebuffer: 5 seconds
            )
            .build()
    }


    @Provides
    fun provideCacheDataSourceFactory(@ActivityContext context: Context, cache: Cache): CacheDataSource.Factory {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("RetroClub/1.0")
            .setConnectTimeoutMs(30000)
            .setReadTimeoutMs(30000)
        
        val defaultDataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        
        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(defaultDataSourceFactory)
            .setCacheWriteDataSinkFactory(null) // Use default
            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    @Provides
    fun provideExoPlayer(
        @ActivityContext context: Context, 
        loadControl: LoadControl,
        cacheDataSourceFactory: CacheDataSource.Factory
    ): ExoPlayer = ExoPlayer.Builder(context)
        .setLoadControl(loadControl)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(cacheDataSourceFactory)
        )
        .build()

    @Provides
    fun provideCastContext(@ActivityContext context: Context): CastContext? =
        try {
            CastContext.getSharedInstance(context)
        } catch (e: Exception) {
            null
        }
}