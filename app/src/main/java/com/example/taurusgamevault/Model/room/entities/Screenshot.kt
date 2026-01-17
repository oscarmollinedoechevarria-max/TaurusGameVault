package com.example.taurusgamevault.Model.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "screenshot",
    foreignKeys = [
        ForeignKey(
            entity = Game::class,
            parentColumns = ["game_id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Screenshot(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "screenshot_id")
    var screenshot_id: Long = 0L,

    @ColumnInfo(name = "game_id")
    var gameId: Long,

    @ColumnInfo(name = "image")
    var image: String
)