package com.example.taurusgamevault.Model.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.taurusgamevault.Model.room.dao.AnnotationDao
import com.example.taurusgamevault.Model.room.dao.GameDao
import com.example.taurusgamevault.Model.room.dao.ListDao
import com.example.taurusgamevault.Model.room.dao.ObjectiveDao
import com.example.taurusgamevault.Model.room.dao.ScreenshotDao
import com.example.taurusgamevault.Model.room.dao.TagDao
import com.example.taurusgamevault.Model.room.entities.Annotation
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.Model.room.entities.GameList
import com.example.taurusgamevault.Model.room.entities.List_game
import com.example.taurusgamevault.Model.room.entities.Objective
import com.example.taurusgamevault.Model.room.entities.Screenshot
import com.example.taurusgamevault.Model.room.entities.TagGame
import com.example.taurusgamevault.Model.room.entities.TagList
import com.example.taurusgamevault.Model.room.entities.Tag

@Database(
    entities = [
        Game::class,
        GameList::class,
        List_game::class,
        Screenshot::class,
        Tag::class,
        TagGame::class,
        TagList::class,
        Annotation::class,
        Objective::class,
    ],
    version = 1, // 3
    exportSchema = false
)
abstract class DataBase : RoomDatabase() {

    abstract fun gameDAO(): GameDao
    abstract fun listDAO(): ListDao
    abstract fun screenshotDAO(): ScreenshotDao
    abstract fun tagDao(): TagDao
    abstract fun annotationDao(): AnnotationDao

    abstract fun objectiveDao(): ObjectiveDao


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

        fun clearInstance() {
            INSTANCE = null
        }

        private fun buildDatabase(context: Context): DataBase {
//            context.deleteDatabase("taurus_game_vault")

            return Room.databaseBuilder(
                context.applicationContext,
                DataBase::class.java,
                "taurus_game_vault"
            )
//                .fallbackToDestructiveMigration()
                .createFromAsset("database/taurus_game_vault.db")
                .build()
        }
    }
}