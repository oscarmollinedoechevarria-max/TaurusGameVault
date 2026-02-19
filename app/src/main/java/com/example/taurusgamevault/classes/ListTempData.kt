package com.example.taurusgamevault.classes

import android.net.Uri

data class ListTempData (
    val name: String,
    val description: String?,
    val image: Uri?,
    val games: List<Long>?
)