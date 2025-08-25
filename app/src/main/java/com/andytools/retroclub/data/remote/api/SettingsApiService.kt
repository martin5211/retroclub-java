package com.andytools.retroclub.data.remote.api

import com.andytools.retroclub.common.constants.Constants
import com.andytools.retroclub.data.remote.dto.SettingsDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject

class SettingsApiService @Inject constructor(
    private val client: OkHttpClient
) {
    suspend fun authenticateUser(username: String, password: String): String = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("username", username)
            put("password", password)
        }
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(Constants.TOKEN_ENDPOINT)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            JSONObject(responseBody ?: "").getString("access_token")
        } else {
            throw Exception("Authentication failed: ${response.code}")
        }
    }

    suspend fun loadSettings(token: String): List<SettingsDto> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(Constants.SETTINGS_ENDPOINT)
            .addHeader("Authorization", "Bearer $token")
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            val jsonObject = JSONObject(responseBody ?: "{}")
            val settings = mutableListOf<SettingsDto>()

            // Iterate through all keys in the JSONObject
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = jsonObject.getString(key)
                settings.add(
                    SettingsDto(
                        setting_key = key,
                        setting_value = value
                    )
                )
            }

            settings
        } else {
            throw Exception("Failed to settingss: ${response.code}")
        }
    }
}