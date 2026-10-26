package com.example.ui.screens

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameType
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonCard
import com.example.ui.theme.BgNearBlack
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ErrorPink
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningYellow
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

private enum class SpeedState {
    IDLE,
    WAITING,
    READY_TO_TAP,
    FALSE_START,
    ROUND_RESULT,
    SESSION_COMPLETE
}

@Composable
fun BrainSpeedScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val totalRounds = 5
    var currentRound by remember { mutableIntStateOf(1) }
    var speedState by remember { mutableStateOf(SpeedState.IDLE) }
    var signalTimestamp by remember { mutableLongStateOf(0L) }
    var lastReactionMs by remember { mutableLongStateOf(0L) }
    var falseStartCount by remember { mutableIntStateOf(0) }

    val reactionTimes = remember { mutableStateListOf<Long>() }

    fun scheduleSignal() {
        speedState = SpeedState.WAITING
    }

    LaunchedEffect(speedState) {
        if (speedState == SpeedState.WAITING) {
            val randomDelay = Random.nextLong(1800, 4200)
            delay(randomDelay)
            if (speedState == SpeedState.WAITING) {
                signalTimestamp = SystemClock.elapsedRealtime()
                speedState = SpeedState.READY_TO_TAP
                viewModel.soundManager.playCorrect()
            }
        }
    }

    fun handleTap() {
        when (speedState) {
            SpeedState.IDLE -> {
                viewModel.soundManager.playTap()
                viewModel.vibrationManager.vibrateTap()
                scheduleSignal()
            }
            SpeedState.WAITING -> {
                // False start!
                speedState = SpeedState.FALSE_START
                falseStartCount++
                viewModel.soundManager.playWrong()
                viewModel.vibrationManager.vibrateError()
            }
            SpeedState.READY_TO_TAP -> {
                val now = SystemClock.elapsedRealtime()
                val delta = (now - signalTimestamp).coerceAtLeast(1)
                lastReactionMs = delta
                reactionTimes.add(delta)
                viewModel.vibrationManager.vibrateSuccess()

                if (currentRound >= totalRounds) {
                    speedState = SpeedState.SESSION_COMPLETE
                    val best = reactionTimes.minOrNull()?.toInt() ?: delta.toInt()
                    val avg = reactionTimes.average().toInt()
                    // Score formula: 1000 - avg (capped 100..1000), minus false starts penalty
                    val calculatedScore = (1000 - avg).coerceIn(100, 1000) - (falseStartCount * 50).coerceAtLeast(0)
                    val rawXp = (calculatedScore / 6).coerceIn(40, 250)
                    viewModel.submitGameResult(
                        gameType = GameType.BRAIN_SPEED,
                        score = calculatedScore.coerceAtLeast(50),
                        accuracy = if (reactionTimes.size + falseStartCount > 0) {
                            ((reactionTimes.size.toFloat() / (reactionTimes.size + falseStartCount)) * 100).toInt()
                        } else 100,
                        rawXp = rawXp,
                        bestMs = best
                    )
                } else {
                    speedState = SpeedState.ROUND_RESULT
                }
            }
            SpeedState.FALSE_START -> {
                viewModel.soundManager.playTap()
                scheduleSignal()
            }
            SpeedState.ROUND_RESULT -> {
                currentRound++
                viewModel.soundManager.playTap()
                scheduleSignal()
            }
            SpeedState.SESSION_COMPLETE -> {
                // Done
            }
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = when (speedState) {
            SpeedState.WAITING -> Color(0xFF1E1026) // Deep waiting violet
            SpeedState.READY_TO_TAP -> NeonGreen // High-visibility signal
            SpeedState.FALSE_START -> ErrorPink
            else -> BgNearBlack
        },
        animationSpec = tween(durationMillis = 80),
        label = "speed_bg_anim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("brain_speed_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("brain_speed_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (speedState == SpeedState.READY_TO_TAP) BgNearBlack else TextPrimary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BRAIN SPEED",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = if (speedState == SpeedState.READY_TO_TAP) BgNearBlack else NeonGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "ROUND $currentRound OF $totalRounds",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (speedState == SpeedState.READY_TO_TAP) BgNearBlack else TextSecondary
                    )
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = if (speedState == SpeedState.READY_TO_TAP) BgNearBlack else WarningYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${reactionTimes.minOrNull() ?: "--"}ms",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (speedState == SpeedState.READY_TO_TAP) BgNearBlack else TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Reactive Tap Zone
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 2.dp,
                    color = when (speedState) {
                        SpeedState.READY_TO_TAP -> BgNearBlack
                        SpeedState.FALSE_START -> ErrorPink
                        SpeedState.WAITING -> ElectricPurple
                        else -> SurfaceCardBorder
                    },
                    shape = RoundedCornerShape(24.dp)
                )
                .background(
                    when (speedState) {
                        SpeedState.READY_TO_TAP -> NeonGreen
                        SpeedState.FALSE_START -> ErrorPink.copy(alpha = 0.2f)
                        SpeedState.WAITING -> ElectricPurple.copy(alpha = 0.15f)
                        else -> SurfaceCard
                    }
                )
                .clickable(
                    enabled = speedState != SpeedState.SESSION_COMPLETE,
                    onClick = { handleTap() }
                )
                .testTag("reaction_tap_target"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                when (speedState) {
                    SpeedState.IDLE -> {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "TAP TO START ROUND $currentRound",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "When screen turns green, tap immediately!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                    SpeedState.WAITING -> {
                        Text(
                            text = "WAIT FOR GREEN...",
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = ElectricPurple,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Do not tap early!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary
                            )
                        )
                    }
                    SpeedState.READY_TO_TAP -> {
                        Text(
                            text = "TAP NOW!",
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = BgNearBlack,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                    SpeedState.FALSE_START -> {
                        Text(
                            text = "FALSE START!",
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = ErrorPink,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You tapped before the green signal.\nTap to retry round.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                    SpeedState.ROUND_RESULT -> {
                        Text(
                            text = "${lastReactionMs}ms",
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = getSpeedRating(lastReactionMs),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = CyberCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tap to advance to Round ${currentRound + 1}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                    SpeedState.SESSION_COMPLETE -> {
                        val best = reactionTimes.minOrNull() ?: 0L
                        val avg = if (reactionTimes.isNotEmpty()) reactionTimes.average().toInt() else 0
                        Text(
                            text = "SESSION COMPLETE",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "BEST",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                                )
                                Text(
                                    text = "${best}ms",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = CyberCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "AVERAGE",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                                )
                                Text(
                                    text = "${avg}ms",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = NeonGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "FAULTS",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                                )
                                Text(
                                    text = "$falseStartCount",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = if (falseStartCount > 0) ErrorPink else TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = getSpeedRating(avg.toLong()),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = WarningYellow,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NeonButton(
                                text = "PLAY AGAIN",
                                icon = Icons.Default.Replay,
                                accentColor = CyberCyan,
                                onClick = {
                                    currentRound = 1
                                    falseStartCount = 0
                                    reactionTimes.clear()
                                    speedState = SpeedState.IDLE
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "speed_play_again_button"
                            )
                            NeonButton(
                                text = "EXIT",
                                accentColor = NeonGreen,
                                onClick = onBack,
                                modifier = Modifier.weight(1f),
                                testTag = "speed_exit_button"
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getSpeedRating(ms: Long): String {
    return when {
        ms < 200 -> "⚡ GODLIKE REFLEXES"
        ms < 250 -> "🚀 SUPERHUMAN SPEED"
        ms < 300 -> "⚡ SHARP REFLEXES"
        ms < 360 -> "🎯 SOLID SPEED"
        else -> "🐢 WARMING UP"
    }
}
