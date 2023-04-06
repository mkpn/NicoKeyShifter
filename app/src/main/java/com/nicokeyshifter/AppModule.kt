package com.nicokeyshifter

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRetrofit(): ApiService {
        val okHttpClientBuilder = OkHttpClient.Builder()
        val logging = HttpLoggingInterceptor {
            Timber.tag("OkHttp log").d(it)
        }.apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        okHttpClientBuilder.addInterceptor(logging)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.search.nicovideo.jp/api/v2/snapshot/")
            .client(okHttpClientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }
}

