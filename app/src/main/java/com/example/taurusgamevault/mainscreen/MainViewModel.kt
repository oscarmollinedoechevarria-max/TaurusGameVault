package com.example.taurusgamevault.mainscreen

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.taurusgamevault.database.Repository
import com.example.taurusgamevault.database.entities.Game

class MainViewModel : ViewModel() {
    private var _games: LiveData<List<Game>>? = null
    val games: LiveData<List<Game>>? get() = _games

    fun getGames(context: Context) {
        _games = Repository.getGames(context)
    }
}