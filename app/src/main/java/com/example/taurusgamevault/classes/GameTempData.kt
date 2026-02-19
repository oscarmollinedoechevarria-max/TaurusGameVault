package com.example.taurusgamevault.classes

import android.net.Uri
import com.example.taurusgamevault.Model.room.entities.Screenshot

//TODO: SCREENSHOTS EDIT - **
//TODO: SIMPLE MAIN MENU GAME EDIT AND HOVER - **
//TODO: SIMPLE LIST GAME EDIT AND HOVER - **
//TODO: On add a game to a plataform that is empty create the list of that plataform do it too with all basic tags
//TODO: GAMES AUTO ADD BY NAME
//todo: SHOW MORE SHOW LESS LIST
//TODO: FIX DATABSE LIST XML ON MAIN ACTIVITY
//TODO: FRAGMENT NAMES ON NAVIGATION
data class GameTempData(
    val imageUri: Uri?,
    val name: String,
    val description: String?,
    val releaseDate: String?,
    val playtime: Int,
    val personalRating: Float,
    val gameState: String?,
    val startDate: String?,
    val endDate: String?,
    val deadline: String?,
    val priority: String?,
    val screenshots: List<Uri>?,
    val allScreenshots: List<String>?,
    val plataforms: List<Long>?
)
