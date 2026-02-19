package com.example.taurusgamevault.classes

import android.os.Parcelable
import com.example.taurusgamevault.Model.room.entities.Game
import kotlinx.parcelize.Parcelize

@Parcelize
data class SimplifiedGame (
    val gameId: Long,
    val name: String,
    val description: String,
    val releaseDate: String,
    val image: String
) : Parcelable

fun SimplifiedGame.toGame(): Game {
    return  Game (
        this.gameId,
        this.name,
        this.description,
        this.releaseDate,
        game_image = this.image
    )
}
