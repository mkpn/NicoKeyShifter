package com.nicokeyshifter

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("video/contents/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("targets") targets: String = "title",
        @Query("fields") fields: String = "contentId,title,viewCounter,thumbnailUrl",
        @Query("_sort") sort: String = "-viewCounter",
    ): Response<SearchResult>
}