package com.nicokeyshifter

import javax.inject.Inject

class ApiRequestService@Inject constructor(
    private val apiService: ApiService
) {
//    fun getMusicRanking() {
//        val request = Request.Builder()
//            .url("https://www.nicovideo.jp/ranking/genre/music_sound?term=24h&page=1&rss=2.0&lang=ja-jp")
//            .build()
//
//        OkHttpClient.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                response.body?.let {
//                    NicoNicoRssXmlParser().parse(it.byteStream())
//                }
//            }
//        })
//    }
}