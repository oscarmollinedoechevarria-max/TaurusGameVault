package com.example.taurusgamevault.Model.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.taurusgamevault.Model.room.dao.GameDao
import com.example.taurusgamevault.Model.room.dao.ListDao
import com.example.taurusgamevault.Model.room.dao.PlataformDao
import com.example.taurusgamevault.Model.room.dao.ScreenshotDao
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.Model.room.entities.GameList
import com.example.taurusgamevault.Model.room.entities.List_game
import com.example.taurusgamevault.Model.room.entities.Plataform
import com.example.taurusgamevault.Model.room.entities.PlataformGame
import com.example.taurusgamevault.Model.room.entities.Screenshot

@Database(
    entities = [
        Game::class,
        GameList::class,
        List_game::class,
        Plataform::class,
        PlataformGame::class,
        Screenshot::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DataBase : RoomDatabase() {

    abstract fun gameDAO(): GameDao
    abstract fun listDAO(): ListDao
    abstract fun plataformDAO(): PlataformDao
    abstract fun screenshotDAO(): ScreenshotDao

    companion object {

        @Volatile
        private var INSTANCE: DataBase? = null

        fun getDatabase(context: Context): DataBase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            if (INSTANCE == null) {
                synchronized(this) {
                    // Pass the database to the INSTANCE
                    INSTANCE = buildDatabase(context)
                }
            }
            // Return database.
            return INSTANCE!!
        }

        private fun buildDatabase(context: Context): DataBase {
            context.deleteDatabase("taurus_game_vault")

            return Room.databaseBuilder(
                context.applicationContext,
                DataBase::class.java,
                "taurus_game_vault"
            )
//                .createFromAsset("database/taurus_game_vault.db")
                .build()
        }
    }
}