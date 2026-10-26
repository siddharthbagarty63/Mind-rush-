package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.GameType
import com.example.model.LevelFormula
import com.example.ui.components.ArcadeProgressBar
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonCard
import com.example.ui.theme.BgNearBlack
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningYellow
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.playerStats.collectAsStateWithLifecycle()
    val dailyChallenge by viewModel.dailyChallenge.collectAsStateWithLifecycle()

    val level = stats?.currentLevel ?: 1
    val totalXp = stats?.totalXp ?: 0
    val xpForNext = LevelFormula.xpRequiredForLevel(level)
    val levelProgress = if (xpForNext > 0) totalXp.toFloat() / xpForNext else 0f
    val rankTitle = LevelFormula.getLevelTitle(level)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgNearBlack)
            .statusBarsPadding()
            .testTag("home_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header & Branding
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(NeonGreen, CyberCyan))
                            )
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(BgNearBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Mind Rush Logo",
                                tint = NeonGreen,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "MIND RUSH",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                        )
                        Text(
                            text = "OFFLINE BRAIN ARCADE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    modifier = Modifier.testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary
                    )
                }
            }
        }

        // Player Level & XP Card
        item {
            NeonCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonGreen.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonGreen)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LVL $level",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = BgNearBlack,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = rankTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Train neural circuits daily for peak reflexes",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "TOTAL XP",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                            Text(
                                text = "$totalXp / $xpForNext",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    ArcadeProgressBar(
                        progress = levelProgress,
                        barColor = NeonGreen,
                        height = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Daily Challenge Card
        item {
            val challenge = dailyChallenge
            if (challenge != null) {
                NeonCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (challenge.isCompleted) NeonGreen else WarningYellow,
                    onClick = {
                        val targetScreen = when (challenge.gameType) {
                            GameType.MEMORY_MATRIX.name -> AppScreen.MEMORY_MATRIX
                            GameType.BRAIN_SPEED.name -> AppScreen.BRAIN_SPEED
                            GameType.COLOR_CHAOS.name -> AppScreen.COLOR_CHAOS
                            else -> AppScreen.NUMBER_NINJA
                        }
                        viewModel.navigateTo(targetScreen)
                    },
                    testTag = "daily_challenge_card"
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (challenge.isCompleted) Icons.Default.CheckCircle else Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (challenge.isCompleted) NeonGreen else WarningYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DAILY CHALLENGE",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (challenge.isCompleted) NeonGreen else WarningYellow,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                            Text(
                                text = "+${challenge.xpReward} XP",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = WarningYellow,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = challenge.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = challenge.targetDescription,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (challenge.isCompleted) {
                            Text(
                                text = "COMPLETED TODAY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        } else {
                            NeonButton(
                                text = "PLAY CHALLENGE",
                                icon = Icons.Default.PlayArrow,
                                accentColor = WarningYellow,
                                onClick = {
                                    val targetScreen = when (challenge.gameType) {
                                        GameType.MEMORY_MATRIX.name -> AppScreen.MEMORY_MATRIX
                                        GameType.BRAIN_SPEED.name -> AppScreen.BRAIN_SPEED
                                        GameType.COLOR_CHAOS.name -> AppScreen.COLOR_CHAOS
                                        else -> AppScreen.NUMBER_NINJA
                                    }
                                    viewModel.navigateTo(targetScreen)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "daily_challenge_play_button"
                            )
                        }
                    }
                }
            }
        }

        // Section Title: ARCADE GAMES
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ARCADE GAMES",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "4 MODES",
                    style = MaterialTheme.typography.labelSmall.copy(color = CyberCyan)
                )
            }
        }

        // Game Card 1: Memory Matrix
        item {
            GameDashboardCard(
                title = GameType.MEMORY_MATRIX.title,
                category = GameType.MEMORY_MATRIX.category,
                description = GameType.MEMORY_MATRIX.description,
                difficulty = GameType.MEMORY_MATRIX.difficultyLabel,
                bestScore = "${stats?.memoryMatrixHighScore ?: 0} pts",
                accentColor = CyberCyan,
                icon = Icons.Default.Extension,
                onPlay = { viewModel.navigateTo(AppScreen.MEMORY_MATRIX) },
                testTag = "play_memory_matrix_card"
            )
        }

        // Game Card 2: Brain Speed
        item {
            val bestMs = stats?.brainSpeedBestMs ?: 0
            val bestDisplay = if (bestMs > 0) "${bestMs}ms" else "No Record"
            GameDashboardCard(
                title = GameType.BRAIN_SPEED.title,
                category = GameType.BRAIN_SPEED.category,
                description = GameType.BRAIN_SPEED.description,
                difficulty = GameType.BRAIN_SPEED.difficultyLabel,
                bestScore = bestDisplay,
                accentColor = NeonGreen,
                icon = Icons.Default.Speed,
                onPlay = { viewModel.navigateTo(AppScreen.BRAIN_SPEED) },
                testTag = "play_brain_speed_card"
            )
        }

        // Game Card 3: Color Chaos
        item {
            GameDashboardCard(
                title = GameType.COLOR_CHAOS.title,
                category = GameType.COLOR_CHAOS.category,
                description = GameType.COLOR_CHAOS.description,
                difficulty = GameType.COLOR_CHAOS.difficultyLabel,
                bestScore = "${stats?.colorChaosHighScore ?: 0} pts",
                accentColor = ElectricPurple,
                icon = Icons.Default.Palette,
                onPlay = { viewModel.navigateTo(AppScreen.COLOR_CHAOS) },
                testTag = "play_color_chaos_card"
            )
        }

        // Game Card 4: Number Ninja
        item {
            GameDashboardCard(
                title = GameType.NUMBER_NINJA.title,
                category = GameType.NUMBER_NINJA.category,
                description = GameType.NUMBER_NINJA.description,
                difficulty = GameType.NUMBER_NINJA.difficultyLabel,
                bestScore = "${stats?.numberNinjaHighScore ?: 0} pts",
                accentColor = WarningYellow,
                icon = Icons.Default.Bolt,
                onPlay = { viewModel.navigateTo(AppScreen.NUMBER_NINJA) },
                testTag = "play_number_ninja_card"
            )
        }
    }
}

@Composable
fun GameDashboardCard(
    title: String,
    category: String,
    description: String,
    difficulty: String,
    bestScore: String,
    accentColor: Color,
    icon: ImageVector,
    onPlay: () -> Unit,
    testTag: String
) {
    NeonCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = accentColor.copy(alpha = 0.5f),
        onClick = onPlay,
        testTag = testTag
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(1.dp, accentColor, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = category.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Best Score Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCardBorder)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "BEST",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        )
                        Text(
                            text = bestScore,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚙ $difficulty",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = BgNearBlack,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PLAY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = BgNearBlack,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
