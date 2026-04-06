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
            val result = Repository.getAnnotationByGameId(context, gameId)
            _annotation.postValue(result)
        }
    }

    fun saveAnnotation(context: Context, gameId: Long, text: String) {
        viewModelScope.launch {
            val existing = Repository.getAnnotationByGameId(context, gameId)
            
            val newAnnotation = if (existing != null) {
                existing.copy(text = text)
            } else {
                Annotation(
                    text = text,
                    game_id = gameId
                )
            }
            if (existing != null) {
                Repository.updateAnnotation(context, newAnnotation)
            } else {
                Repository.insertAnnotation(context, newAnnotation)
            }
        }
    }
}
