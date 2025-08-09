package com.andytools.retroclub.data.remote.api

import com.andytools.retroclub.common.constants.Constants
import com.andytools.retroclub.data.remote.dto.MediaItemDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class MediaApiService @Inject constructor(
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

    suspend fun loadMediaItems(token: String): List<MediaItemDto> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(Constants.MEDIA_ITEMS_ENDPOINT)
            .addHeader("Authorization", "Bearer $token")
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            val jsonArray = JSONArray(responseBody ?: "[]")
            val items = mutableListOf<MediaItemDto>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                items.add(
                    MediaItemDto(
                        title = item.getString("title"),
                        thumbnailUrl = item.getString("thumbnail_url")
                    )
                )
            }
            items
        } else {
            throw Exception("Failed to load media items: ${response.code}")
        }
    }
}