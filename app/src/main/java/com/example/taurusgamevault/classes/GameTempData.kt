package com.example.taurusgamevault.classes

import android.net.Uri

data class GameTempData(
    val imageUri: Uri? = null,
    val screenshotUris: List<Uri> = emptyList(),
    val name: String = "",
    val description: String? = null,
    val releaseDate: String? = null,
    val playtime: Int? = 0,
    val personalRating: Double? = 0.0,
    val gameState: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val deadline: String? = null,
    val priority: Int? = 0
)
