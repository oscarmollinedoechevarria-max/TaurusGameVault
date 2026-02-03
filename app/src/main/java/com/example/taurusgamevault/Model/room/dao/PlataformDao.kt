package com.example.taurusgamevault.Model.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.taurusgamevault.Model.room.entities.Plataform
import com.example.taurusgamevault.Model.room.entities.PlataformGame

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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addGamePlataform(plataformGame: PlataformGame): Long

    @Query("""
    SELECT *
    FROM plataform
    INNER JOIN plataform_game pg
        ON plataform.plataform_id = pg.plataform_id
    WHERE pg.game_id = :gameId
""")
    fun getGamePlataforms(gameId: Long): LiveData<List<Plataform>>



}