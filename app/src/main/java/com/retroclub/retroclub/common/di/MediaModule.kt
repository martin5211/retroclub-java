package com.retroclub.retroclub.common.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
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
    fun provideExoPlayer(@ActivityContext context: Context): ExoPlayer = 
        ExoPlayer.Builder(context).build()

    @Provides
    fun provideCastContext(@ActivityContext context: Context): CastContext? =
        try {
            CastContext.getSharedInstance(context)
        } catch (e: Exception) {
            null
        }
}