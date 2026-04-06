package com.example.taurusgamevault.Model.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// deprecated
@Entity(tableName = "plataform")
data class Plataform(
    @PrimaryKey(autoGenerate = true)
    var plataform_id: Long = 0L,

    @ColumnInfo(name = "name")
    var name: String,
)