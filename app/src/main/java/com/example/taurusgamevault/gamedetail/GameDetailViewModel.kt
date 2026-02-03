package com.example.taurusgamevault.gamedetail

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.Model.room.entities.Plataform
import com.example.taurusgamevault.Model.room.entities.PlataformGame
import com.example.taurusgamevault.Model.room.entities.Screenshot
import com.example.taurusgamevault.classes.GameTempData
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class GameDetailViewModel : ViewModel() {
    private var _game: LiveData<Game>? = null
    val game: LiveData<Game>? get() = _game

    private var _screenshots: LiveData<List<Screenshot>>? = null
    val screenshots: LiveData<List<Screenshot>>? get() = _screenshots

    private var _plataforms: LiveData<List<Plataform>>? = null
    val plataforms: LiveData<List<Plataform>>? get() = _plataforms

    private var _allPlataforms: LiveData<List<Plataform>>? = null
    val allPlataforms: LiveData<List<Plataform>>? get() = _allPlataforms

    fun getPlataforms(context: Context) {
        _allPlataforms = Repository.getPlataforms(context)
    }

    fun getGame(context: Context, gameId: Long) {
        _game = Repository.getGame(context, gameId)
    }

    fun getScreenshots(context: Context, gameId: Long) {
        _screenshots = Repository.getScreenshots(context, gameId)
    }

    fun getGamePlataforms(context: Context, gameId: Long) {
        _plataforms = Repository.getGamePlataforms(context, gameId)
    }

    fun saveGame(context: Context, gameTempData: GameTempData, gameId: Long) {
        viewModelScope.launch {
            if (gameTempData.imageUri != null) {
                val gameImage: String? = gameTempData.imageUri?.let { uri ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    inputStream?.use { input ->
                        val tempFile = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }

                        val compressedFile = Compressor.compress(context, tempFile) {
                            quality(80)
                            format(Bitmap.CompressFormat.JPEG)
                        }

                        try {
                            Repository.uploadImageAndGetPublicUrl(compressedFile)
                        } finally {
                            tempFile.delete()
                        }
                    }
                }
            }

            val game = Game(
                game_id = gameId,
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
                game_image = ""
            )

            val gameID = Repository.updateGame(context, game)

            if (gameTempData.screenshots != null) {
                gameTempData.screenshots.forEachIndexed { index, uri ->
                    val screenshot: String? = gameTempData.screenshots[index].let { uri ->
                        val inputStream = context.contentResolver.openInputStream(uri)
                        inputStream?.use { input ->
                            val tempFile = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
                            tempFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                            val compressedFile = Compressor.compress(context, tempFile) {
                                quality(80)
                                format(Bitmap.CompressFormat.JPEG)
                            }

                            try {
                                Repository.uploadScreenshotAndGetPublicUrl(compressedFile)
                            } finally {
                                tempFile.delete()
                            }
                        }
                    }


                    val temp = Screenshot(
                        gameId = gameId,
                        image = screenshot ?: ""
                    )

                    Repository.addScreenshot(context, temp)
                }
            }

            if (plataforms != null) {
                gameTempData.plataforms?.forEach { platformID ->
                    val temp = PlataformGame(
                        plataform_id = platformID,
                        game_id = gameId
                    )

                    Repository.addGamePlataform(context, temp)
                }
            }

            Toast.makeText(context, "Game saved successfully!", Toast.LENGTH_SHORT).show()
        }
    }
}