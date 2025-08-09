package com.andytools.retroclub.common.utils

import android.util.Log

object Logger {
    private const val TAG = "Retroclub"

    fun d(message: String) = Log.d(TAG, message)
    fun e(message: String, throwable: Throwable? = null) = Log.e(TAG, message, throwable)
}