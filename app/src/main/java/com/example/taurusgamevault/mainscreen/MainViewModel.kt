package com.example.taurusgamevault.mainscreen

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.Model.room.entities.Tag

class MainViewModel : ViewModel() {
    private val _games = MutableLiveData<List<Game>>()
    val games: LiveData<List<Game>> = _games

    private var currentQuery: String = ""
    private var selectedTagIds: List<Long> = emptyList()

    private var currentSource: LiveData<List<Game>>? = null

    // observer for games list
    private val observer = Observer<List<Game>> { list ->
        _games.value = if (currentQuery.isBlank()) list
        else list.filter { it.name.contains(currentQuery, ignoreCase = true) }
    }

    fun getGames(context: Context) {
        swapSource(Repository.getGames(context) ?: MutableLiveData())
    }

    fun searchGames(query: String) {
        currentQuery = query
        _games.value = _games.value?.let { list ->
            if (query.isBlank()) list
            else list.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    fun filterByTags(context: Context, tags: Set<Tag>) {
        selectedTagIds = tags.map { it.tag_id }
        swapSource(Repository.getGamesByTags(context, selectedTagIds))
    }

    // change games source and observe changes
    private fun swapSource(newSource: LiveData<List<Game>>) {
        currentSource?.removeObserver(observer)
        currentSource = newSource
        currentSource?.observeForever(observer)
    }

    override fun onCleared() {
        super.onCleared()
        currentSource?.removeObserver(observer)
    }
}