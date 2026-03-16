package com.example.taurusgamevault.Model.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "objective",
    foreignKeys = [
        ForeignKey(
            entity = Game::class,
            parentColumns = ["game_id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Objective(
    @PrimaryKey(autoGenerate = true) val objective_id: Long = 0L,
    val game_id: Long,
    val title: String,
    val completed: Boolean = false
)
