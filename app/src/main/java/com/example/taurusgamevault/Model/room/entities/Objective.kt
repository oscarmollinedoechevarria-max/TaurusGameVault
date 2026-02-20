package com.example.taurusgamevault.Model.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "objective")
data class Objective(
    @PrimaryKey val objective_id: Int,
    val text: String,
    val completed: Char
)
