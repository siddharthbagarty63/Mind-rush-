package com.example.model

object LevelFormula {
    fun xpRequiredForLevel(level: Int): Int {
        return 100 + (level * 50)
    }

    fun getLevelTitle(level: Int): String {
        return when (level) {
            1 -> "Beginner"
            2 -> "Quick Thinker"
            3 -> "Brain Explorer"
            4 -> "Mind Master"
            5 -> "Brain Champion"
            6 -> "Neural Prodigy"
            7 -> "Arcade Legend"
            8 -> "Quantum Intellect"
            9 -> "Cyber Sage"
            10 -> "Neural Overlord"
            11 -> "Cosmic Thinker"
            else -> "Infinite Mind"
        }
    }

    /**
     * Calculates (newLevel, remainingXpInLevel, leveledUp) given starting level, starting XP, and xpEarned.
     */
    fun calculateProgression(currentLevel: Int, currentXp: Int, xpEarned: Int): ProgressionResult {
        var level = currentLevel
        var xp = currentXp + xpEarned
        var didLevelUp = false

        while (true) {
            val req = xpRequiredForLevel(level)
            if (xp >= req) {
                xp -= req
                level += 1
                didLevelUp = true
            } else {
                break
            }
        }

        return ProgressionResult(
            newLevel = level,
            newXp = xp,
            didLevelUp = didLevelUp,
            xpForNext = xpRequiredForLevel(level)
        )
    }
}

data class ProgressionResult(
    val newLevel: Int,
    val newXp: Int,
    val didLevelUp: Boolean,
    val xpForNext: Int
)
