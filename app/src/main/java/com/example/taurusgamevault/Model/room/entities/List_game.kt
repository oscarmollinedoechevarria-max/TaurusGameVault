package com.example.taurusgamevault.Model.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "list_game",
    primaryKeys = ["game_id", "list_id"],
    foreignKeys = [
        ForeignKey(entity = Game::class,
            parentColumns = ["game_id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = GameList::class,
            parentColumns = ["list_id"],
            childColumns = ["list_id"],
            onDelete = ForeignKey.CASCADE)
    ]
)
data class List_game(
    @ColumnInfo(name = "list_id")
    var list_id: Long,

    @ColumnInfo(name = "game_id")
    var game_id: Long,
)