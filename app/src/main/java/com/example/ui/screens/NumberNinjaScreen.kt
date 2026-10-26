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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
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

data class MathQuestion(
    val categoryLabel: String,
    val expression: String,
    val correctAnswer: Int,
    val options: List<Int>
)

@Composable
fun NumberNinjaScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val totalQuestions = 12
    var questionIndex by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var maxStreak by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var errorCount by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    var currentQuestion by remember { mutableStateOf(generateMathQuestion(1)) }
    var timeRemainingFraction by remember { mutableFloatStateOf(1f) }

    fun loadNextQuestion() {
        if (questionIndex > totalQuestions) {
            isGameOver = true
            val accuracy = if (correctCount + errorCount > 0) {
                ((correctCount.toFloat() / (correctCount + errorCount)) * 100).toInt()
            } else 0
            val rawXp = (score / 10).coerceIn(40, 350)
            viewModel.submitGameResult(
                gameType = GameType.NUMBER_NINJA,
                score = score,
                accuracy = accuracy,
                rawXp = rawXp,
                streakCount = maxStreak
            )
            return
        }

        currentQuestion = generateMathQuestion(questionIndex)
        timeRemainingFraction = 1f
    }

    LaunchedEffect(Unit) {
        viewModel.soundManager.playGameStart()
        loadNextQuestion()
    }

    // Question Countdown Timer (5.5s per question)
    LaunchedEffect(questionIndex, isGameOver) {
        if (!isGameOver) {
            val totalSteps = 55
            val stepTime = 100L
            for (step in totalSteps downTo 0) {
                timeRemainingFraction = step.toFloat() / totalSteps
                delay(stepTime)
            }
            // Timeout
            streak = 0
            errorCount++
            viewModel.soundManager.playWrong()
            viewModel.vibrationManager.vibrateError()
            questionIndex++
            loadNextQuestion()
        }
    }

    fun handleOptionSelected(answer: Int) {
        if (isGameOver) return

        if (answer == currentQuestion.correctAnswer) {
            streak++
            if (streak > maxStreak) maxStreak = streak
            correctCount++

            val multiplier = (1 + (streak / 3)).coerceAtMost(4)
            val speedBonus = (timeRemainingFraction * 80).toInt()
            val points = (150 + speedBonus) * multiplier
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
        loadNextQuestion()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgNearBlack)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("number_ninja_screen"),
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
                modifier = Modifier.testTag("number_ninja_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "NUMBER NINJA",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = WarningYellow,
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
            barColor = if (timeRemainingFraction > 0.3f) WarningYellow else ErrorPink,
            height = 8.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Card
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
                            color = WarningYellow,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Math Problem Display
        NeonCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            borderColor = WarningYellow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(WarningYellow.copy(alpha = 0.15f))
                        .border(1.dp, WarningYellow, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = currentQuestion.categoryLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = WarningYellow,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = currentQuestion.expression,
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "= ?",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Answer Choices (4 options)
        if (!isGameOver) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    currentQuestion.options.take(2).forEach { option ->
                        NumberOptionButton(
                            value = option,
                            onClick = { handleOptionSelected(option) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    currentQuestion.options.drop(2).take(2).forEach { option ->
                        NumberOptionButton(
                            value = option,
                            onClick = { handleOptionSelected(option) },
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
                borderColor = WarningYellow
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MATH TRIUMPH!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = WarningYellow,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Score: $score • Correct: $correctCount / $totalQuestions",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                    )
                    Text(
                        text = "+${(score / 10).coerceIn(40, 350)} XP Earned",
                        style = MaterialTheme.typography.labelMedium.copy(color = NeonGreen)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NeonButton(
                            text = "REPLAY",
                            icon = Icons.Default.Replay,
                            accentColor = WarningYellow,
                            onClick = {
                                questionIndex = 1
                                score = 0
                                streak = 0
                                maxStreak = 0
                                correctCount = 0
                                errorCount = 0
                                isGameOver = false
                                loadNextQuestion()
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "ninja_replay_button"
                        )
                        NeonButton(
                            text = "EXIT",
                            accentColor = NeonGreen,
                            onClick = onBack,
                            modifier = Modifier.weight(1f),
                            testTag = "ninja_exit_button"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberOptionButton(
    value: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.5.dp, SurfaceCardBorder, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = WarningYellow),
                onClick = onClick
            )
            .testTag("math_option_$value"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$value",
            style = MaterialTheme.typography.titleLarge.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        )
    }
}

private fun generateMathQuestion(roundNumber: Int): MathQuestion {
    val qType = when {
        roundNumber % 5 == 1 -> "ADDITION"
        roundNumber % 5 == 2 -> "SUBTRACTION"
        roundNumber % 5 == 3 -> "MULTIPLICATION"
        roundNumber % 5 == 4 -> "DIVISION"
        else -> "SEQUENCE"
    }

    val (expression, answer) = when (qType) {
        "ADDITION" -> {
            val a = Random.nextInt(15, 65)
            val b = Random.nextInt(12, 58)
            Pair("$a + $b", a + b)
        }
        "SUBTRACTION" -> {
            val a = Random.nextInt(40, 99)
            val b = Random.nextInt(12, a - 5)
            Pair("$a − $b", a - b)
        }
        "MULTIPLICATION" -> {
            val a = Random.nextInt(4, 14)
            val b = Random.nextInt(3, 12)
            Pair("$a × $b", a * b)
        }
        "DIVISION" -> {
            // Strictly valid whole number division with no zero divisor!
            val divisor = Random.nextInt(2, 12)
            val quotient = Random.nextInt(3, 15)
            val dividend = divisor * quotient
            Pair("$dividend ÷ $divisor", quotient)
        }
        else -> { // Missing number sequence
            val patternType = Random.nextInt(0, 3)
            when (patternType) {
                0 -> { // Arithmetic progression
                    val start = Random.nextInt(3, 20)
                    val step = Random.nextInt(3, 9)
                    Pair("$start, ${start + step}, ${start + 2 * step}, ${start + 3 * step}, ?", start + 4 * step)
                }
                1 -> { // Geometric progression
                    val start = Random.nextInt(2, 5)
                    val mult = 2
                    Pair("$start, ${start * mult}, ${start * 4}, ${start * 8}, ?", start * 16)
                }
                else -> { // Squares
                    val base = Random.nextInt(2, 6)
                    Pair("${base * base}, ${(base + 1) * (base + 1)}, ${(base + 2) * (base + 2)}, ?", (base + 3) * (base + 3))
                }
            }
        }
    }

    // Generate 3 unique plausible distractors
    val distractors = mutableSetOf<Int>()
    distractors.add(answer)

    val offsets = listOf(-10, -5, -2, -1, 1, 2, 5, 10, -3, 3, 4, -4)
    var attempts = 0
    while (distractors.size < 4 && attempts < 40) {
        val offset = offsets.random()
        val candidate = answer + offset
        if (candidate != answer && candidate > 0) {
            distractors.add(candidate)
        }
        attempts++
    }
    // Fallback if needed
    var fallback = 1
    while (distractors.size < 4) {
        val candidate = answer + fallback
        distractors.add(candidate)
        fallback++
    }

    val shuffledOptions = distractors.shuffled(Random(System.currentTimeMillis() + roundNumber))

    return MathQuestion(
        categoryLabel = qType,
        expression = expression,
        correctAnswer = answer,
        options = shuffledOptions
    )
}
