package com.example.taurusgamevault.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.taurusgamevault.database.entities.Game
import com.example.taurusgamevault.database.entities.Plataform

@Dao
interface PlataformDao {

    @Query("SELECT * FROM plataform")
    fun getPlataforms(): LiveData<List<Plataform>>

    @Query("""
    UPDATE plataform
    SET name = :name
    WHERE plataform_id = :id
    """)
    suspend fun updatePlataform(
        id: Long,
        name: String,
    )

    @Delete
    suspend fun deletePlataform(plataform: Plataform)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addPlataform(plataform: Plataform): Long

}