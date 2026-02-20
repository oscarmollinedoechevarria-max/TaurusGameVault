package com.example.taurusgamevault.Model.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag")
data class Tag(
    @PrimaryKey val tag_id: Long,
    val name: String,
    val image: String?
)