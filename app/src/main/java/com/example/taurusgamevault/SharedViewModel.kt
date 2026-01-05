package com.example.taurusgamevault

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taurusgamevault.database.Repository
import com.example.taurusgamevault.database.entities.Game
import com.example.taurusgamevault.database.entities.Plataform
import kotlinx.coroutines.launch

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    fun insertGames(context: Context) {
        viewModelScope.launch {
            Repository.addGame(
                context,
                Game(
                    name = "The Legend of Kotlin",
                    description = "Juego de aventuras",
                    release_date = "2023",
                    playtime = 0,
                    personal_rating = 0,
                    game_state = "0",
                    start_date = null,
                    end_date = null,
                    priority = 2,
                    deadline = null
                )
            )

            Repository.addGame(
                context,
                Game(
                    name = "Room Saga",
                    description = "RPG basado en Room",
                    release_date = "2021",
                    playtime = 15,
                    personal_rating = 8,
                    game_state = "1",
                    start_date = System.currentTimeMillis(),
                    end_date = null,
                    priority = 1,
                    deadline = null
                )
            )
        }
    }

}