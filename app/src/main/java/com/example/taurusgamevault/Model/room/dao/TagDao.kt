package com.example.taurusgamevault.Model.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.taurusgamevault.Model.room.entities.Tag
import com.example.taurusgamevault.Model.room.entities.TagGame

@Dao
interface TagDao {
    @Query("SELECT * FROM tag WHERE tag_id = :tagId")
    fun getTag(tagId: Long): LiveData<List<Tag>>

    @Query("SELECT * FROM tag")
    fun getTags(): LiveData<List<Tag>>

    @Query("SELECT * FROM tag WHERE isPlataform = 1")
    fun getPlataforms(): LiveData<List<Tag>>

    @Query("""
    UPDATE tag
    SET image = :image
    WHERE tag_id = :id
    """)
    suspend fun updateTag(
        id: Long,
        image: String?,
    )

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTag(tag: Tag): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagGame(tagGame: TagGame): Long

    @Delete
    suspend fun deleteTagGame(tagGame: TagGame)

    @Query("DELETE FROM tag_game WHERE game_id = :gameId")
    suspend fun deleteTagsByGameId(gameId: Long)

    @Query("""
        SELECT t.* FROM tag t
        INNER JOIN tag_game tg ON t.tag_id = tg.tag_id
        WHERE tg.game_id = :gameId
    """)
    fun getGameTags(gameId: Long): LiveData<List<Tag>>
}
