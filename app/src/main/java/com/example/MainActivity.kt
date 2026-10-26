package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AchievementUnlockBanner
import com.example.ui.components.LevelUpDialog
import com.example.ui.screens.BrainSpeedScreen
import com.example.ui.screens.ColorChaosScreen
import com.example.ui.screens.GamesHubScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoryMatrixScreen
import com.example.ui.screens.NumberNinjaScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BgNearBlack
import com.example.ui.theme.BgNavyDark
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainContent(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val levelUpPopup by viewModel.levelUpPopup.collectAsStateWithLifecycle()
    val achievementBanner by viewModel.achievementBanner.collectAsStateWithLifecycle()

    val isGameplayScreen = currentScreen in listOf(
        AppScreen.MEMORY_MATRIX,
        AppScreen.BRAIN_SPEED,
        AppScreen.COLOR_CHAOS,
        AppScreen.NUMBER_NINJA
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BgNearBlack),
        containerColor = BgNearBlack,
        bottomBar = {
            if (!isGameplayScreen) {
                BottomArcadeNavigation(
                    currentScreen = currentScreen,
                    onSelectScreen = { viewModel.navigateTo(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentScreen,
                label = "screen_crossfade"
            ) { screen ->
                when (screen) {
                    AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                    AppScreen.GAMES_HUB -> GamesHubScreen(viewModel = viewModel)
                    AppScreen.PROGRESS -> ProgressScreen(viewModel = viewModel)
                    AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    AppScreen.MEMORY_MATRIX -> MemoryMatrixScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                    AppScreen.BRAIN_SPEED -> BrainSpeedScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                    AppScreen.COLOR_CHAOS -> ColorChaosScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                    AppScreen.NUMBER_NINJA -> NumberNinjaScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
            }

            // Global Overlay: Achievement Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                AchievementUnlockBanner(
                    title = achievementBanner.title,
                    description = achievementBanner.description,
                    visible = achievementBanner.isVisible,
                    onDismiss = { viewModel.dismissAchievementBanner() }
                )
            }

            // Global Overlay: Level Up Dialog
            if (levelUpPopup.isVisible) {
                LevelUpDialog(
                    newLevel = levelUpPopup.newLevel,
                    levelTitle = levelUpPopup.levelTitle,
                    onDismiss = { viewModel.dismissLevelUpPopup() }
                )
            }
        }
    }
}

private data class NavItem(
    val screen: AppScreen,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
private fun BottomArcadeNavigation(
    currentScreen: AppScreen,
    onSelectScreen: (AppScreen) -> Unit
) {
    val navItems = listOf(
        NavItem(AppScreen.HOME, "Home", Icons.Default.Home, "nav_home"),
        NavItem(AppScreen.GAMES_HUB, "Games", Icons.Default.SportsEsports, "nav_games"),
        NavItem(AppScreen.PROGRESS, "Progress", Icons.Default.Leaderboard, "nav_progress"),
        NavItem(AppScreen.SETTINGS, "Settings", Icons.Default.Settings, "nav_settings")
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = SurfaceCardBorder)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_nav_bar"),
        containerColor = BgNavyDark,
        tonalElevation = 8.dp
    ) {
        navItems.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectScreen(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) BgNearBlack else TextMuted
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) NeonGreen else TextMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = NeonGreen,
                    selectedIconColor = BgNearBlack,
                    unselectedIconColor = TextMuted,
                    selectedTextColor = NeonGreen,
                    unselectedTextColor = TextMuted
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
