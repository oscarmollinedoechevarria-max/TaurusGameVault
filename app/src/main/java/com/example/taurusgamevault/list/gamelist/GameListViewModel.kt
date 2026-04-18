package com.example.taurusgamevault.list.gamelist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.GameList

class GameListViewModel : ViewModel() {
    private val _list = MutableLiveData<List<GameList>>()
    val list: LiveData<List<GameList>> = _list

    private var currentQuery: String = ""
    private var currentSource: LiveData<List<GameList>>? = null

    private val observer = Observer<List<GameList>> { list ->
        _list.value = if (currentQuery.isBlank()) list
        else list.filter { it.name.contains(currentQuery, ignoreCase = true) }
    }

    fun getList(context: Context) {
        swapSource(Repository.getLists(context) ?: MutableLiveData())
    }

    fun searchList(query: String) {
        currentQuery = query
        _list.value = _list.value?.let { list ->
            if (query.isBlank()) list
            else list.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    private fun swapSource(newSource: LiveData<List<GameList>>) {
        currentSource?.removeObserver(observer)
        currentSource = newSource
        currentSource?.observeForever(observer)
    }

    override fun onCleared() {
        super.onCleared()
        currentSource?.removeObserver(observer)
    }
}