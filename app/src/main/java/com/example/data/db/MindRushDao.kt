package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MindRushDao {
    // Stats
    @Query("SELECT * FROM player_stats WHERE id = 1")
    fun getPlayerStats(): Flow<PlayerStatsEntity?>

    @Query("SELECT * FROM player_stats WHERE id = 1")
    suspend fun getPlayerStatsSync(): PlayerStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: PlayerStatsEntity)

    // Game Records
    @Insert
    suspend fun insertGameRecord(record: GameRecordEntity)

    @Query("SELECT * FROM game_records ORDER BY timestamp DESC LIMIT 30")
    fun getRecentGameRecords(): Flow<List<GameRecordEntity>>

    @Query("SELECT COUNT(*) FROM game_records")
    suspend fun getTotalGamesCount(): Int

    @Query("SELECT COUNT(*) FROM game_records WHERE gameType = :type")
    suspend fun getGameCountByType(type: String): Int

    // Achievements
    @Query("SELECT * FROM achievements ORDER BY isUnlocked DESC, id ASC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 0")
    suspend fun getLockedAchievementsSync(): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievements(list: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    // Daily Challenges
    @Query("SELECT * FROM daily_challenges WHERE dateString = :dateStr")
    fun getDailyChallenge(dateStr: String): Flow<DailyChallengeEntity?>

    @Query("SELECT * FROM daily_challenges WHERE dateString = :dateStr")
    suspend fun getDailyChallengeSync(dateStr: String): DailyChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyChallenge(challenge: DailyChallengeEntity)

    @Query("SELECT COUNT(*) FROM daily_challenges WHERE isCompleted = 1")
    suspend fun getCompletedDailyChallengesCount(): Int
}
