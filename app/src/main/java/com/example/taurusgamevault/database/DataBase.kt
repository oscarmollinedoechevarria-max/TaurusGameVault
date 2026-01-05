package com.example.taurusgamevault.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.taurusgamevault.database.dao.GameDao
import com.example.taurusgamevault.database.dao.ListDao
import com.example.taurusgamevault.database.dao.PlataformDao
import com.example.taurusgamevault.database.dao.ScreenshotDao
import com.example.taurusgamevault.database.entities.Game
import com.example.taurusgamevault.database.entities.GameList
import com.example.taurusgamevault.database.entities.List_game
import com.example.taurusgamevault.database.entities.Plataform
import com.example.taurusgamevault.database.entities.Plataform_game
import com.example.taurusgamevault.database.entities.Screenshot

@Database(
    entities = [
        Game::class,
        GameList::class,
        List_game::class,
        Plataform::class,
        Plataform_game::class,
        Screenshot::class
    ],
    version = 1,
    exportSchema = true
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
            if (DataBase.Companion.INSTANCE == null) {
                synchronized(this) {
                    // Pass the database to the INSTANCE
                    DataBase.Companion.INSTANCE = DataBase.Companion.buildDatabase(context)
                }
            }
            // Return database.
            return DataBase.Companion.INSTANCE!!
        }

        private fun buildDatabase(context: Context): DataBase {
            context.deleteDatabase("taurus_game_vault")

            return Room.databaseBuilder(
                context.applicationContext,
                DataBase::class.java,
                "taurus_game_vault"
            )
                .createFromAsset("database/taurus_game_vault.db")
                .build()
        }
    }
}
