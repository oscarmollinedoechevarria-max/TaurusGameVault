package com.example.taurusgamevault

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.Game
import kotlinx.coroutines.launch

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    fun insertGames(context: Context) {
        viewModelScope.launch {
            Repository.addGame(
                context,
                Game(
                    name = "Fallout: New vegas",
                    description = "RPG",
                    release_date = "2010",
                    playtime = 75,
                    personal_rating = 0.0,
                    game_state = "0",
                    start_date = null,
                    end_date = null,
                    priority = 2,
                    deadline = null,
                    game_image = "https://swsawzjvsxtesdhbsuxn.supabase.co/storage/v1/object/public/filesdatabase/gameImages/test.jpg"
                )
            )

            Repository.addGame(
                context,
                Game(
                    name = "Room Saga",
                    description = "RPG based on Room",
                    release_date = "2021",
                    playtime = 15,
                    personal_rating = 8.0,
                    game_state = "1",
                    start_date = "00",
                    end_date = null,
                    priority = 1,
                    deadline = null,
                    game_image = null
                )
            )
        }
    }

}