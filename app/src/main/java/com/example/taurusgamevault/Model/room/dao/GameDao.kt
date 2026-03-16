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

    @Query("SELECT * FROM game WHERE game_id = :gameID")
    fun getGame(gameID: Long): LiveData<Game>

    @Query("""
        SELECT game.* FROM game
        INNER JOIN tag_game ON game.game_id = tag_game.game_id
        WHERE tag_game.tag_id = :tagId
    """)
    fun getGamesByTag(tagId: Long): LiveData<List<Game>>

    @Query("""
    SELECT game.* FROM game
    INNER JOIN tag_game ON game.game_id = tag_game.game_id
    WHERE tag_game.tag_id IN (:tagIds)
    GROUP BY game.game_id
    HAVING COUNT(DISTINCT tag_game.tag_id) = :tagCount
""")
    fun getGamesByTags(tagIds: List<Long>, tagCount: Int): LiveData<List<Game>>

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
        personalRating: Float?,
        gameState: String?,
        startDate: String?,
        endDate: String?,
        priority: String?,
        deadline: String?
    )

    @Query("UPDATE game SET game_image = :newImage WHERE game_id = :id")
    suspend fun updateGameImage(id: Long, newImage: String?)

    @Delete
    suspend fun deleteGame(game: Game)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addGame(game: Game): Long

}
