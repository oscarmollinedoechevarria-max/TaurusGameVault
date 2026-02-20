package com.example.taurusgamevault.Model.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "annotation")
data class Annotation(
    @PrimaryKey val annotation_id: Int,
    val text: String,
    val game_id: Int? = null
)
