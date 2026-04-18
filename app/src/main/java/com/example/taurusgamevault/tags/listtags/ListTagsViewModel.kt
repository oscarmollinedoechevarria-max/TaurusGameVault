package com.example.taurusgamevault.tags.listtags

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.Tag

class ListTagsViewModel : ViewModel() {
    private val _tags = MutableLiveData<List<Tag>>()
    val tags: LiveData<List<Tag>> = _tags

    private var currentQuery: String = ""
    private var currentSource: LiveData<List<Tag>>? = null

    private val observer = Observer<List<Tag>> { list ->
        _tags.value = if (currentQuery.isBlank()) list
        else list.filter { it.name.contains(currentQuery, ignoreCase = true) }
    }

    fun getTags(context: Context) {
        swapSource(Repository.getTags(context) ?: MutableLiveData())
    }

    fun searchTags(query: String) {
        currentQuery = query
        _tags.value = _tags.value?.let { list ->
            if (query.isBlank()) list
            else list.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    private fun swapSource(newSource: LiveData<List<Tag>>) {
        currentSource?.removeObserver(observer)
        currentSource = newSource
        currentSource?.observeForever(observer)
    }

    override fun onCleared() {
        super.onCleared()
        currentSource?.removeObserver(observer)
    }
}