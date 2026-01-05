package com.example.taurusgamevault.database

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.taurusgamevault.database.entities.Game
import com.example.taurusgamevault.database.entities.GameList
import com.example.taurusgamevault.database.entities.Plataform
import com.example.taurusgamevault.database.entities.Screenshot

class Repository {
    companion object {
        fun initializeDB(context: Context): DataBase {
            return DataBase.getDatabase(context)
        }

        // TODO: Game CRUD
        fun getGames(context: Context): LiveData<List<Game>>? {
            val db = initializeDB(context)

            return db.gameDAO().getGames()
        }

        suspend fun addGame(context: Context, game: Game): Long {
            val db = initializeDB(context)

            return db.gameDAO().addGame(game)
        }

        suspend fun updateGame(context: Context, game: Game){
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

        suspend fun deleteGame(context: Context, game: Game){
            val db = initializeDB(context)

            return db.gameDAO().deleteGame(game)
        }

        // TODO: List CRUD
        fun getList(context: Context): LiveData<List<GameList>>? {
            val db = initializeDB(context)

            return db.listDAO().getLists()
        }

        suspend fun addList(context: Context, list: GameList): Long {
            val db = initializeDB(context)

            return db.listDAO().addList(list)
        }

        suspend fun updateList(context: Context, list: GameList){
            val db = initializeDB(context)

            return db.listDAO().updateList(
                id = list.list_id,
                name = list.name,
                description = list.description,
                image = list.image
            )
        }

        suspend fun deleteList(context: Context, list: GameList){
            val db = initializeDB(context)

            return db.listDAO().deleteList(list)
        }

        // TODO: Plataform CRUD
        fun getPlataforms(context: Context): LiveData<List<Plataform>>? {
            val db = initializeDB(context)

            return db.plataformDAO().getPlataforms()
        }

        suspend fun addPlataform(context: Context, plataform: Plataform): Long {
            val db = initializeDB(context)

            return db.plataformDAO().addPlataform(plataform)
        }

        suspend fun updatePlataform(context: Context, plataform: Plataform){
            val db = initializeDB(context)

            return db.plataformDAO().updatePlataform(
                id = plataform.plataform_id,
                name = plataform.name
            )
        }

        suspend fun deletePlataform(context: Context, plataform: Plataform){
            val db = initializeDB(context)

            return db.plataformDAO().deletePlataform(plataform)
        }

        // TODO: Screenshot CRUD
        fun getScreenshots(context: Context): LiveData<List<Screenshot>>? {
            val db = initializeDB(context)

            return db.screenshotDAO().getScreenshots()
        }

        suspend fun addScreenshot(context: Context, screenshot: Screenshot): Long {
            val db = initializeDB(context)

            return db.screenshotDAO().addScreenshot(screenshot)
        }

        suspend fun updateScreenshot(context: Context, screenshot: Screenshot){
            val db = initializeDB(context)

            return db.screenshotDAO().updateScreenshot(
                id = screenshot.screenshot_id,
                image = screenshot.image
            )
        }

        suspend fun deleteScreenshot(context: Context, screenshot: Screenshot){
            val db = initializeDB(context)

            return db.screenshotDAO().deleteScreenshot(screenshot)
        }

    }
}