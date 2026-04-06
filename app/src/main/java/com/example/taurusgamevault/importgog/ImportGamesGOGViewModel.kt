package com.example.taurusgamevault.importgog

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.Model.room.entities.Screenshot
import com.example.taurusgamevault.classes.GameTempData
import com.example.taurusgamevault.enums.Priority
import com.example.taurusgamevault.importgamesigdb.ImportState
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.awaitAll
import org.json.JSONObject
import androidx.lifecycle.viewModelScope
import com.example.taurusgamevault.classes.GOGGame
import com.example.taurusgamevault.classes.toTempGameData
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ImportGamesGOGViewModel : ViewModel() {

    private val _state = MutableLiveData<ImportState>(ImportState.Idle)
    val state: LiveData<ImportState> = _state

    // client for http requests
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()


    // get access token
    fun fetchTokenAndImport(
        context: Context,
        code: String,
        clientId: String,
        clientSecret: String,
        redirectUri: String
    ) {
        viewModelScope.launch {
            _state.value = ImportState.Loading(0, 0, "Authenticating with GOG...")
            try {
                val accessToken = withContext(Dispatchers.IO) {
                    val url = "https://auth.gog.com/token" +
                            "?client_id=$clientId" +
                            "&client_secret=$clientSecret" +
                            "&grant_type=authorization_code" +
                            "&code=$code" +
                            "&redirect_uri=${Uri.encode("https://embed.gog.com/on_login_success?origin=client")}"

                    val request = Request.Builder().url(url).build()
                    httpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw Exception("Token error: ${response.code}")
                        val body = response.body?.string() ?: throw Exception("Empty token response")
                        JSONObject(body).getString("access_token")
                    }
                }
                importGames(context, accessToken)

            } catch (e: Exception) {
                _state.value = ImportState.Error(e.message ?: "Auth error")
            }
        }
    }

    // import games in db
    fun importGames(context: Context, accessToken: String) {
        viewModelScope.launch {
            _state.value = ImportState.Loading(0, 0, "Fetching GOG library...")

            try {
                val games = withContext(Dispatchers.IO) {
                    fetchGOGGames(accessToken)
                }

                if (games.isEmpty()) {
                    _state.value = ImportState.Error("No games found. Check your access token.")
                    return@launch
                }

                _state.value = ImportState.Loading(0, games.size, "")

                games.mapIndexed { index, game ->
                    async {
                        _state.postValue(ImportState.Loading(index + 1, games.size, game.title))
                        saveGame(context, game.toTempGameData())
                    }
                }.awaitAll()

                _state.value = ImportState.Done

            } catch (e: Exception) {
                _state.value = ImportState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // get games form api with access token
    private suspend fun fetchGOGGames(accessToken: String): List<GOGGame> {
        return withContext(Dispatchers.IO) {
            val url = "https://embed.gog.com/account/getFilteredProducts?mediaType=1&page=1"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("GOG API error: ${response.code}")
                val body = response.body?.string() ?: throw Exception("Empty response")
                val json = JSONObject(body)
                val products = json.optJSONArray("products") ?: return@use emptyList()

                (0 until products.length()).map { i ->
                    val obj = products.getJSONObject(i)
                    GOGGame(
                        id    = obj.getLong("id"),
                        title = obj.getString("title"),
                        image = obj.optString("image", "")
                    )
                }
            }
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