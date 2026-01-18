package com.example.taurusgamevault.Model.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//TODO: FIX CAMPS WITH SQLITE
@Entity(tableName = "game")
data class Game(
    @PrimaryKey(autoGenerate = true)
    val game_id: Long = 0L,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "release_date")
    val release_date: String? = null,

    @ColumnInfo(name = "playtime")
    val playtime: Int? = 0,

    @ColumnInfo(name = "personal_rating")
    val personal_rating: Double? = 0.0,

    @ColumnInfo(name = "game_state")
    val game_state: String? = null,

    @ColumnInfo(name = "start_date")
    val start_date: String? = null,

    @ColumnInfo(name = "end_date")
    val end_date: String? = null,

    @ColumnInfo(name = "priority")
    val priority: Int? = 0,

    @ColumnInfo(name = "deadline")
    val deadline: String? = null,

    @ColumnInfo(name = "game_image")
    val game_image: String? = null
)
