package com.example.taurusgamevault.annotations

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.Annotation
import kotlinx.coroutines.launch

class AnnotationsViewModel : ViewModel() {

    private val _annotation = MutableLiveData<Annotation?>()
    val annotation: LiveData<Annotation?> = _annotation

    fun loadAnnotation(context: Context, gameId: Long) {
        viewModelScope.launch {
            val db = Repository.initializeDB(context)
            val result = db.annotationDao().getAnnotationByGameId(gameId)
            _annotation.postValue(result)
        }
    }

    fun saveAnnotation(context: Context, gameId: Long, text: String) {
        viewModelScope.launch {
            val db = Repository.initializeDB(context)
            val existing = db.annotationDao().getAnnotationByGameId(gameId)
            
            val newAnnotation = if (existing != null) {
                existing.copy(text = text)
            } else {
                Annotation(
                    text = text,
                    game_id = gameId
                )
            }
            db.annotationDao().insertAnnotation(newAnnotation)
        }
    }
}
