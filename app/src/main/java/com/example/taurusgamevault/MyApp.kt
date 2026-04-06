package com.example.taurusgamevault

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.taurusgamevault.Model.retrofit.igdb.IgdbRetrofit
import com.example.taurusgamevault.Model.supabase.SupabaseClientManager

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Coil, Supabase, and Retrofit
        val imageLoader = ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(512 * 1024 * 1024)
                    .build()
            }
            .build()

        Coil.setImageLoader(imageLoader)

        SupabaseClientManager.init(this)

        IgdbRetrofit.init(this)
    }
}
