package com.example.taurusgamevault.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.taurusgamevault.database.entities.Game
import com.example.taurusgamevault.database.entities.Plataform
import com.example.taurusgamevault.database.entities.Screenshot

@Dao
interface ScreenshotDao {
    @Query("SELECT * FROM screenshot")
    fun getScreenshots(): LiveData<List<Screenshot>>

    @Query("""
    UPDATE screenshot
    SET image = :image
    WHERE screenshot_id = :id
    """)
    suspend fun updateScreenshot(
        id: Long,
        image: String,
    )

    @Delete
    suspend fun deleteScreenshot(screenshot: Screenshot)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addScreenshot(screenshot: Screenshot): Long

}