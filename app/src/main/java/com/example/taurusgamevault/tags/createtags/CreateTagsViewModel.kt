package com.example.taurusgamevault.tags.createtags

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.Tag
import kotlinx.coroutines.launch

class CreateTagsViewModel : ViewModel() {
    fun saveTag(context: Context, tag: Tag){
        viewModelScope.launch {
            Repository.addTag(context, tag)
        }
    }
}