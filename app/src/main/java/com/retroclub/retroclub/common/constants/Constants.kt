package com.retroclub.retroclub.common.constants

object Constants {
    const val VIDEO_URL = "https://live20.bozztv.com/akamaissh101/ssh101/retroclub/playlist.m3u8"
//    const val VIDEO_URL = "https://video08.logicahost.com.br/retroclub/retroclub/playlist.m3u8"
    const val MEDIA_CONTROLLER_TIMEOUT = 3000L // 3 seconds
    const val API_BASE_URL = "https://api.retroclub.xyz"
    const val TOKEN_ENDPOINT = "$API_BASE_URL/login"
    const val MEDIA_ITEMS_ENDPOINT = "$API_BASE_URL/items"
    const val SETTINGS_ENDPOINT = "$API_BASE_URL/settings"
    const val REFRESH_INTERVAL_MINUTES = 1L
    const val REFRESH_INTERVAL_MS = REFRESH_INTERVAL_MINUTES * 60 * 1000L

    const val WHATSAPP_GROUP_URL = "https://chat.whatsapp.com/DlTY8bciHttJeeI9wVGAx7?mode=ac_t"
}