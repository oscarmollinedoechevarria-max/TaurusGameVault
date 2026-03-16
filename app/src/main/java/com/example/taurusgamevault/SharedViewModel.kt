package com.example.taurusgamevault

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.GameList
import com.example.taurusgamevault.Model.room.entities.Tag
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private var _plataforms: LiveData<List<Tag>>? = null
    val plataforms: LiveData<List<Tag>>? get() = _plataforms

    fun getPlataforms(context: Context) {
        _plataforms = Repository.getPlataforms(context)
    }

    private var _lists: LiveData<List<GameList>>? = null
    val lists: LiveData<List<GameList>>? get() = _lists

    fun getListNames(context: Context){
        _lists = Repository.getLists(context)
    }

//    fun insertGames(context: Context) {
//        viewModelScope.launch {
//            Repository.addGame(
//                context,
//                Game(
//                    name = "Fallout: New vegas",
//                    description = "RPG",
//                    release_date = "2010",
//                    playtime = 75,
//                    personal_rating = 0.0f,
//                    game_state = "0",
//                    start_date = "10/10/10",
//                    end_date = "10/10/10",
//                    priority = 2,
//                    deadline = "10/10/10",
//                    game_image = "https://swsawzjvsxtesdhbsuxn.supabase.co/storage/v1/object/public/filesdatabase/gameImages/test.jpg"
//                )
//            )
//
//            Repository.addGame(
//                context,
//                Game(
//                    name = "The Elder Scrolls III: Morrowind",
//                    description = "RPG",
//                    release_date = "2001",
//                    playtime = 15,
//                    personal_rating = 8.0f,
//                    game_state = "1",
//                    start_date = "00",
//                    end_date = null,
//                    priority = 1,
//                    deadline = null,
//                    game_image = "https://swsawzjvsxtesdhbsuxn.supabase.co/storage/v1/object/public/filesdatabase/gameImages/temp_image.jpg"
//                )
//            )
//        }
//    }

    fun debugAssets(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("DebugAssets", "=== ESTRUCTURA DE ASSETS ===")

            fun listAssetsRecursive(path: String, indent: String = "") {
                try {
                    val list = context.assets.list(path) ?: emptyArray()
                    list.forEach { item ->
                        Log.d("DebugAssets", "$indent$item")
                        listAssetsRecursive(if (path.isEmpty()) item else "$path/$item", "$indent  ")
                    }
                } catch (e: Exception) {
                    // Es un archivo, no un directorio
                }
            }

            listAssetsRecursive("")
            Log.d("DebugAssets", "=== FIN ===")
        }
    }

