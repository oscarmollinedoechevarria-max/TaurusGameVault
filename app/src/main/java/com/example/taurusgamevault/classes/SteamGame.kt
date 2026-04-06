package com.example.taurusgamevault.classes

import android.net.Uri
import com.example.taurusgamevault.enums.Priority

data class SteamGame(
    val appId: Int,
    val name: String,
    val playtimeMinutes: Int
)

fun SteamGame.toTempGameData() = GameTempData(
    imageUri = Uri.parse("https://cdn.akamai.steamstatic.com/steam/apps/$appId/header.jpg"),
    name = name,
    description = "",
    releaseDate = null,
    screenshots = listOf(
        Uri.parse("https://cdn.akamai.steamstatic.com/steam/apps/$appId/ss_1.jpg")
    ),
    playtime = playtimeMinutes / 60,
    personalRating = 0f,
    gameState = null,
    startDate = null,
    endDate = null,
    deadline = null,
    priority = Priority.BACK_LOG.text,
    allScreenshots = null,
    plataforms = null
)
