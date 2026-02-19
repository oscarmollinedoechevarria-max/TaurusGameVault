package com.example.taurusgamevault.gamepicker

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.Game

class GamePickerViewModel : ViewModel() {
    private var _games: LiveData<List<Game>>? = null
    val games: LiveData<List<Game>>? get() = _games

    fun getGames(context: Context) {
        _games = Repository.getGames(context)
    }
}