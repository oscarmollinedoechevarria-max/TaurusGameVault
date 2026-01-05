package com.example.taurusgamevault.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "plataform_game",
    primaryKeys = ["game_id", "plataform_id"],
    foreignKeys = [
        ForeignKey(entity = Game::class,
            parentColumns = ["game_id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Plataform::class,
            parentColumns = ["plataform_id"],
            childColumns = ["plataform_id"],
            onDelete = ForeignKey.CASCADE)
    ]
)
data class Plataform_game (
    @ColumnInfo(name = "plataform_id")
    var plataform_id: Long,

    @ColumnInfo(name = "game_id")
    var game_id: Long,
)
