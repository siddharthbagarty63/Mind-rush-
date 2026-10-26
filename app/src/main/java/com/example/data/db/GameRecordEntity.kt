package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameType: String,
    val score: Int,
    val accuracy: Int,
    val xpEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
)
