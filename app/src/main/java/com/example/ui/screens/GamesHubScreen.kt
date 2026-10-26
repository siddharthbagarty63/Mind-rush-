package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.GameType
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
fun GamesHubScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.playerStats.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filterOptions = listOf("ALL", "MEMORY", "SPEED", "ATTENTION", "MATH")

    val games = listOf(
        GameType.MEMORY_MATRIX,
        GameType.BRAIN_SPEED,
        GameType.COLOR_CHAOS,
        GameType.NUMBER_NINJA
    )

    val filteredGames = games.filter { game ->
        when (selectedFilter) {
            "MEMORY" -> game == GameType.MEMORY_MATRIX
            "SPEED" -> game == GameType.BRAIN_SPEED
            "ATTENTION" -> game == GameType.COLOR_CHAOS
            "MATH" -> game == GameType.NUMBER_NINJA
            else -> true
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgNearBlack)
            .statusBarsPadding()
            .testTag("games_hub_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "ARCADE VAULT",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "Select a neural training game to test your limits",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterOptions) { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) NeonGreen else SurfaceCard)
                            .border(
                                1.dp,
                                if (isSelected) NeonGreen else SurfaceCardBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                selectedFilter = filter
                                viewModel.soundManager.playTap()
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("filter_chip_$filter"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) BgNearBlack else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        items(filteredGames) { game ->
            when (game) {
                GameType.MEMORY_MATRIX -> {
                    GameDashboardCard(
                        title = game.title,
                        category = game.category,
                        description = game.description,
                        difficulty = game.difficultyLabel,
                        bestScore = "${stats?.memoryMatrixHighScore ?: 0} pts",
                        accentColor = CyberCyan,
                        icon = Icons.Default.Extension,
                        onPlay = { viewModel.navigateTo(AppScreen.MEMORY_MATRIX) },
                        testTag = "games_hub_play_memory"
                    )
                }
                GameType.BRAIN_SPEED -> {
                    val bestMs = stats?.brainSpeedBestMs ?: 0
                    val bestDisplay = if (bestMs > 0) "${bestMs}ms" else "No Record"
                    GameDashboardCard(
                        title = game.title,
                        category = game.category,
                        description = game.description,
                        difficulty = game.difficultyLabel,
                        bestScore = bestDisplay,
                        accentColor = NeonGreen,
                        icon = Icons.Default.Speed,
                        onPlay = { viewModel.navigateTo(AppScreen.BRAIN_SPEED) },
                        testTag = "games_hub_play_speed"
                    )
                }
                GameType.COLOR_CHAOS -> {
                    GameDashboardCard(
                        title = game.title,
                        category = game.category,
                        description = game.description,
                        difficulty = game.difficultyLabel,
                        bestScore = "${stats?.colorChaosHighScore ?: 0} pts",
                        accentColor = ElectricPurple,
                        icon = Icons.Default.Palette,
                        onPlay = { viewModel.navigateTo(AppScreen.COLOR_CHAOS) },
                        testTag = "games_hub_play_color"
                    )
                }
                GameType.NUMBER_NINJA -> {
                    GameDashboardCard(
                        title = game.title,
                        category = game.category,
                        description = game.description,
                        difficulty = game.difficultyLabel,
                        bestScore = "${stats?.numberNinjaHighScore ?: 0} pts",
                        accentColor = WarningYellow,
                        icon = Icons.Default.Bolt,
                        onPlay = { viewModel.navigateTo(AppScreen.NUMBER_NINJA) },
                        testTag = "games_hub_play_ninja"
                    )
                }
            }
        }
    }
}
