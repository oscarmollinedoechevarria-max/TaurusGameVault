package com.example.taurusgamevault.Model.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.taurusgamevault.Model.room.entities.Game

@Dao
interface GameDao {
    @Query("SELECT * FROM game")
    fun getGames(): LiveData<List<Game>>

    @Query("""
    UPDATE game 
    SET name = :name,
        description = :description,
        release_date = :releaseDate,
        playtime = :playtime,
        personal_rating = :personalRating,
        game_state = :gameState,
        start_date = :startDate,
        end_date = :endDate,
        priority = :priority,
        deadline = :deadline
    WHERE game_id = :id
    """)
    suspend fun updateGame(
        id: Long,
        name: String?,
        description: String?,
        releaseDate: String?,
        playtime: Int?,
        personalRating: Double?,
        gameState: String?,
        startDate: String?,
        endDate: String?,
        priority: Int?,
        deadline: String?
    )

    @Delete
    suspend fun deleteGame(game: Game)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addGame(game: Game): Long

}