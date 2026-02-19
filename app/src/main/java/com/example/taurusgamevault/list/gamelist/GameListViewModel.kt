package com.example.taurusgamevault.list.gamelist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.GameList

class GameListViewModel : ViewModel() {
    private var _list: LiveData<List<GameList>>? = null
    val list: LiveData<List<GameList>>? get() = _list

    fun getList(context: Context) {
        _list = Repository.getLists(context)
    }

}