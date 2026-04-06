package com.example.taurusgamevault.Model.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.taurusgamevault.Model.room.entities.Annotation

@Dao
interface AnnotationDao {
    @Query("SELECT * FROM annotation WHERE game_id = :gameId LIMIT 1")
    suspend fun getAnnotationByGameId(gameId: Long): Annotation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: Annotation): Long

    @Query("UPDATE annotation SET text = :text WHERE annotation_id = :id")
    suspend fun updateAnnotation(id: Long, text: String)
}
