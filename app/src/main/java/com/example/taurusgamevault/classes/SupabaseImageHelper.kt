package com.example.taurusgamevault.classes

// supabase client for images, used object for singleton
object SupabaseImageHelper {
    private const val SUPABASE_URL = "https://swsawzjvsxtesdhbsuxn.supabase.co"
    private const val STORAGE_BUCKET = "filesdatabase"

    fun getImageUrl(imagePath: String?): String? {
        if (imagePath.isNullOrBlank()) return null

        if (imagePath.startsWith("http")) return imagePath

        return "$SUPABASE_URL/storage/v1/object/public/$STORAGE_BUCKET/$imagePath"
    }
}