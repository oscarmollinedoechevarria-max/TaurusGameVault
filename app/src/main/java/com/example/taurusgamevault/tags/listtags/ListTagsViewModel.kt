package com.example.taurusgamevault.tags.listtags

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.Tag

class ListTagsViewModel : ViewModel() {
    private var _tags: LiveData<List<Tag>>? = null
    val tags: LiveData<List<Tag>>? get() = _tags

    fun getTags(context: Context) {
        _tags = Repository.getTags(context)
    }
}