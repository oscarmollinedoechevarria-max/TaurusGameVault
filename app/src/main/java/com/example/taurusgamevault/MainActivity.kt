package com.example.taurusgamevault

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[SharedViewModel::class.java]

//        viewModel.insertGames(applicationContext)

//        viewModel.debugAssets(applicationContext)
//
//        viewModel.uploadAllGamesWithMetadata(applicationContext)

    }
}