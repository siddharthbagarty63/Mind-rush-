package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.components.ArcadeProgressBar
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonCard
import com.example.ui.theme.BgNearBlack
import com.example.ui.theme.CyberCyan
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

private enum class MatrixPhase {
    READY,
    MEMORIZE,
    RECALL,
    ROUND_SUCCESS,
    GAME_OVER
}

@Composable
fun MemoryMatrixScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var round by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var phase by remember { mutableStateOf(MatrixPhase.READY) }
    var memorizeProgress by remember { mutableStateOf(1f) }

    // Grid size depends on round
    val gridSize = when {
        round <= 2 -> 3
        round <= 5 -> 4
        round <= 8 -> 5
        else -> 6
    }
    val targetCount = when {
        round == 1 -> 3
        round == 2 -> 4
        round <= 4 -> 5
        round <= 6 -> 6
        round <= 8 -> 7
        else -> 8
    }

    val totalCells = gridSize * gridSize
    val targetIndices = remember { mutableStateListOf<Int>() }
    val correctlySelected = remember { mutableStateListOf<Int>() }
    val wrongSelected = remember { mutableStateListOf<Int>() }
    var totalRoundsCleared by remember { mutableIntStateOf(0) }
    var totalMistakes by remember { mutableIntStateOf(0) }

    fun startNewRound() {
        targetIndices.clear()
        correctlySelected.clear()
        wrongSelected.clear()

        // Pick distinct target indices
        val pool = (0 until totalCells).toMutableList()
        pool.shuffle(Random(System.currentTimeMillis()))
        targetIndices.addAll(pool.take(targetCount))

        phase = MatrixPhase.READY
    }

    LaunchedEffect(Unit) {
        viewModel.soundManager.playGameStart()
        startNewRound()
    }

    // Sequence controller
    LaunchedEffect(phase, round) {
        if (phase == MatrixPhase.READY) {
            delay(500)
            phase = MatrixPhase.MEMORIZE
        } else if (phase == MatrixPhase.MEMORIZE) {
            val durationMs = (1600 + targetCount * 180).toLong()
            val steps = 30
            val interval = durationMs / steps
            for (i in steps downTo 0) {
                memorizeProgress = i.toFloat() / steps
                delay(interval)
            }
            phase = MatrixPhase.RECALL
        } else if (phase == MatrixPhase.ROUND_SUCCESS) {
            viewModel.soundManager.playCorrect()
            viewModel.vibrationManager.vibrateSuccess()
            totalRoundsCleared++
            val roundBonus = 100 * round + (lives * 30)
            score += roundBonus
            delay(800)
            round++
            startNewRound()
        }
    }

    fun handleCellClick(index: Int) {
        if (phase != MatrixPhase.RECALL) return
        if (correctlySelected.contains(index) || wrongSelected.contains(index)) return

        if (targetIndices.contains(index)) {
            correctlySelected.add(index)
            viewModel.soundManager.playTap()
            viewModel.vibrationManager.vibrateTap()

            // Check if all found
            if (correctlySelected.size == targetIndices.size) {
                phase = MatrixPhase.ROUND_SUCCESS
            }
        } else {
            wrongSelected.add(index)
            totalMistakes++
            lives--
            viewModel.soundManager.playWrong()
            viewModel.vibrationManager.vibrateError()

            if (lives <= 0) {
                phase = MatrixPhase.GAME_OVER
                val earnedXp = (score / 10).coerceAtLeast(30)
                val accuracy = if (totalRoundsCleared + totalMistakes > 0) {
                    ((totalRoundsCleared.toFloat() / (totalRoundsCleared + totalMistakes)) * 100).toInt()
                } else 0
                viewModel.submitGameResult(
                    gameType = GameType.MEMORY_MATRIX,
                    score = score,
                    accuracy = accuracy,
                    rawXp = earnedXp
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgNearBlack)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("memory_matrix_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "MEMORY MATRIX",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "ROUND $round • ${gridSize}×${gridSize} GRID",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            // Lives indicators
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { index ->
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Life",
                        tint = if (index < lives) ErrorPink else SurfaceCardBorder,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(horizontal = 1.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Card
        NeonCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (phase == MatrixPhase.MEMORIZE) CyberCyan else SurfaceCardBorder
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SCORE",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TARGETS",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                        Text(
                            text = "${correctlySelected.size} / $targetCount",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val statusText = when (phase) {
                    MatrixPhase.READY -> "GET READY..."
                    MatrixPhase.MEMORIZE -> "MEMORIZE PATTERN!"
                    MatrixPhase.RECALL -> "REPRODUCE PATTERN"
                    MatrixPhase.ROUND_SUCCESS -> "ROUND COMPLETE!"
                    MatrixPhase.GAME_OVER -> "SYSTEM OVERLOAD"
                }
                val statusColor = when (phase) {
                    MatrixPhase.MEMORIZE -> CyberCyan
                    MatrixPhase.RECALL -> NeonGreen
                    MatrixPhase.ROUND_SUCCESS -> WarningYellow
                    MatrixPhase.GAME_OVER -> ErrorPink
                    else -> TextSecondary
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                )

                if (phase == MatrixPhase.MEMORIZE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ArcadeProgressBar(
                        progress = memorizeProgress,
                        barColor = CyberCyan,
                        modifier = Modifier.fillMaxWidth(),
                        height = 6.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Matrix Grid View
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val sideLength = minOf(maxWidth, maxHeight)

            Box(
                modifier = Modifier
                    .size(sideLength)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .border(1.5.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (row in 0 until gridSize) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (col in 0 until gridSize) {
                                val cellIndex = row * gridSize + col
                                val isTarget = targetIndices.contains(cellIndex)
                                val isCorrect = correctlySelected.contains(cellIndex)
                                val isWrong = wrongSelected.contains(cellIndex)

                                val cellBgColor by animateColorAsState(
                                    targetValue = when {
                                        phase == MatrixPhase.MEMORIZE && isTarget -> CyberCyan
                                        isCorrect -> NeonGreen
                                        isWrong -> ErrorPink
                                        else -> BgNearBlack
                                    },
                                    animationSpec = tween(150),
                                    label = "cell_color"
                                )

                                val cellBorderColor = when {
                                    phase == MatrixPhase.MEMORIZE && isTarget -> CyberCyan
                                    isCorrect -> NeonGreen
                                    isWrong -> ErrorPink
                                    else -> SurfaceCardBorder
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(cellBgColor)
                                        .border(1.dp, cellBorderColor, RoundedCornerShape(8.dp))
                                        .testTag("matrix_tile_${row}_${col}")
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(color = NeonGreen),
                                            enabled = phase == MatrixPhase.RECALL && !isCorrect && !isWrong,
                                            onClick = { handleCellClick(cellIndex) }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCorrect) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(BgNearBlack)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Game Over Panel
        AnimatedVisibility(visible = phase == MatrixPhase.GAME_OVER) {
            NeonCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = ErrorPink
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GAME OVER",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = ErrorPink,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Final Score: $score • Rounds Cleared: $totalRoundsCleared",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                    )
                    Text(
                        text = "+${(score / 10).coerceAtLeast(30)} XP Awarded",
                        style = MaterialTheme.typography.labelMedium.copy(color = WarningYellow)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NeonButton(
                            text = "REPLAY",
                            icon = Icons.Default.Replay,
                            accentColor = CyberCyan,
                            onClick = {
                                round = 1
                                score = 0
                                lives = 3
                                totalRoundsCleared = 0
                                totalMistakes = 0
                                startNewRound()
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "memory_matrix_replay_button"
                        )
                        NeonButton(
                            text = "EXIT",
                            accentColor = NeonGreen,
                            onClick = onBack,
                            modifier = Modifier.weight(1f),
                            testTag = "memory_matrix_exit_button"
                        )
                    }
                }
            }
        }
    }
}
