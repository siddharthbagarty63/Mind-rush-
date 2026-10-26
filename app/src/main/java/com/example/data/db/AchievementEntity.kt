package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String, // MEMORY, SPEED, ATTENTION, MATH, GENERAL
    val xpReward: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L
)
