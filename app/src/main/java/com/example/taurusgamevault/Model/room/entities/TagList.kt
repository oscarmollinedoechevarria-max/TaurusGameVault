package com.example.taurusgamevault.Model.room.entities

import androidx.room.Entity

@Entity(
    tableName = "tag_list",
    primaryKeys = ["game_id", "tag_id"]
)
data class TagList(
    val game_id: Int,
    val tag_id: Int
)
