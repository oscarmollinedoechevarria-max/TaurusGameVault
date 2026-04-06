package com.example.taurusgamevault

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.GameList
import com.example.taurusgamevault.Model.room.entities.Tag
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

// SharedViewModel for sharing base data
class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private var _plataforms: LiveData<List<Tag>>? = null
    val plataforms: LiveData<List<Tag>>? get() = _plataforms

    fun getPlataforms(context: Context) {
        _plataforms = Repository.getPlataforms(context)
    }

    private var _lists: LiveData<List<GameList>>? = null
    val lists: LiveData<List<GameList>>? get() = _lists

    fun getListNames(context: Context) {
        _lists = Repository.getLists(context)
    }

}