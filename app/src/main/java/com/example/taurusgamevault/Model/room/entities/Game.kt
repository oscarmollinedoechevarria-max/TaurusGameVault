package com.example.taurusgamevault.Model.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.taurusgamevault.classes.SimplifiedGame

//TODO: FIX CAMPS WITH SQLITE (fixed?)
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
    val personal_rating: Float? = 0.0f,

    @ColumnInfo(name = "game_state")
    val game_state: String? = null,

    @ColumnInfo(name = "start_date")
    val start_date: String? = null,

    @ColumnInfo(name = "end_date")
    val end_date: String? = null,

    @ColumnInfo(name = "priority")
    val priority: String? = "",

    @ColumnInfo(name = "deadline")
    val deadline: String? = null,

    @ColumnInfo(name = "game_image")
    val game_image: String? = null
)

// Extension function to convert Game to SimplifiedGame
fun Game.toSimplifiedGame(): SimplifiedGame {
    return SimplifiedGame(
        gameId = this.game_id,
        name = this.name,
        description = this.description ?: "",
        releaseDate = this.release_date ?: "",
        image = this.game_image ?: ""
    )
}