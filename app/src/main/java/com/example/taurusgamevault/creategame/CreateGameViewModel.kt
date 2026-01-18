package com.example.taurusgamevault.creategame

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.classes.GameTempData
import kotlinx.coroutines.launch
import java.io.File

//TODO: FIX GAME TYPES FOR CREATE, SCREENSHOTS ADD DATABASE AND SUPABASE
//TODO: FIX GAME ROUTES BY UUID IN THE SUPABASE ADD
//TODO: FUTURE: IMPLEMENT FIREBASE ADD BACKUP MAYBE ACCOUNT FOR EACH USER
//TODO: FIX ERROR AND CHARGING IMAGES FOR GAME CARDS
//TODO: ADD FUNCTION FOR 2 IMAGES FOR GAME: CARD VERSION AND BIG VERSION FOR DETAIL GAME VIEW
//TODO: ADD SUPERIRO BUTTON INSIDE DRAWER FOR CREATE GAME TOO
//TODO: REVISE WHY NOT DOING 2 COLUMN GRID

class CreateGameViewModel : ViewModel() {

    fun saveGame(context: Context, gameTempData: GameTempData) {
        viewModelScope.launch {
            val gameImage: String? = gameTempData.imageUri?.let { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.use { input ->
                    val tempFile = File(context.cacheDir, "temp_image.jpg")
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                    Repository.uploadImageAndGetPublicUrl(tempFile)
                }
            }

            val game = Game(
                name = gameTempData.name,
                description = gameTempData.description,
                release_date = gameTempData.releaseDate,
                playtime = gameTempData.playtime,
                personal_rating = gameTempData.personalRating,
                game_state = gameTempData.gameState,
                start_date = gameTempData.startDate,
                end_date = gameTempData.endDate,
                priority = gameTempData.priority,
                deadline = gameTempData.deadline,
                game_image = gameImage
            )

            Repository.addGame(context, game)

            Toast.makeText(context, "Game saved successfully!", Toast.LENGTH_SHORT).show()
        }
    }
}