package com.example.data

import com.example.data.db.AchievementEntity
import com.example.data.db.DailyChallengeEntity
import com.example.data.db.GameRecordEntity
import com.example.data.db.MindRushDao
import com.example.data.db.PlayerStatsEntity
import com.example.model.GameType
import com.example.model.LevelFormula
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

data class GameCompletionResult(
    val xpEarned: Int,
    val didLevelUp: Boolean,
    val newLevel: Int,
    val unlockedAchievements: List<AchievementEntity>,
    val dailyChallengeCompleted: Boolean
)

class MindRushRepository(private val dao: MindRushDao) {

    val playerStats: Flow<PlayerStatsEntity?> = dao.getPlayerStats()
    val recentRecords: Flow<List<GameRecordEntity>> = dao.getRecentGameRecords()
    val achievements: Flow<List<AchievementEntity>> = dao.getAllAchievements()

    fun getDailyChallengeForToday(): Flow<DailyChallengeEntity?> {
        val todayStr = getTodayDateString()
        return dao.getDailyChallenge(todayStr)
    }

    suspend fun initializeDefaultsIfEmpty() {
        val stats = dao.getPlayerStatsSync()
        if (stats == null) {
            dao.insertOrUpdateStats(PlayerStatsEntity())
        }

        val defaultAchievements = listOf(
            AchievementEntity(
                id = "first_game",
                title = "First Step",
                description = "Complete your very first brain arcade game.",
                category = "GENERAL",
                xpReward = 100
            ),
            AchievementEntity(
                id = "memory_starter",
                title = "Memory Initiate",
                description = "Reach at least 500 score in Memory Matrix.",
                category = "MEMORY",
                xpReward = 150
            ),
            AchievementEntity(
                id = "visual_architect",
                title = "Visual Architect",
                description = "Score over 1,500 points in Memory Matrix.",
                category = "MEMORY",
                xpReward = 300
            ),
            AchievementEntity(
                id = "lightning_reflex",
                title = "Lightning Reflex",
                description = "Achieve a reaction time under 250ms in Brain Speed.",
                category = "SPEED",
                xpReward = 250
            ),
            AchievementEntity(
                id = "speed_demon",
                title = "Reflex Master",
                description = "Complete 5 Brain Speed sessions.",
                category = "SPEED",
                xpReward = 200
            ),
            AchievementEntity(
                id = "color_maestro",
                title = "Color Maestro",
                description = "Score over 1,000 points in Color Chaos.",
                category = "ATTENTION",
                xpReward = 250
            ),
            AchievementEntity(
                id = "focus_titan",
                title = "Focus Titan",
                description = "Achieve 100% accuracy in a Color Chaos game.",
                category = "ATTENTION",
                xpReward = 300
            ),
            AchievementEntity(
                id = "math_samurai",
                title = "Math Samurai",
                description = "Score over 1,200 points in Number Ninja.",
                category = "MATH",
                xpReward = 200
            ),
            AchievementEntity(
                id = "math_prodigy",
                title = "Math Prodigy",
                description = "Score over 2,500 points in Number Ninja.",
                category = "MATH",
                xpReward = 350
            ),
            AchievementEntity(
                id = "dedicated_player",
                title = "Dedicated Player",
                description = "Complete 10 total brain arcade game sessions.",
                category = "GENERAL",
                xpReward = 300
            ),
            AchievementEntity(
                id = "xp_grinder",
                title = "XP Grinder",
                description = "Reach Player Level 3.",
                category = "GENERAL",
                xpReward = 200
            ),
            AchievementEntity(
                id = "mind_master",
                title = "Mind Master",
                description = "Reach Player Level 5.",
                category = "GENERAL",
                xpReward = 500
            ),
            AchievementEntity(
                id = "daily_challenger",
                title = "Daily Challenger",
                description = "Complete your first Daily Challenge.",
                category = "GENERAL",
                xpReward = 250
            ),
            AchievementEntity(
                id = "streak_legend",
                title = "Streak Legend",
                description = "Achieve a 5x streak in any game mode.",
                category = "GENERAL",
                xpReward = 200
            )
        )
        dao.insertAchievements(defaultAchievements)

        ensureDailyChallengePrepared()
    }

    suspend fun ensureDailyChallengePrepared(): DailyChallengeEntity {
        val todayStr = getTodayDateString()
        var challenge = dao.getDailyChallengeSync(todayStr)
        if (challenge == null) {
            challenge = generateDailyChallenge(todayStr)
            dao.insertOrUpdateDailyChallenge(challenge)
        }
        return challenge
    }

