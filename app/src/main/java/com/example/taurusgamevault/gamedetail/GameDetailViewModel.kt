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
import com.example.taurusgamevault.Model.room.entities.Tag
import com.example.taurusgamevault.Model.room.entities.TagGame
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

    private var _gameTags: LiveData<List<Tag>>? = null
    val gameTags: LiveData<List<Tag>>? get() = _gameTags

    private var _allTags: LiveData<List<Tag>>? = null
    val allTags: LiveData<List<Tag>>? get() = _allTags

    fun getTags(context: Context) {
        _allTags = Repository.getTags(context)
    }

    fun getGame(context: Context, gameId: Long) {
        _game = Repository.getGame(context, gameId)
    }

    fun getScreenshots(context: Context, gameId: Long) {
        _screenshots = Repository.getScreenshots(context, gameId)
    }

    fun getGameTags(context: Context, gameId: Long) {
        _gameTags = Repository.getGameTags(context, gameId)
    }

    fun saveGame(context: Context, gameTempData: GameTempData, gameId: Long, onComplete: () -> Unit) {
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

            // Update platforms and Tags
            if (gameTempData.plataforms != null) {
                Repository.deleteTagsByGameId(context, gameId)
                gameTempData.plataforms.forEach { tagId ->
                    Repository.addTagGame(
                        context,
                        TagGame(
                            tag_id = tagId,
                            game_id = gameId
                        )
                    )
                }
            }

            Toast.makeText(context, "Game saved successfully!", Toast.LENGTH_SHORT).show()
            onComplete()
        }
    }
}
