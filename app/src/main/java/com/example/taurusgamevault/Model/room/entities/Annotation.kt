package com.example.taurusgamevault.Model.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "annotation")
data class Annotation(
    @PrimaryKey(autoGenerate = true) val annotation_id: Long = 0L,
    val text: String,
    val game_id: Long
)
