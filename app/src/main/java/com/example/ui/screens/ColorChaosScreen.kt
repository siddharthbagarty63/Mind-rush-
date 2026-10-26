package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.components.StreakBadge
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

data class ColorOption(val name: String, val color: Color)

private val COLOR_PALETTE = listOf(
    ColorOption("RED", Color(0xFFFF3366)),
    ColorOption("GREEN", Color(0xFF39FF88)),
    ColorOption("BLUE", Color(0xFF3B82F6)),
    ColorOption("YELLOW", Color(0xFFFACC15)),
    ColorOption("PURPLE", Color(0xFFA855F7)),
    ColorOption("CYAN", Color(0xFF22D3EE))
)

enum class MatchRule {
    MATCH_TEXT_COLOR,
    MATCH_WORD_NAME
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorChaosScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val totalQuestions = 15
    var questionIndex by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var maxStreak by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var errorCount by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    var currentWord by remember { mutableStateOf("BLUE") }
    var currentInkColor by remember { mutableStateOf(Color(0xFFFF3366)) }
    var currentRule by remember { mutableStateOf(MatchRule.MATCH_TEXT_COLOR) }
    var currentCorrectOptionName by remember { mutableStateOf("RED") }
    var choices by remember { mutableStateOf(COLOR_PALETTE.take(4)) }

    var timeRemainingFraction by remember { mutableFloatStateOf(1f) }

    fun nextQuestion() {
        if (questionIndex > totalQuestions) {
            isGameOver = true
            val accuracy = if (correctCount + errorCount > 0) {
                ((correctCount.toFloat() / (correctCount + errorCount)) * 100).toInt()
            } else 0
            val rawXp = (score / 8).coerceIn(40, 300)
            viewModel.submitGameResult(
                gameType = GameType.COLOR_CHAOS,
                score = score,
                accuracy = accuracy,
                rawXp = rawXp,
                streakCount = maxStreak
            )
            return
        }

        // Pick 4 color options
        val shuffledOptions = COLOR_PALETTE.shuffled()
        val chosenSet = shuffledOptions.take(4)
        choices = chosenSet

        // Decide rule: early rounds match text color, later rounds alternate
        currentRule = if (questionIndex <= 3) {
            MatchRule.MATCH_TEXT_COLOR
        } else if (Random.nextBoolean()) {
            MatchRule.MATCH_TEXT_COLOR
        } else {
            MatchRule.MATCH_WORD_NAME
        }

        val wordOption = chosenSet.random()
        val inkOption = (chosenSet - wordOption).randomOrNull() ?: chosenSet.random()

        currentWord = wordOption.name
        currentInkColor = inkOption.color

        currentCorrectOptionName = when (currentRule) {
            MatchRule.MATCH_TEXT_COLOR -> inkOption.name
            MatchRule.MATCH_WORD_NAME -> wordOption.name
        }

        timeRemainingFraction = 1f
    }

    LaunchedEffect(Unit) {
        viewModel.soundManager.playGameStart()
        nextQuestion()
    }

    // Question Timer (3.5 seconds per round)
    LaunchedEffect(questionIndex, isGameOver) {
        if (!isGameOver) {
            val totalSteps = 35
            val stepTime = 100L
            for (step in totalSteps downTo 0) {
                timeRemainingFraction = step.toFloat() / totalSteps
                delay(stepTime)
            }
            // Timeout counts as error
            streak = 0
            errorCount++
            viewModel.soundManager.playWrong()
            viewModel.vibrationManager.vibrateError()
            questionIndex++
            nextQuestion()
        }
    }

    fun handleAnswer(selectedName: String) {
        if (isGameOver) return

        if (selectedName == currentCorrectOptionName) {
            streak++
            if (streak > maxStreak) maxStreak = streak
            correctCount++

            val streakMultiplier = (1 + (streak / 3)).coerceAtMost(4)
            val timeBonus = (timeRemainingFraction * 50).toInt()
            val points = (100 + timeBonus) * streakMultiplier
            score += points

            viewModel.soundManager.playCorrect()
            viewModel.vibrationManager.vibrateSuccess()
        } else {
            streak = 0
            errorCount++
            viewModel.soundManager.playWrong()
            viewModel.vibrationManager.vibrateError()
        }

        questionIndex++
        nextQuestion()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgNearBlack)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("color_chaos_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("color_chaos_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "COLOR CHAOS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = ElectricPurple,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "QUESTION ${questionIndex.coerceAtMost(totalQuestions)} / $totalQuestions",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            StreakBadge(streak = streak)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timer Bar
        ArcadeProgressBar(
            progress = timeRemainingFraction,
            barColor = if (timeRemainingFraction > 0.3f) CyberCyan else ErrorPink,
            height = 8.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Score Card
        NeonCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "SCORE", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "ACCURACY", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                    val acc = if (correctCount + errorCount > 0) {
                        ((correctCount.toFloat() / (correctCount + errorCount)) * 100).toInt()
                    } else 100
                    Text(
                        text = "$acc%",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stroop Challenge Presentation Card
        NeonCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            borderColor = if (currentRule == MatchRule.MATCH_TEXT_COLOR) ElectricPurple else CyberCyan
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Rule Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (currentRule == MatchRule.MATCH_TEXT_COLOR)
                                ElectricPurple.copy(alpha = 0.2f)
                            else CyberCyan.copy(alpha = 0.2f)
                        )
                        .border(
                            1.dp,
                            if (currentRule == MatchRule.MATCH_TEXT_COLOR) ElectricPurple else CyberCyan,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (currentRule == MatchRule.MATCH_TEXT_COLOR)
                            "SELECT INK / TEXT COLOR"
                        else
                            "SELECT WORD MEANING",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (currentRule == MatchRule.MATCH_TEXT_COLOR) ElectricPurple else CyberCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                // The Stroop word
                Text(
                    text = currentWord,
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = currentInkColor,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Do not be deceived by the font color!",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Choice Buttons in 2x2 Grid
        if (!isGameOver) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    choices.take(2).forEach { option ->
                        ColorChoiceButton(
                            option = option,
                            onClick = { handleAnswer(option.name) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    choices.drop(2).take(2).forEach { option ->
                        ColorChoiceButton(
                            option = option,
                            onClick = { handleAnswer(option.name) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Game Over Panel
        AnimatedVisibility(visible = isGameOver) {
            NeonCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonGreen
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CHAOS SURVIVED!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Final Score: $score • Max Streak: ${maxStreak}x",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                    )
                    Text(
                        text = "Correct: $correctCount / $totalQuestions",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                    Text(
                        text = "+${(score / 8).coerceIn(40, 300)} XP Earned",
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
                                questionIndex = 1
                                score = 0
                                streak = 0
                                maxStreak = 0
                                correctCount = 0
                                errorCount = 0
                                isGameOver = false
                                nextQuestion()
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "color_replay_button"
                        )
                        NeonButton(
                            text = "EXIT",
                            accentColor = NeonGreen,
                            onClick = onBack,
                            modifier = Modifier.weight(1f),
                            testTag = "color_exit_button"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorChoiceButton(
    option: ColorOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.5.dp, option.color, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = option.color),
                onClick = onClick
            )
            .testTag("color_choice_${option.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(option.color)
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = option.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}
