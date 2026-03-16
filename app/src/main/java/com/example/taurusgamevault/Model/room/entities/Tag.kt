package com.example.taurusgamevault.Model.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val tag_id: Long = 0L,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "isPlataform")
    val isPlataform: Boolean = false,

    @ColumnInfo(name = "image")
    val image: String?
)