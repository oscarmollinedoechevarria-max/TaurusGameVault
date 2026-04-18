package com.example.taurusgamevault.goldenPick

import android.app.AlertDialog
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asFlow
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.classes.GameStats
import com.example.taurusgamevault.SharedViewModel
import com.example.taurusgamevault.enums.GameStates
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GoldenPickOverlayViewModel : ViewModel() {
    private var _vaultStats: LiveData<GameStats>? = null
    val vaultStats: LiveData<GameStats>? get() = _vaultStats

    private val _gamePickedEvent = MutableLiveData<Pair<Long, String>?>()
    val gamePickedEvent: LiveData<Pair<Long, String>?> = _gamePickedEvent

    private val _errorNoGames = MutableLiveData<Boolean>()
    val errorNoGames: LiveData<Boolean> = _errorNoGames

    fun getVaultStats(context: Context) {
            _vaultStats = Repository.getVaultStats(context)
    }

    fun pickRandomGame(context: Context) {
        viewModelScope.launch {
            val games = Repository.getGames(context)?.asFlow()?.first()
            val pendingGames = games?.filter { it.game_state == "Pending" }

            if (!pendingGames.isNullOrEmpty()) {
                val randomGame = pendingGames.random()
                _gamePickedEvent.value = Pair(randomGame.game_id, randomGame.name)
            } else {
                _errorNoGames.value = true
            }
        }
    }
}