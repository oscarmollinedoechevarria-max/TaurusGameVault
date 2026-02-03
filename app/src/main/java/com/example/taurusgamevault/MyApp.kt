package com.example.taurusgamevault

import android.app.Application
import androidx.fragment.app.viewModels
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.taurusgamevault.Model.supabase.SupabaseClientManager
import com.example.taurusgamevault.mainscreen.MainViewModel
import kotlin.getValue

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()


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
    }
}
