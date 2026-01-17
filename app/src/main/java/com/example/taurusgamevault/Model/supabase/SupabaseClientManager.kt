package com.example.taurusgamevault.Model.supabase

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import kotlin.time.Duration.Companion.seconds
import com.example.taurusgamevault.R


object SupabaseClientManager {

    lateinit var supabase: SupabaseClient

    fun init(context: Context) {
        supabase = createSupabaseClient(
            supabaseUrl = context.getString(R.string.supabase_url),
            supabaseKey = context.getString(R.string.supabase_key)
        ) {
            install(Storage) {
                transferTimeout = 120.seconds
            }
        }
    }
}