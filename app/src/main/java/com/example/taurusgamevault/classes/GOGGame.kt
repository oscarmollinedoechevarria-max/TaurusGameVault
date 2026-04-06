package com.example.taurusgamevault.classes

import android.net.Uri
import com.example.taurusgamevault.enums.Priority


data class GOGGame(
    val id: Long,
    val title: String,
    val image: String
)

fun GOGGame.toTempGameData() = GameTempData(
    imageUri = if (image.isNotEmpty())
        Uri.parse("https:$image.png") else null,
    name = title,
    description = "",
    releaseDate = null,
    screenshots = null,
    playtime = 0,
    personalRating = 0f,
    gameState = null,
    startDate = null,
    endDate = null,
    deadline = null,
    priority = Priority.BACK_LOG.text,
    allScreenshots = null,
    plataforms = null
)