    suspend fun recordGameResult(
        gameType: GameType,
        score: Int,
        accuracy: Int,
        rawXp: Int,
        bestMs: Int = 0,
        streakCount: Int = 0
    ): GameCompletionResult {
        val currentStats = dao.getPlayerStatsSync() ?: PlayerStatsEntity()

        var newMemoryBest = currentStats.memoryMatrixHighScore
        var newSpeedBest = currentStats.brainSpeedBestMs
        var newColorBest = currentStats.colorChaosHighScore
        var newNumberBest = currentStats.numberNinjaHighScore

        when (gameType) {
            GameType.MEMORY_MATRIX -> {
                newMemoryBest = max(newMemoryBest, score)
            }
            GameType.BRAIN_SPEED -> {
                if (bestMs > 0) {
                    newSpeedBest = if (newSpeedBest == 0) bestMs else min(newSpeedBest, bestMs)
                }
            }
            GameType.COLOR_CHAOS -> {
                newColorBest = max(newColorBest, score)
            }
            GameType.NUMBER_NINJA -> {
                newNumberBest = max(newNumberBest, score)
            }
        }

        val todayStr = getTodayDateString()
        var dailyChallenge = dao.getDailyChallengeSync(todayStr) ?: generateDailyChallenge(todayStr)
        var dailyChallengeCompletedThisTime = false
        var bonusDailyXp = 0

        if (!dailyChallenge.isCompleted && dailyChallenge.gameType == gameType.name) {
            val metGoal = when (gameType) {
                GameType.BRAIN_SPEED -> bestMs in 1..dailyChallenge.targetGoal
                else -> score >= dailyChallenge.targetGoal
            }
            if (metGoal) {
                dailyChallengeCompletedThisTime = true
                bonusDailyXp = dailyChallenge.xpReward
                dailyChallenge = dailyChallenge.copy(
                    isCompleted = true,
                    currentProgress = dailyChallenge.targetGoal,
                    completedAt = System.currentTimeMillis()
                )
                dao.insertOrUpdateDailyChallenge(dailyChallenge)
            }
        }

        val totalXpAwarded = rawXp + bonusDailyXp

        val progResult = LevelFormula.calculateProgression(
            currentLevel = currentStats.currentLevel,
            currentXp = currentStats.totalXp,
            xpEarned = totalXpAwarded
        )

        val updatedStats = currentStats.copy(
            totalXp = progResult.newXp,
            currentLevel = progResult.newLevel,
            gamesPlayed = currentStats.gamesPlayed + 1,
            memoryMatrixHighScore = newMemoryBest,
            brainSpeedBestMs = newSpeedBest,
            colorChaosHighScore = newColorBest,
            numberNinjaHighScore = newNumberBest
        )
        dao.insertOrUpdateStats(updatedStats)

        dao.insertGameRecord(
            GameRecordEntity(
                gameType = gameType.name,
                score = score,
                accuracy = accuracy,
                xpEarned = totalXpAwarded
            )
        )

        val newlyUnlocked = evaluateAchievements(
            gameType = gameType,
            score = score,
            accuracy = accuracy,
            bestMs = bestMs,
            streakCount = streakCount,
            stats = updatedStats,
            dailyChallengeCompleted = dailyChallengeCompletedThisTime
        )

        return GameCompletionResult(
            xpEarned = totalXpAwarded,
            didLevelUp = progResult.didLevelUp,
            newLevel = progResult.newLevel,
            unlockedAchievements = newlyUnlocked,
            dailyChallengeCompleted = dailyChallengeCompletedThisTime
        )
    }

    private suspend fun evaluateAchievements(
        gameType: GameType,
        score: Int,
        accuracy: Int,
        bestMs: Int,
        streakCount: Int,
        stats: PlayerStatsEntity,
        dailyChallengeCompleted: Boolean
    ): List<AchievementEntity> {
        val locked = dao.getLockedAchievementsSync()
        val unlockedList = mutableListOf<AchievementEntity>()
        val now = System.currentTimeMillis()

        for (ach in locked) {
            var shouldUnlock = false
            when (ach.id) {
                "first_game" -> {
                    if (stats.gamesPlayed >= 1) shouldUnlock = true
                }
                "memory_starter" -> {
                    if (gameType == GameType.MEMORY_MATRIX && score >= 500) shouldUnlock = true
                }
                "visual_architect" -> {
                    if (gameType == GameType.MEMORY_MATRIX && score >= 1500) shouldUnlock = true
                }
                "lightning_reflex" -> {
                    if (gameType == GameType.BRAIN_SPEED && bestMs in 1..250) shouldUnlock = true
                }
                "speed_demon" -> {
                    val count = dao.getGameCountByType(GameType.BRAIN_SPEED.name)
                    if (count >= 5) shouldUnlock = true
                }
                "color_maestro" -> {
                    if (gameType == GameType.COLOR_CHAOS && score >= 1000) shouldUnlock = true
                }
                "focus_titan" -> {
                    if (gameType == GameType.COLOR_CHAOS && accuracy >= 100) shouldUnlock = true
                }
                "math_samurai" -> {
                    if (gameType == GameType.NUMBER_NINJA && score >= 1200) shouldUnlock = true
                }
                "math_prodigy" -> {
                    if (gameType == GameType.NUMBER_NINJA && score >= 2500) shouldUnlock = true
                }
                "dedicated_player" -> {
                    if (stats.gamesPlayed >= 10) shouldUnlock = true
                }
                "xp_grinder" -> {
                    if (stats.currentLevel >= 3) shouldUnlock = true
                }
                "mind_master" -> {
                    if (stats.currentLevel >= 5) shouldUnlock = true
                }
                "daily_challenger" -> {
                    if (dailyChallengeCompleted || dao.getCompletedDailyChallengesCount() >= 1) shouldUnlock = true
                }
                "streak_legend" -> {
                    if (streakCount >= 5) shouldUnlock = true
                }
            }

            if (shouldUnlock) {
                val updated = ach.copy(isUnlocked = true, unlockedAt = now)
                dao.updateAchievement(updated)
                unlockedList.add(updated)
            }
        }

        return unlockedList
    }

