package com.example.taurusgamevault.creategame

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.Model.room.entities.TagGame
import com.example.taurusgamevault.Model.room.entities.Screenshot
import com.example.taurusgamevault.Model.room.entities.Tag
import com.example.taurusgamevault.classes.GameTempData
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class CreateGameViewModel : ViewModel() {
    private var _tags: LiveData<List<Tag>>? = null
    val tags: LiveData<List<Tag>>? get() = _tags

    fun getTags(context: Context) {
        _tags = Repository.getTags(context)
    }

    fun saveGame(context: Context, gameTempData: GameTempData, tagsSelected: List<Long>?) {
        viewModelScope.launch {
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

            val gameID = Repository.addGame(context, game)

            gameTempData.screenshots?.forEachIndexed { index, uri ->
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
                    gameId = gameID,
                    image = screenshot ?: ""
                )

                Repository.addScreenshot(context, temp)
            }

            // Save platforms
            gameTempData.plataforms?.forEach { platformID ->
                val temp = TagGame(
                    tag_id = platformID,
                    game_id = gameID
                )

                Repository.addTagGame(context, temp)
            }

            // Save tags
            tagsSelected?.forEach { tagId ->
                val temp = TagGame(
                    tag_id = tagId,
                    game_id = gameID
                )
                Repository.addTagGame(context, temp)
            }

            Toast.makeText(context, "Game saved successfully!", Toast.LENGTH_SHORT).show()
        }
    }
}
