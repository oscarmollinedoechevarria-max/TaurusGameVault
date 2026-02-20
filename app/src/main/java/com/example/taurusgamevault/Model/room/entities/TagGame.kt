package com.example.taurusgamevault.Model.room.entities

import androidx.room.Entity

@Entity(
    tableName = "tag_game",
    primaryKeys = ["game_id", "tag_id"]
)
data class TagGame(
    val game_id: Int,
    val tag_id: Int
)