    suspend fun updateSettings(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        val stats = dao.getPlayerStatsSync() ?: PlayerStatsEntity()
        dao.insertOrUpdateStats(stats.copy(soundEnabled = soundEnabled, hapticsEnabled = hapticsEnabled))
    }

    suspend fun resetAllProgress() {
        val current = dao.getPlayerStatsSync() ?: PlayerStatsEntity()
        dao.insertOrUpdateStats(
            PlayerStatsEntity(
                id = 1,
                totalXp = 0,
                currentLevel = 1,
                gamesPlayed = 0,
                memoryMatrixHighScore = 0,
                brainSpeedBestMs = 0,
                colorChaosHighScore = 0,
                numberNinjaHighScore = 0,
                soundEnabled = current.soundEnabled,
                hapticsEnabled = current.hapticsEnabled
            )
        )
        val defaultAchievements = listOf(
            AchievementEntity("first_game", "First Step", "Complete your very first brain arcade game.", "GENERAL", 100),
            AchievementEntity("memory_starter", "Memory Initiate", "Reach at least 500 score in Memory Matrix.", "MEMORY", 150),
            AchievementEntity("visual_architect", "Visual Architect", "Score over 1,500 points in Memory Matrix.", "MEMORY", 300),
            AchievementEntity("lightning_reflex", "Lightning Reflex", "Achieve a reaction time under 250ms in Brain Speed.", "SPEED", 250),
            AchievementEntity("speed_demon", "Reflex Master", "Complete 5 Brain Speed sessions.", "SPEED", 200),
            AchievementEntity("color_maestro", "Color Maestro", "Score over 1,000 points in Color Chaos.", "ATTENTION", 250),
            AchievementEntity("focus_titan", "Focus Titan", "Achieve 100% accuracy in a Color Chaos game.", "ATTENTION", 300),
            AchievementEntity("math_samurai", "Math Samurai", "Score over 1,200 points in Number Ninja.", "MATH", 200),
            AchievementEntity("math_prodigy", "Math Prodigy", "Score over 2,500 points in Number Ninja.", "MATH", 350),
            AchievementEntity("dedicated_player", "Dedicated Player", "Complete 10 total brain arcade game sessions.", "GENERAL", 300),
            AchievementEntity("xp_grinder", "XP Grinder", "Reach Player Level 3.", "GENERAL", 200),
            AchievementEntity("mind_master", "Mind Master", "Reach Player Level 5.", "GENERAL", 500),
            AchievementEntity("daily_challenger", "Daily Challenger", "Complete your first Daily Challenge.", "GENERAL", 250),
            AchievementEntity("streak_legend", "Streak Legend", "Achieve a 5x streak in any game mode.", "GENERAL", 200)
        )
        for (a in defaultAchievements) {
            dao.updateAchievement(a)
        }
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun generateDailyChallenge(dateStr: String): DailyChallengeEntity {
        val hash = dateStr.hashCode()
        val index = kotlin.math.abs(hash) % 4
        return when (index) {
            0 -> DailyChallengeEntity(
                dateString = dateStr,
                gameType = GameType.MEMORY_MATRIX.name,
                title = "Matrix Recall Blitz",
                targetDescription = "Score at least 800 points in Memory Matrix.",
                targetGoal = 800,
                xpReward = 200
            )
            1 -> DailyChallengeEntity(
                dateString = dateStr,
                gameType = GameType.BRAIN_SPEED.name,
                title = "Sub-300ms Reflexes",
                targetDescription = "Clock a reaction time under 300ms in Brain Speed.",
                targetGoal = 300,
                xpReward = 250
            )
            2 -> DailyChallengeEntity(
                dateString = dateStr,
                gameType = GameType.COLOR_CHAOS.name,
                title = "Stroop Overdrive",
                targetDescription = "Score at least 1,000 points in Color Chaos.",
                targetGoal = 1000,
                xpReward = 220
            )
            else -> DailyChallengeEntity(
                dateString = dateStr,
                gameType = GameType.NUMBER_NINJA.name,
                title = "Rapid Math Strike",
                targetDescription = "Score at least 1,500 points in Number Ninja.",
                targetGoal = 1500,
                xpReward = 240
            )
        }
    }
}
