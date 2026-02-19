package com.example.taurusgamevault.gamedetail

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
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
                val inputStream = context.contentResolver.openInputStream(gameTempData.imageUri)
                inputStream?.use { input ->
                    val tempFile = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
                    tempFile.outputStream().use { output -> input.copyTo(output) }

                    val compressedFile = Compressor.compress(context, tempFile) {
                        quality(80)
                        format(Bitmap.CompressFormat.JPEG)
                    }

                    try {
                        val oldImageUrl = _game?.value?.game_image
                        Repository.updateGameImageAndGetPublicUrl(
                            context,
                            gameId,
                            compressedFile,
                            oldImageUrl
                        )
                    } finally {
                        tempFile.delete()
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
                game_image = _game?.value?.game_image ?: ""
            )

            Repository.updateGame(context, game)

            if (!gameTempData.allScreenshots.isNullOrEmpty()) {

                val oldScreenshots = _screenshots?.value?.map { it.image } ?: emptyList()

                Repository.deleteScreenshotsByGameId(context, gameId)

                gameTempData.allScreenshots.forEachIndexed { index, screenshotUrl ->

                    val publicUrl: String? = if (screenshotUrl.startsWith("content://")) {
                        val uri = Uri.parse(screenshotUrl)
                        val inputStream = context.contentResolver.openInputStream(uri)
                        inputStream?.use { input ->
                            val tempFile = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
                            tempFile.outputStream().use { output -> input.copyTo(output) }

                            val compressedFile = Compressor.compress(context, tempFile) {
                                quality(80)
                                format(Bitmap.CompressFormat.JPEG)
                            }

                            try {
                                val oldUrl = oldScreenshots.getOrNull(index)
                                Repository.updateScreenshotAndGetPublicUrl(compressedFile, oldUrl)
                            } finally {
                                tempFile.delete()
                            }
                        }
                    } else {
                        screenshotUrl
                    }

                    Repository.addScreenshot(
                        context,
                        Screenshot(
                            gameId = gameId,
                            image = publicUrl ?: ""
                        )
                    )
                }
            }

            // Update platforms
            if (!gameTempData.plataforms.isNullOrEmpty()) {
                gameTempData.plataforms.forEach { platformID ->
                    Repository.addGamePlataform(
                        context,
                        PlataformGame(
                            plataform_id = platformID,
                            game_id = gameId
                        )
                    )
                }
            }

            Toast.makeText(context, "Game saved successfully!", Toast.LENGTH_SHORT).show()
        }
    }
}