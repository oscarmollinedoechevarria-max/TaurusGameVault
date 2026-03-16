package com.example.taurusgamevault.importgamesigdb

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.retrofit.igdb.IgdbGame
import com.example.taurusgamevault.Model.retrofit.igdb.IgdbRetrofit
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.Model.room.entities.TagGame
import com.example.taurusgamevault.Model.room.entities.Screenshot
import com.example.taurusgamevault.classes.GameTempData
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import com.example.taurusgamevault.Model.retrofit.igdb.igdbImageUrl
import com.example.taurusgamevault.enums.Priority
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class ImportGamesNoAccountViewModel(
    private val igdbRetrofit: IgdbRetrofit
) : ViewModel() {

    private val _state = MutableLiveData<ImportState>(ImportState.Idle)
    val state: LiveData<ImportState> = _state

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun importGames(context: Context, rawNames: String) {
        val names = rawNames.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        viewModelScope.launch {
            _state.value = ImportState.Loading(0, names.size, "")

            val results = withContext(Dispatchers.IO) {
                Repository.importGames(igdbRetrofit.service, names) { current, total, gameName ->
                    _state.postValue(ImportState.Loading(current, total, gameName))
                }
            }

            results.filterNotNull()
                .map { igdbGame ->
                    async { saveGame(context, igdbGame.toTempGameData()) }
                }.awaitAll()

            _state.value = ImportState.Done
        }
    }

    suspend fun saveGame(context: Context, gameTempData: GameTempData) {

        val screenshots = gameTempData.screenshots?.take(3)

        val (gameImage, uploadedScreenshots) = coroutineScope {
            val imageDeferred = async {
                gameTempData.imageUri?.let { downloadAndUploadImage(context, it.toString()) }
            }
            val screenshotsDeferred = screenshots?.map { url ->
                async { downloadAndUploadImage(context, url.toString()) }
            }
            imageDeferred.await() to screenshotsDeferred?.awaitAll()
        }

        val game = Game(
            name            = gameTempData.name,
            description     = gameTempData.description,
            release_date    = gameTempData.releaseDate,
            playtime        = gameTempData.playtime,
            personal_rating = gameTempData.personalRating,
            game_state      = gameTempData.gameState,
            start_date      = gameTempData.startDate,
            end_date        = gameTempData.endDate,
            priority        = gameTempData.priority,
            deadline        = gameTempData.deadline,
            game_image      = gameImage
        )

        val gameID = Repository.addGame(context, game)

        uploadedScreenshots?.filterNotNull()?.forEach { uploaded ->
            Repository.addScreenshot(context, Screenshot(gameId = gameID, image = uploaded))
        }

        gameTempData.plataforms?.forEach { platformID ->
            Repository.addTagGame(
                context,
                TagGame(tag_id = platformID, game_id = gameID)
            )
        }

        Toast.makeText(context, "Game saved successfully!", Toast.LENGTH_SHORT).show()
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private suspend fun downloadAndUploadImage(context: Context, url: String): String? {
        return try {
            val tempFile = File(context.cacheDir, "${UUID.randomUUID()}.jpg")

            withContext(Dispatchers.IO) {
                val request = Request.Builder().url(url).build()
                httpClient.newCall(request).execute().use { response ->
                    response.body?.byteStream()?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }

            val compressedFile = Compressor.compress(context, tempFile) {
                quality(60)
                format(Bitmap.CompressFormat.JPEG)
            }

            try {
                Repository.uploadImageAndGetPublicUrl(compressedFile)
            } finally {
                tempFile.delete()
            }

        } catch (e: Exception) {
            Log.e("saveGame", "Error downloading image from $url: ${e.message}")
            null
        }
    }}

sealed class ImportState {
    object Idle : ImportState()
    data class Loading(val current: Int, val total: Int, val currentGame: String) : ImportState()
    object Done : ImportState()
    data class Error(val message: String) : ImportState()
}

fun IgdbGame.toTempGameData() = GameTempData(
    imageUri      = cover?.image_id?.let { Uri.parse(igdbImageUrl(it)) },
    name          = name.orEmpty(),
    description   = summary.orEmpty(),
    releaseDate   = first_release_date?.let {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(java.sql.Date(it * 1000))
    },
    screenshots   = screenshots?.map { Uri.parse(igdbImageUrl(it.image_id ?: "", "screenshot_big")) },
    playtime      = 0,
    personalRating = 0f,
    gameState     = null,
    startDate     = null,
    endDate       = null,
    deadline      = null,
    priority      = Priority.BACK_LOG.text,
    allScreenshots = null,
    plataforms    = null
)
