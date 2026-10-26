package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.audio.VibrationManager
import com.example.data.db.AchievementEntity
import com.example.data.db.AppDatabase
import com.example.data.db.DailyChallengeEntity
import com.example.data.db.GameRecordEntity
import com.example.data.db.PlayerStatsEntity
import com.example.data.GameCompletionResult
import com.example.data.MindRushRepository
import com.example.model.GameType
import com.example.model.LevelFormula
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    GAMES_HUB,
    PROGRESS,
    SETTINGS,
    MEMORY_MATRIX,
    BRAIN_SPEED,
    COLOR_CHAOS,
    NUMBER_NINJA
}

data class LevelUpPopupState(
    val isVisible: Boolean = false,
    val newLevel: Int = 1,
    val levelTitle: String = ""
)

data class AchievementBannerState(
    val isVisible: Boolean = false,
    val title: String = "",
    val description: String = ""
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = MindRushRepository(db.mindRushDao())

    val soundManager = SoundManager()
    val vibrationManager = VibrationManager(application)

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _screenHistory = mutableListOf<AppScreen>()

    private val _levelUpPopup = MutableStateFlow(LevelUpPopupState())
    val levelUpPopup: StateFlow<LevelUpPopupState> = _levelUpPopup.asStateFlow()

    private val _achievementBanner = MutableStateFlow(AchievementBannerState())
    val achievementBanner: StateFlow<AchievementBannerState> = _achievementBanner.asStateFlow()

    val playerStats: StateFlow<PlayerStatsEntity?> = repository.playerStats
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val recentRecords: StateFlow<List<GameRecordEntity>> = repository.recentRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val achievements: StateFlow<List<AchievementEntity>> = repository.achievements
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dailyChallenge: StateFlow<DailyChallengeEntity?> = repository.getDailyChallengeForToday()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfEmpty()
        }
        viewModelScope.launch {
            playerStats.collect { stats ->
                if (stats != null) {
                    soundManager.isSoundEnabled = stats.soundEnabled
                    vibrationManager.isHapticsEnabled = stats.hapticsEnabled
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            _screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
            soundManager.playTap()
            vibrationManager.vibrateTap()
        }
    }

    fun navigateBack(): Boolean {
        if (_screenHistory.isNotEmpty()) {
            val previous = _screenHistory.removeAt(_screenHistory.lastIndex)
            _currentScreen.value = previous
            soundManager.playTap()
            vibrationManager.vibrateTap()
            return true
        } else if (_currentScreen.value != AppScreen.HOME) {
            _currentScreen.value = AppScreen.HOME
            soundManager.playTap()
            vibrationManager.vibrateTap()
            return true
        }
        return false
    }

    fun submitGameResult(
        gameType: GameType,
        score: Int,
        accuracy: Int,
        rawXp: Int,
        bestMs: Int = 0,
        streakCount: Int = 0,
        onFinished: ((GameCompletionResult) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.recordGameResult(
                gameType = gameType,
                score = score,
                accuracy = accuracy,
                rawXp = rawXp,
                bestMs = bestMs,
                streakCount = streakCount
            )

            // Audio & Haptic triggers
            soundManager.playXpReward()

            if (result.didLevelUp) {
                soundManager.playLevelUp()
                vibrationManager.vibrateLevelUp()
                _levelUpPopup.value = LevelUpPopupState(
                    isVisible = true,
                    newLevel = result.newLevel,
                    levelTitle = LevelFormula.getLevelTitle(result.newLevel)
                )
            } else {
                soundManager.playVictory()
                vibrationManager.vibrateSuccess()
            }

            if (result.unlockedAchievements.isNotEmpty()) {
                val firstAch = result.unlockedAchievements.first()
                soundManager.playAchievement()
                _achievementBanner.value = AchievementBannerState(
                    isVisible = true,
                    title = firstAch.title,
                    description = firstAch.description
                )
            }

            onFinished?.invoke(result)
        }
    }

    fun toggleSound(enabled: Boolean) {
        soundManager.isSoundEnabled = enabled
        viewModelScope.launch {
            repository.updateSettings(
                soundEnabled = enabled,
                hapticsEnabled = vibrationManager.isHapticsEnabled
            )
        }
        if (enabled) soundManager.playTap()
    }

    fun toggleHaptics(enabled: Boolean) {
        vibrationManager.isHapticsEnabled = enabled
        viewModelScope.launch {
            repository.updateSettings(
                soundEnabled = soundManager.isSoundEnabled,
                hapticsEnabled = enabled
            )
        }
        if (enabled) vibrationManager.vibrateTap()
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllProgress()
            soundManager.playWrong()
            vibrationManager.vibrateError()
        }
    }

    fun dismissLevelUpPopup() {
        _levelUpPopup.value = _levelUpPopup.value.copy(isVisible = false)
    }

    fun dismissAchievementBanner() {
        _achievementBanner.value = _achievementBanner.value.copy(isVisible = false)
    }
}
