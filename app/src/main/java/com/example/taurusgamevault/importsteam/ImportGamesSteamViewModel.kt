package com.example.taurusgamevault.importsteam

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.classes.GameTempData
import com.example.taurusgamevault.importgamesigdb.ImportState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.coroutineScope
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.Model.room.entities.Screenshot
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import java.io.File
import java.util.UUID
import com.example.taurusgamevault.classes.SteamGame
import com.example.taurusgamevault.classes.toTempGameData

class ImportGamesSteamViewModel : ViewModel() {

    private val _state = MutableLiveData<ImportState>(ImportState.Idle)
    val state: LiveData<ImportState> = _state

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // import games from steam api
    fun importGames(context: Context, steamId: String, apiKey: String) {
        viewModelScope.launch {
            _state.value = ImportState.Loading(0, 0, "Fetching Steam library...")

            try {
                val games = withContext(Dispatchers.IO) {
                    fetchSteamGames(steamId, apiKey)
                }

                if (games.isEmpty()) {
                    _state.value = ImportState.Error("No games found. Check your Steam ID and API Key.")
                    return@launch
                }

                _state.value = ImportState.Loading(0, games.size, "")

                games.mapIndexed { index, game ->
                    async {
                        _state.postValue(ImportState.Loading(index + 1, games.size, game.name))
                        val tempData = game.toTempGameData()
                        saveGame(context, tempData)
                    }
                }.awaitAll()

                _state.value = ImportState.Done

            } catch (e: Exception) {
                _state.value = ImportState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // get games from steam api
    private suspend fun fetchSteamGames(steamId: String, apiKey: String): List<SteamGame> {
        return withContext(Dispatchers.IO) {
            val url = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/" +
                    "?key=$apiKey&steamid=$steamId&include_appinfo=true&include_played_free_games=true"

            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("Steam API error: ${response.code}")
                val body = response.body?.string() ?: throw Exception("Empty response")
                val json = JSONObject(body)
                val gamesArray = json.getJSONObject("response").optJSONArray("games")
                    ?: return@use emptyList()

                (0 until gamesArray.length()).map { i ->
                    val obj = gamesArray.getJSONObject(i)
                    SteamGame(
                        appId = obj.getInt("appid"),
                        name = obj.getString("name"),
                        playtimeMinutes = obj.optInt("playtime_forever", 0)
                    )
                }
            }
        }
    }

    // save game to database
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

        Toast.makeText(context, "${gameTempData.name} saved!", Toast.LENGTH_SHORT).show()
    }

    private suspend fun downloadAndUploadImage(context: Context, url: String): String? {
        return try {
            val tempFile = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
            withContext(Dispatchers.IO) {
                val request = Request.Builder().url(url).build()
                httpClient.newCall(request).execute().use { response ->
                    response.body?.byteStream()?.use { input ->
                        tempFile.outputStream().use { output -> input.copyTo(output) }
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
    }
}