//    fun uploadAllGamesWithMetadata(context: Context) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                Log.d("UploadGames", "=== INICIO DEL PROCESO ===")
//                val assetManager = context.assets
//
//                // Listar archivos en assets root
//                Log.d("UploadGames", "Listando archivos en assets/")
//                val rootFiles = assetManager.list("") ?: emptyArray()
//                Log.d("UploadGames", "Archivos en root: ${rootFiles.joinToString(", ")}")
//
//                // Intentar leer el JSON general
//                Log.d("UploadGames", "Intentando leer games_data.json")
//                val jsonStream = try {
//                    assetManager.open("games_data.json")
//                } catch (e: Exception) {
//                    Log.e("UploadGames", "ERROR: No se pudo abrir games_data.json: ${e.message}")
//                    e.printStackTrace()
//
//                    // Intentar buscar en subdirectorios
//                    Log.d("UploadGames", "Buscando JSON en game_images/")
//                    try {
//                        assetManager.open("game_images/games_data.json")
//                    } catch (e2: Exception) {
//                        Log.e("UploadGames", "ERROR: Tampoco está en game_images/: ${e2.message}")
//                        withContext(Dispatchers.Main) {
//                            Toast.makeText(context, "ERROR: games_data.json no encontrado en assets/", Toast.LENGTH_LONG).show()
//                        }
//                        return@launch
//                    }
//                }
//
//                Log.d("UploadGames", "✓ JSON encontrado, leyendo contenido...")
//                val jsonString = jsonStream.bufferedReader().use { it.readText() }
//                Log.d("UploadGames", "✓ JSON leído, tamaño: ${jsonString.length} caracteres")
//
//                val gamesArray = try {
//                    JSONArray(jsonString)
//                } catch (e: Exception) {
//                    Log.e("UploadGames", "ERROR: JSON inválido: ${e.message}")
//                    e.printStackTrace()
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "ERROR: JSON mal formado", Toast.LENGTH_LONG).show()
//                    }
//                    return@launch
//                }
//
//                Log.d("UploadGames", "✓ JSON parseado, total de juegos: ${gamesArray.length()}")
//
//                // Listar carpetas de juegos disponibles
//                Log.d("UploadGames", "Listando carpetas en game_images/")
//                val gameImagesFolders = try {
//                    assetManager.list("game_images") ?: emptyArray()
//                } catch (e: Exception) {
//                    Log.e("UploadGames", "ERROR: No se pudo listar game_images/: ${e.message}")
//                    emptyArray()
//                }
//                Log.d("UploadGames", "Carpetas encontradas (${gameImagesFolders.size}): ${gameImagesFolders.joinToString(", ")}")
//
//                var successCount = 0
//                var failCount = 0
//                var skippedCount = 0
//
//                for (i in 0 until gamesArray.length()) {
//                    try {
//                        val gameData = gamesArray.getJSONObject(i)
//                        val gameName = gameData.getString("name")
//
//                        Log.d("UploadGames", "\n--- [${i + 1}/${gamesArray.length()}] Procesando: $gameName ---")
//
//                        // Sanitizar nombre para buscar carpeta
//                        val folderName = gameName.replace(":", "")
//                            .replace("/", "")
//                            .replace("'", "")
//                            .trim()
//
//                        Log.d("UploadGames", "Nombre de carpeta buscado: '$folderName'")
//
//                        val gamePath = "game_images/$folderName"
//
//                        // Verificar si existe la carpeta del juego
//                        val files = try {
//                            val fileList = assetManager.list(gamePath) ?: emptyArray()
//                            Log.d("UploadGames", "Archivos en $gamePath (${fileList.size}): ${fileList.joinToString(", ")}")
//                            fileList
//                        } catch (e: Exception) {
//                            Log.w("UploadGames", "⚠ No se encontró carpeta para: $gameName (buscado: $folderName)")
//                            Log.w("UploadGames", "Error: ${e.message}")
//                            emptyArray()
//                        }
//
//                        if (files.isEmpty()) {
//                            Log.w("UploadGames", "⚠ SKIP: No images found for: $gameName")
//                            skippedCount++
//                            continue
//                        }
//
//                        // Buscar archivos de imágenes
//                        val coverFile = files.find { it.startsWith("cover.") }
//                        val screenshotFiles = files.filter {
//                            it.startsWith("gameplay_") || it.startsWith("artwork.")
//                        }
//
//                        Log.d("UploadGames", "Cover encontrado: ${coverFile ?: "NONE"}")
//                        Log.d("UploadGames", "Screenshots encontrados (${screenshotFiles.size}): ${screenshotFiles.joinToString(", ")}")
//
//                        // Subir cover
//                        Log.d("UploadGames", "Subiendo cover...")
//                        val coverUrl: String? = coverFile?.let { fileName ->
//                            val url = uploadImageFromAssets(context, "$gamePath/$fileName")
//                            if (url != null) {
//                                Log.d("UploadGames", "✓ Cover subido: ${url.take(50)}...")
//                            } else {
//                                Log.e("UploadGames", "✗ Error al subir cover")
//                            }
//                            url
//                        }
//
//                        // Crear el juego con datos del JSON
//                        Log.d("UploadGames", "Creando juego en BD...")
//                        val game = Game(
//                            name = gameName,
//                            description = if (gameData.isNull("description")) null
//                            else gameData.getString("description"),
//                            release_date = if (gameData.isNull("release_date")) null
//                            else gameData.getString("release_date"),
//                            playtime = gameData.optInt("playtime", 0),
//                            personal_rating = if (gameData.isNull("personal_rating")) null
//                            else gameData.getDouble("personal_rating").toFloat(),
//                            game_state = gameData.optString("game_state", "pending"),
//                            start_date = if (gameData.isNull("start_date")) null
//                            else gameData.getString("start_date"),
//                            end_date = if (gameData.isNull("end_date")) null
//                            else gameData.getString("end_date"),
//                            priority = gameData.optInt("priority", 0),
//                            deadline = if (gameData.isNull("deadline")) null
//                            else gameData.getString("deadline"),
//                            game_image = coverUrl
//                        )
//
//                        val gameID = Repository.addGame(context, game)
//                        Log.d("UploadGames", "✓ Juego creado con ID: $gameID")
//
//                        // Subir screenshots
//                        Log.d("UploadGames", "Subiendo ${screenshotFiles.size} screenshots...")
//                        var screenshotCount = 0
//                        screenshotFiles.forEachIndexed { index, fileName ->
//                            Log.d("UploadGames", "  Screenshot ${index + 1}/${screenshotFiles.size}: $fileName")
//                            val screenshotUrl = uploadImageFromAssets(
//                                context,
//                                "$gamePath/$fileName",
//                                isScreenshot = true
//                            )
//
//                            screenshotUrl?.let {
//                                val screenshot = Screenshot(
//                                    gameId = gameID,
//                                    image = it
//                                )
//                                Repository.addScreenshot(context, screenshot)
//                                screenshotCount++
//                                Log.d("UploadGames", "  ✓ Screenshot ${index + 1} subido")
//                            } ?: Log.e("UploadGames", "  ✗ Error al subir screenshot ${index + 1}")
//                        }
//                        Log.d("UploadGames", "✓ Screenshots subidos: $screenshotCount/${screenshotFiles.size}")
//
//                        // Agregar relaciones plataforma-juego
//                        if (gameData.has("platforms")) {
//                            val platformsArray = gameData.getJSONArray("platforms")
//                            Log.d("UploadGames", "Agregando ${platformsArray.length()} plataformas...")
//                            for (j in 0 until platformsArray.length()) {
//                                val platformId = platformsArray.getInt(j).toLong()
//                                val platformGame = PlataformGame(
//                                    plataform_id = platformId,
//                                    game_id = gameID
//                                )
//                                Repository.addGamePlataform(context, platformGame)
//                                Log.d("UploadGames", "  ✓ Plataforma $platformId vinculada")
//                            }
//                        } else {
//                            Log.w("UploadGames", "⚠ No hay plataformas para este juego")
//                        }
//
//                        Log.d("UploadGames", "✓✓✓ $gameName COMPLETADO ✓✓✓")
//                        successCount++
//
//                    } catch (e: Exception) {
//                        Log.e("UploadGames", "✗✗✗ ERROR procesando juego ${i + 1} ✗✗✗")
//                        Log.e("UploadGames", "Error: ${e.message}")
//                        e.printStackTrace()
//                        failCount++
//                    }
//                }
//
//                Log.d("UploadGames", "\n=== RESUMEN FINAL ===")
//                Log.d("UploadGames", "✓ Exitosos: $successCount")
//                Log.d("UploadGames", "✗ Fallidos: $failCount")
//                Log.d("UploadGames", "⊘ Saltados (sin imágenes): $skippedCount")
//                Log.d("UploadGames", "Total procesados: ${successCount + failCount + skippedCount}/${gamesArray.length()}")
//
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(
//                        context,
//                        "Upload complete!\n✓ Success: $successCount\n✗ Failed: $failCount\n⊘ Skipped: $skippedCount",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//
//            } catch (e: Exception) {
//                Log.e("UploadGames", "===== ERROR FATAL =====")
//                Log.e("UploadGames", "Mensaje: ${e.message}")
//                Log.e("UploadGames", "Tipo: ${e.javaClass.simpleName}")
//                e.printStackTrace()
//
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "ERROR FATAL: ${e.message}", Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//    }

    private suspend fun uploadImageFromAssets(
        context: Context,
        assetPath: String,
        isScreenshot: Boolean = false
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("UploadImage", "Subiendo: $assetPath")
                val inputStream = context.assets.open(assetPath)
                inputStream.use { input ->
                    val tempFile = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                    Log.d("UploadImage", "Archivo temporal creado: ${tempFile.length()} bytes")

                    val compressedFile = Compressor.compress(context, tempFile) {
                        quality(80)
                        format(Bitmap.CompressFormat.JPEG)
                    }
                    Log.d("UploadImage", "Archivo comprimido: ${compressedFile.length()} bytes")

                    try {
                        val url = if (isScreenshot) {
                            Repository.uploadScreenshotAndGetPublicUrl(compressedFile)
                        } else {
                            Repository.uploadImageAndGetPublicUrl(compressedFile)
                        }
                        Log.d("UploadImage", "✓ URL obtenida: ${url?.take(50)}...")
                        url
                    } finally {
                        tempFile.delete()
                        if (compressedFile.exists()) {
                            compressedFile.delete()
                        }
                        Log.d("UploadImage", "Archivos temporales eliminados")
                    }
                }
            } catch (e: Exception) {
                Log.e("UploadImage", "Error uploading $assetPath: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }
}