package com.example.taurusgamevault.database.entities

import android.accessibilityservice.GestureDescription
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game")
data class Game(
    @PrimaryKey(autoGenerate = true)
    var game_id: Long = 0L,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "description")
    var description: String?,

    @ColumnInfo(name = "release_date")
    var release_date: String,

    @ColumnInfo(name = "playtime")
    var playtime : Long?,

    @ColumnInfo(name = "personal_rating")
    var personal_rating: Int?,

    @ColumnInfo(name = "game_state")
    var game_state : String?,

    @ColumnInfo(name = "start_date")
    var start_date: Long?,

    @ColumnInfo(name = "end_date")
    var end_date: Long?,

    @ColumnInfo(name = "priority")
    var priority: Int?,

    @ColumnInfo(name = "deadline")
    var deadline: Long?,
)
