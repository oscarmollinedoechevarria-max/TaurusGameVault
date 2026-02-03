package com.example.taurusgamevault.classes

import android.net.Uri
import com.example.taurusgamevault.Model.room.entities.Screenshot

data class GameTempData(
    val imageUri: Uri? = null,
    val name: String = "",
    val description: String? = null,
    val releaseDate: String? = null,
    val playtime: Int? = 0,
    val personalRating: Float? = 0.0f,
    val gameState: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val deadline: String? = null,
    val priority: String? = "",
    val screenshots: List<Uri>? = null,
    val plataforms: List<Long>? = emptyList()
)
