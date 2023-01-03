package com.nicokeyshifter

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class ApiRequestService {
    fun getMusicRanking() {
        val request = Request.Builder()
            .url("https://www.nicovideo.jp/ranking/genre/music_sound?term=24h&page=1&rss=2.0&lang=ja-jp")
            .build()

        OkHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let {
                    NicoNicoRssXmlParser().parse(it.byteStream())
                }
            }
        })
    }
}