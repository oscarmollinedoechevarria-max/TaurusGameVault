package com.example.taurusgamevault.Model.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.Model.room.entities.GameList
import com.example.taurusgamevault.Model.room.entities.List_game

@Dao
interface ListDao {
    @Query("SELECT * FROM list")
    fun getLists(): LiveData<List<GameList>>

    @Query("SELECT * FROM list WHERE list_id = :gameListId")
    fun getList(gameListId: Long): LiveData<GameList>


    @Query("""
    UPDATE list
    SET name = :name,
        description = :description,
        image = :image
    WHERE list_id = :id
""")
    suspend fun updateList(
        id: Long,
        name: String,
        description: String?,
        image: String?
    )

    @Query("UPDATE list SET image = :image WHERE list_id = :id")
    suspend fun updateListImage(id: Long, image: String?)

    @Delete
    suspend fun deleteList(list: GameList)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addList(list: GameList): Long

    @Query("SELECT game.* FROM game INNER JOIN list_game ON game.game_id = list_game.game_id WHERE list_game.list_id = :gameListId")
    fun getGamesByListId(gameListId: Long): LiveData<List<Game>>
    @Delete
    suspend fun deleteListGame(list: List_game)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addListGame(list: List_game): Long

}