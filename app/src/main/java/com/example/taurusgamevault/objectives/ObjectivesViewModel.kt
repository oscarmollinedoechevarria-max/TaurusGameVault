package com.example.taurusgamevault.objectives

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.Objective
import kotlinx.coroutines.launch

class ObjectivesViewModel : ViewModel() {

    private val _editMode = MutableLiveData<Boolean>(false)
    val editMode: LiveData<Boolean> = _editMode

    fun toggleEditMode() {
        _editMode.value = !(_editMode.value ?: false)
    }

    fun getLiveObjectives(context: Context, gameId: Long): LiveData<List<Objective>> {
        return Repository.getObjectivesByGame(context, gameId)
    }

    fun addObjective(context: Context, gameId: Long, title: String) {
        viewModelScope.launch {
            val objective = Objective(game_id = gameId, title = title)
            Repository.insertObjective(context, objective)
        }
    }

    fun toggleCompleted(context: Context, objective: Objective) {
        viewModelScope.launch {
            val updated = objective.copy(completed = !objective.completed)
            Repository.updateObjective(context, updated)
        }
    }

    fun deleteObjective(context: Context, objective: Objective) {
        viewModelScope.launch {
            Repository.deleteObjective(context, objective)
        }
    }
}
