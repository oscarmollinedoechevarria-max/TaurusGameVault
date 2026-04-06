package com.example.taurusgamevault.goldenPick

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.classes.GameStats

class GoldenPickOverlayViewModel : ViewModel() {
    private var _vaultStats: LiveData<GameStats>? = null
    val vaultStats: LiveData<GameStats>? get() = _vaultStats

    fun getVaultStats(context: Context) {
            _vaultStats = Repository.getVaultStats(context)
    }

}