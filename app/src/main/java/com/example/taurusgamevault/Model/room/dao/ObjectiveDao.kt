package com.example.taurusgamevault.Model.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.taurusgamevault.Model.room.entities.Objective

@Dao
interface ObjectiveDao {
    @Query("SELECT * FROM objective WHERE game_id = :gameId")
    fun getObjectivesByGame(gameId: Long): LiveData<List<Objective>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertObjective(objective: Objective)

    @Update
    suspend fun updateObjective(objective: Objective)

    @Delete
    suspend fun deleteObjective(objective: Objective)
}
