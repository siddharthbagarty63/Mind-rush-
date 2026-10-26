package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_challenges")
data class DailyChallengeEntity(
    @PrimaryKey val dateString: String, // YYYY-MM-DD
    val gameType: String,
    val title: String,
    val targetDescription: String,
    val targetGoal: Int,
    val xpReward: Int,
    val isCompleted: Boolean = false,
    val currentProgress: Int = 0,
    val completedAt: Long = 0L
)
