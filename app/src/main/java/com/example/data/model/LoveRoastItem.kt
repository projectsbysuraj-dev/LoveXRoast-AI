package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "love_roast_history")
data class LoveRoastItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "LOVE" or "ROAST"
    val tab: String,  // "NAME", "SITUATION", "COMBO"
    val inputName: String?,
    val inputSituation: String?,
    val outputText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
