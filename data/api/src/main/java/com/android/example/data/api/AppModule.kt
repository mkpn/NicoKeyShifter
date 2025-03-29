package com.android.example.data.api

import android.content.Context
import android.webkit.WebView
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRetrofit(
        @ApplicationContext context: Context
    ): ApiService {
        val okHttpClientBuilder = OkHttpClient.Builder()
        val logging = HttpLoggingInterceptor {
            Timber.tag("OkHttp log").d(it)
        }.apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        okHttpClientBuilder.addInterceptor(logging)

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        // UserAgent文字列の取得
        val userAgent = WebView(context).settings.userAgentString

        val retrofit = Retrofit.Builder()
            .baseUrl("https://snapshot.search.nicovideo.jp/api/v2/snapshot/")
            .client(
                okHttpClientBuilder.addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", userAgent)
                        .build()
                    chain.proceed(request)
                }.build()
            )
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit.create(ApiService::class.java)
    }
}

