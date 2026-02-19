package com.example.taurusgamevault.Model.Repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import com.example.taurusgamevault.Model.room.DataBase
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.Model.room.entities.GameList
import com.example.taurusgamevault.Model.room.entities.List_game
import com.example.taurusgamevault.Model.room.entities.Plataform
import com.example.taurusgamevault.Model.room.entities.PlataformGame
import com.example.taurusgamevault.Model.room.entities.Screenshot
import com.example.taurusgamevault.Model.supabase.SupabaseClientManager
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.flow.first
import java.io.File

class Repository {
    companion object {
        fun initializeDB(context: Context): DataBase {
            return DataBase.Companion.getDatabase(context)
        }

        // TODO: Game CRUD
        //TODO: REDO THE IMPORT GAMES SCRIPT
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

            val pathInBucket = if (oldImageUrl != null && oldImageUrl.contains("filesdatabase/")) {
                oldImageUrl.substringAfter("filesdatabase/")
            } else {
                "listImages/${localFile.name}"
            }

            return try {
                if (oldImageUrl != null) {
                    bucket.update(pathInBucket, byteArray)
                } else {
                    bucket.upload(pathInBucket, byteArray)
                }

                val newUrl = bucket.publicUrl(pathInBucket)

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

        suspend fun deleteScreenshotsByGameId(context: Context, gameId: Long) {
            val db = initializeDB(context)

            val screenshotsList = getScreenshots(context, gameId)?.asFlow()?.first()

            if (screenshotsList != null) {
                for (screenshot in screenshotsList) {
                    deleteScreenshot(context, screenshot)
                }
            }
        }

        suspend fun addGamePlataform(context: Context, gameplataform: PlataformGame): Long {
            val db = initializeDB(context)

            return db.plataformDAO().addGamePlataform(gameplataform)
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


        // TODO: Plataform CRUD
        fun getPlataforms(context: Context): LiveData<List<Plataform>>? {
            val db = initializeDB(context)

            return db.plataformDAO().getPlataforms()
        }

        fun getGamePlataforms(context: Context, gameId: Long): LiveData<List<Plataform>>? {
            val db = initializeDB(context)

            return db.plataformDAO().getGamePlataforms(gameId)
        }

        suspend fun addPlataform(context: Context, plataform: Plataform): Long {
            val db = initializeDB(context)

            return db.plataformDAO().addPlataform(plataform)
        }

        suspend fun updatePlataform(context: Context, plataform: Plataform) {
            val db = initializeDB(context)

            return db.plataformDAO().updatePlataform(
                id = plataform.plataform_id,
                name = plataform.name
            )
        }

        suspend fun deletePlataform(context: Context, plataform: Plataform) {
            val db = initializeDB(context)

            return db.plataformDAO().deletePlataform(plataform)
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

    }

}