package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_stats")
data class PlayerStatsEntity(
    @PrimaryKey val id: Int = 1,
    val totalXp: Int = 0,
    val currentLevel: Int = 1,
    val gamesPlayed: Int = 0,
    val memoryMatrixHighScore: Int = 0,
    val brainSpeedBestMs: Int = 0, // 0 means not yet set, otherwise lowest ms is best
    val colorChaosHighScore: Int = 0,
    val numberNinjaHighScore: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)
