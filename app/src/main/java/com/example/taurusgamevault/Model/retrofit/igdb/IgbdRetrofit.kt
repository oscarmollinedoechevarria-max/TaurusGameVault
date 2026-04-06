package com.example.taurusgamevault.Model.retrofit.igdb

import android.content.Context
import com.example.taurusgamevault.R
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// singleton for igdb api for use as a service
object IgdbRetrofit {
    private const val BASE_URL = "https://api.igdb.com/v4/"

    lateinit var service: IgdbApiService
        private set

    fun init(context: Context) {
        service = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    .addInterceptor { chain ->
                        chain.proceed(
                            chain.request().newBuilder()
                                .addHeader("Client-ID", context.getString(R.string.igdb_client_key))
                                .addHeader("Authorization", "Bearer ${context.getString(R.string.igdb_access_key)}")
                                .build()
                        )
                    }
                    .build()
            )
            .build()
            .create(IgdbApiService::class.java)
    }
}