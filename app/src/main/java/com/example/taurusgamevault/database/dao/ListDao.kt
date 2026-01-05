package com.example.taurusgamevault.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.taurusgamevault.database.entities.Game
import com.example.taurusgamevault.database.entities.GameList

@Dao
interface ListDao {
    @Query("SELECT * FROM list")
    fun getLists(): LiveData<List<GameList>>

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

    @Delete
    suspend fun deleteList(list: GameList)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addList(list: GameList): Long

}