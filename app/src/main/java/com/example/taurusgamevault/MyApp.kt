package com.example.taurusgamevault

import android.app.Application
import com.example.taurusgamevault.Model.supabase.SupabaseClientManager

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        SupabaseClientManager.init(this)
    }
}
