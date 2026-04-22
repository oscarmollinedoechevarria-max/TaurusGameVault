package com.example.taurusgamevault.Model.Repository

import android.content.Context
import android.net.http.HttpException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import com.example.taurusgamevault.Model.retrofit.igdb.IgdbApiService
import com.example.taurusgamevault.Model.retrofit.igdb.IgdbGame
import com.example.taurusgamevault.Model.room.DataBase
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.Model.room.entities.Annotation
import com.example.taurusgamevault.Model.room.entities.GameList
import com.example.taurusgamevault.Model.room.entities.List_game
import com.example.taurusgamevault.Model.room.entities.Objective
import com.example.taurusgamevault.Model.room.entities.Screenshot
import com.example.taurusgamevault.Model.room.entities.Tag
import com.example.taurusgamevault.Model.room.entities.TagGame
import com.example.taurusgamevault.Model.supabase.SupabaseClientManager
import com.example.taurusgamevault.classes.GameStats
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class Repository {
    companion object {
        fun initializeDB(context: Context): DataBase {
            return DataBase.Companion.getDatabase(context)
        }

        // TODO: Game CRUD
        fun getGames(context: Context): LiveData<List<Game>>? {
            val db = initializeDB(context)

            return db.gameDAO().getGames()
        }

        fun getGame(context: Context, gameID: Long): LiveData<Game>? {
            val db = initializeDB(context)

            return db.gameDAO().getGame(gameID)
        }

        suspend fun addGame(context: Context, game: Game): Long {
            val db = initializeDB(context)

            return db.gameDAO().addGame(game)
        }

        // searches the games in the igdb api via query
        @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
        suspend fun searchGame(api: IgdbApiService, name: String): IgdbGame? {
            if (name.isBlank()) return null

            return try {
                // IGDB by relevance
                val query = """
                    search "${name.replace("\"", "")}";
                    fields name, summary, rating, first_release_date, 
                           cover.image_id, platforms.name, screenshots.image_id;
                    where cover != null & category = (0,8,9);
                    limit 20;
                """.trimIndent()

                val body = query.toRequestBody("text/plain".toMediaType())
                val resultsQuery = api.searchGames(body)

                if (resultsQuery.isEmpty()) return null

                // name similarity, release date and rating bonus
                resultsQuery
                    .filter { it.cover != null }
                    .maxByOrNull { game ->
                        val nameSimilarity = nameSimilarityScore(name, game.name.orEmpty())
                        val hasDate = if (game.first_release_date != null) 0.2 else 0.0
                        val ratingBonus = ((game.rating ?: 0.0) / 100.0) * 0.1
                        nameSimilarity + hasDate + ratingBonus
                    }

            } catch (e: HttpException) {
                Log.e("IGDB", "HTTP ${e.cause} searching '$name': ${e.message}")
                null
            } catch (e: IOException) {
                Log.e("IGDB", "Network error searching '$name': ${e.message}")
                null
            } catch (e: Exception) {
                Log.e("IGDB", "Unknown error searching '$name': ${e::class.simpleName} - ${e.message}")
                null
            }
        }

        // similarity in names
        private fun nameSimilarityScore(query: String, candidate: String): Double {
            val normalize = { s: String ->
                s.lowercase()
                    .replace(Regex("[^a-z0-9 ]"), "")
                    .split(" ")
                    .filter { it.isNotBlank() && it !in setOf("the", "a", "an", "of", "and") }
                    .toSet()
            }
            val queryTokens = normalize(query)
            val candidateTokens = normalize(candidate)
            if (queryTokens.isEmpty()) return 0.0
            val intersection = queryTokens.intersect(candidateTokens).size
            val union = queryTokens.union(candidateTokens).size
            return intersection.toDouble() / union.toDouble()
        }

        // queries the games in the api in batches and imports them to the database
        @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
        suspend fun importGames(
            api: IgdbApiService,
            names: List<String>,
            onProgress: (current: Int, total: Int, gameName: String) -> Unit
        ): List<IgdbGame?> {
            val total = names.size
            val counter = java.util.concurrent.atomic.AtomicInteger(0)

            return names.chunked(4).flatMap { batch ->
                coroutineScope {
                    batch.map { name ->
                        async(Dispatchers.IO) {
                            val current = counter.incrementAndGet()

                            onProgress(current, total, name)

                            val result = searchGame(api, name)

                            result
                        }
                    }.awaitAll()
                }
            }
        }

        suspend fun updateGame(context: Context, game: Game) {
            val db = initializeDB(context)

            return db.gameDAO().updateGame(
                id = game.game_id,
                name = game.name,
                description = game.description,
                releaseDate = game.release_date,
                playtime = game.playtime,
                personalRating = game.personal_rating,
                gameState = game.game_state,
                startDate = game.start_date,
                endDate = game.end_date,
                priority = game.priority,
                deadline = game.deadline
            )
        }

        suspend fun deleteGame(context: Context, game: Game) {
            val db = initializeDB(context)

            return db.gameDAO().deleteGame(game)
        }

        // queries for upload and update the game image in the supabase
        suspend fun uploadImageAndGetPublicUrl(localFile: File): String? {
            val storage = SupabaseClientManager.supabase.storage.from("filesdatabase")

            val pathInBucket = "gameImages/${localFile.name}"

            val result = storage.upload(
                path = pathInBucket,
                file = localFile
            )

            if (result.isEmpty()) {
                return null
            }

            return storage.publicUrl(pathInBucket)
        }

        suspend fun uploadScreenshotAndGetPublicUrl(localFile: File): String? {
            val storage = SupabaseClientManager.supabase.storage.from("filesdatabase")

            val pathInBucket = "gameScreenshot/${localFile.name}"

            val result = storage.upload(
                path = pathInBucket,
                file = localFile
            )

            if (result.isEmpty()) {
                return null
            }

            return storage.publicUrl(pathInBucket)
        }

        suspend fun uploadListImageAndGetPublicUrl(localFile: File): String? {
            val storage = SupabaseClientManager.supabase.storage.from("filesdatabase")

            val pathInBucket = "listImages/${localFile.name}"

            val result = storage.upload(
                path = pathInBucket,
                file = localFile
            )

            if (result.isEmpty()) {
                return null
            }

            return storage.publicUrl(pathInBucket)
        }

        suspend fun uploadTagImageAndGetPublicUrl(localFile: File): String? {
            val storage = SupabaseClientManager.supabase.storage.from("filesdatabase")

            val pathInBucket = "tagsImages/${localFile.name}"

            val result = storage.upload(
                path = pathInBucket,
                file = localFile
            )

            if (result.isEmpty()) {
                return null
            }

            return storage.publicUrl(pathInBucket)
        }

        suspend fun updateTagImageAndGetPublicUrl(
            context: Context,
            tagId: Long,
            localFile: File,
            oldImageUrl: String?
        ): String? {
            val bucket = SupabaseClientManager.supabase.storage.from("filesdatabase")
            val byteArray = localFile.readBytes()
            val newPath = "tagsImages/${localFile.name}"

            return try {
                if (!oldImageUrl.isNullOrBlank() && oldImageUrl.contains("filesdatabase/")) {
                    val oldPath = oldImageUrl.substringAfter("filesdatabase/")
                    try {
                        bucket.delete(listOf(oldPath))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                bucket.upload(newPath, byteArray)

                val newUrl = bucket.publicUrl(newPath)

                updateTagImage(context, tagId, newUrl)

                newUrl
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }


        suspend fun updateGameImageAndGetPublicUrl(
            context: Context,
            gameId: Long,
            localFile: File,
            oldImageUrl: String?
        ): String? {
            val bucket = SupabaseClientManager.supabase.storage.from("filesdatabase")
            val byteArray = localFile.readBytes()
            val newPath = "gameImages/${localFile.name}"

            return try {
                if (oldImageUrl != null && oldImageUrl.contains("filesdatabase/")) {
                    val oldPath = oldImageUrl.substringAfter("filesdatabase/")
                    try {
                        bucket.delete(listOf(oldPath))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                bucket.upload(newPath, byteArray)

                val newUrl = bucket.publicUrl(newPath)

                updateGameImage(context, gameId, newUrl)

                newUrl
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        suspend fun updateScreenshotAndGetPublicUrl(
            localFile: File,
            oldImageUrl: String?
        ): String? {
            val bucket = SupabaseClientManager.supabase.storage.from("filesdatabase")
            val byteArray = localFile.readBytes()
            val newPath = "gameScreenshot/${localFile.name}"

            return try {
                if (oldImageUrl != null && oldImageUrl.contains("filesdatabase/")) {
                    val oldPath = oldImageUrl.substringAfter("filesdatabase/")
                    try {
                        bucket.delete(listOf(oldPath))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                bucket.upload(newPath, byteArray)

                bucket.publicUrl(newPath)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        suspend fun updateListImageAndGetPublicUrl(
            context: Context,
            gameId: Long,
            localFile: File,
            oldImageUrl: String?
        ): String? {
            val bucket = SupabaseClientManager.supabase.storage.from("filesdatabase")
            val byteArray = localFile.readBytes()
            val newPath = "listImages/${localFile.name}"

            return try {
                if (oldImageUrl != null && oldImageUrl.contains("filesdatabase/")) {
                    val oldPath = oldImageUrl.substringAfter("filesdatabase/")
                    try {
                        bucket.delete(listOf(oldPath))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                bucket.upload(newPath, byteArray)

                val newUrl = bucket.publicUrl(newPath)

                updateGameListImage(context, gameId, newUrl)

                newUrl
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        suspend fun updateGameListImage(context: Context, gameListId: Long, newImage: String?) {
            val db = initializeDB(context)

            return db.listDAO().updateListImage(gameListId, newImage)
        }

        suspend fun updateGameImage(context: Context, gameId: Long, newImage: String?) {
            val db = initializeDB(context)

            return db.gameDAO().updateGameImage(gameId, newImage)
        }

        suspend fun updateTagImage(context: Context, gameId: Long, newImage: String) {
            val db = initializeDB(context)

            return db.tagDao().updateTagImage(gameId, newImage)
        }

        suspend fun deleteScreenshotsByGameId(context: Context, gameId: Long) {

            val screenshotsList = getScreenshots(context, gameId)?.asFlow()?.first()

            if (screenshotsList != null) {
                for (screenshot in screenshotsList) {
                    deleteScreenshot(context, screenshot)
                }
            }
        }

        fun getVaultStats(context: Context): LiveData<GameStats> {
            val db = initializeDB(context)
            return db.gameDAO().getVaultStats()
        }

        // TODO: List CRUD
        fun getLists(context: Context): LiveData<List<GameList>>? {
            val db = initializeDB(context)

            return db.listDAO().getLists()
        }

        fun getList(context: Context, gameListId: Long): LiveData<GameList>? {
            val db = initializeDB(context)

            return db.listDAO().getList(gameListId)
        }

        suspend fun addList(context: Context, list: GameList): Long {
            val db = initializeDB(context)

            return db.listDAO().addList(list)
        }

        suspend fun updateList(context: Context, list: GameList) {
            val db = initializeDB(context)

            return db.listDAO().updateList(
                id = list.list_id,
                name = list.name,
                description = list.description,
                image = list.image
            )
        }

        suspend fun deleteList(context: Context, list: GameList) {
            val db = initializeDB(context)

            return db.listDAO().deleteList(list)
        }

        // TODO: List game CRUD
        fun getListGamesById(context: Context, gameListId: Long): LiveData<List<Game>>? {
            val db = initializeDB(context)

            return db.listDAO().getGamesByListId(gameListId)
        }

        suspend fun addListGame(context: Context, list: List_game): Long {
            val db = initializeDB(context)

            return db.listDAO().addListGame(list)
        }

        suspend fun deleteListGame(context: Context, list: List_game) {
            val db = initializeDB(context)

            return db.listDAO().deleteListGame(list)
        }

        // TODO: Screenshot CRUD
        fun getScreenshots(context: Context, gameId: Long): LiveData<List<Screenshot>>? {
            val db = initializeDB(context)

            return db.screenshotDAO().getScreenshots(gameId)
        }

        suspend fun addScreenshot(context: Context, screenshot: Screenshot): Long {
            val db = initializeDB(context)

            return db.screenshotDAO().addScreenshot(screenshot)
        }

        suspend fun updateScreenshot(context: Context, screenshot: Screenshot) {
            val db = initializeDB(context)

            return db.screenshotDAO().updateScreenshot(
                id = screenshot.screenshot_id,
                image = screenshot.image
            )
        }

        suspend fun deleteScreenshot(context: Context, screenshot: Screenshot) {
            val db = initializeDB(context)

            return db.screenshotDAO().deleteScreenshot(screenshot)
        }

        // TODO: Tag CRUD

        fun getTags(context: Context): LiveData<List<Tag>>? {
            val db = initializeDB(context)

            return db.tagDao().getTags()
        }

        fun getPlataforms(context: Context): LiveData<List<Tag>>? {
            val db = initializeDB(context)

            return db.tagDao().getPlataforms()
        }

        suspend fun addTag(context: Context, tag: Tag): Long {
            val db = initializeDB(context)

            return db.tagDao().addTag(tag)
        }

        suspend fun updateTag(context: Context, tag: Tag) {
            val db = initializeDB(context)

            return db.tagDao().updateTag(
                id = tag.tag_id,
                image = tag.image
            )
        }

        suspend fun deleteTag(context: Context, tag: Tag) {
            val db = initializeDB(context)

            return db.tagDao().deleteTag(tag)
        }

        suspend fun addTagGame(context: Context, tagGame: TagGame): Long {
            val db = initializeDB(context)
            return db.tagDao().addTagGame(tagGame)
        }

        suspend fun deleteTagsByGameId(context: Context, gameId: Long) {
            val db = initializeDB(context)
            db.tagDao().deleteTagsByGameId(gameId)
        }

        fun getGameTags(context: Context, gameId: Long): LiveData<List<Tag>>? {
            val db = initializeDB(context)
            return db.tagDao().getGameTags(gameId)
        }

        fun getGamesByTag(context: Context, tagId: Long): LiveData<List<Game>>{
            val db = initializeDB(context)
            return db.gameDAO().getGamesByTag(tagId)
        }

        fun getGamesByTags(context: Context, tagIds: List<Long>): LiveData<List<Game>> {
            val db = initializeDB(context)
            return if (tagIds.isEmpty()) {
                db.gameDAO().getGames()
            } else {
                db.gameDAO().getGamesByTags(tagIds, tagIds.size)
            }
        }

        // TODO: Objective CRUD
        fun getObjectivesByGame(context: Context, gameId: Long): LiveData<List<Objective>> {
            val db = initializeDB(context)
            return db.objectiveDao().getObjectivesByGame(gameId)
        }

        suspend fun insertObjective(context: Context, objective: Objective) {
            val db = initializeDB(context)
            db.objectiveDao().insertObjective(objective)
        }

        suspend fun updateObjective(context: Context, objective: Objective) {
            val db = initializeDB(context)
            db.objectiveDao().updateObjective(objective)
        }

        suspend fun deleteObjective(context: Context, objective: Objective) {
            val db = initializeDB(context)
            db.objectiveDao().deleteObjective(objective)
        }

        // TODO: Annotation CRUD
        suspend fun getAnnotationByGameId(context: Context, gameId: Long): Annotation? {
            val db = initializeDB(context)
            return db.annotationDao().getAnnotationByGameId(gameId)
        }

        suspend fun insertAnnotation(context: Context, annotation: Annotation) : Long {
            val db = initializeDB(context)
            return db.annotationDao().insertAnnotation(annotation)
        }

        suspend fun updateAnnotation(context: Context, annotation: Annotation) {
            val db = initializeDB(context)
            db.annotationDao().updateAnnotation(annotation.annotation_id, annotation.text)
        }
    }
}
