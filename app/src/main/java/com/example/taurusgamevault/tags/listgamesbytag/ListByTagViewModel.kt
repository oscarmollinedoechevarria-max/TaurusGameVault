package com.example.taurusgamevault.tags.listgamesbytag

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.Game

class ListByTagViewModel : ViewModel() {
    private var _games: LiveData<List<Game>>? = null
    val games: LiveData<List<Game>>? get() = _games

    fun getGamesByTag(context: Context, tagId: Long) {
        _games = Repository.initializeDB(context).gameDAO().getGamesByTag(tagId)
    }
}
