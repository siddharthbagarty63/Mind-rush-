package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.playerStats.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }

    val soundEnabled = stats?.soundEnabled ?: true
    val hapticsEnabled = stats?.hapticsEnabled ?: true

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgNearBlack)
            .statusBarsPadding()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "SETTINGS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "Audio, haptics and game data configuration",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        // Audio & Haptics Section
        item {
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "FEEDBACK & EFFECTS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingToggleRow(
                        title = "Sound Effects",
                        subtitle = "Synthesized retro neon arcade sound tones",
                        icon = Icons.Default.VolumeUp,
                        isChecked = soundEnabled,
                        onCheckedChange = { viewModel.toggleSound(it) },
                        testTag = "sound_toggle"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingToggleRow(
                        title = "Haptic Vibration",
                        subtitle = "Tactile response for taps, wins and mistakes",
                        icon = Icons.Default.Vibration,
                        isChecked = hapticsEnabled,
                        onCheckedChange = { viewModel.toggleHaptics(it) },
                        testTag = "haptics_toggle"
                    )
                }
            }
        }

        // About & Offline Info Card
        item {
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OFFLINE ARCHITECTURE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Mind Rush — Offline Brain Arcade is completely self-contained. All game engines, high scores, levels, achievements, and daily challenges operate 100% offline with zero cloud telemetry or advertisements.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 20.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "App Version: 1.0.0 • Offline Build",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                }
            }
        }

        // Reset Data Danger Zone
        item {
            NeonCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = ErrorPink.copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "DANGER ZONE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ErrorPink,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Reset all player XP, levels, game records, and locked achievements back to zero.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    NeonButton(
                        text = "RESET ALL PROGRESS",
                        icon = Icons.Default.DeleteForever,
                        accentColor = ErrorPink,
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "reset_progress_button"
                    )
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Reset All Progress?",
                    style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "This will permanently wipe your career XP, personal best scores, and unlocked achievements.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAllData()
                        showResetDialog = false
                    },
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text(text = "RESET", color = ErrorPink, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false },
                    modifier = Modifier.testTag("cancel_reset_button")
                ) {
                    Text(text = "CANCEL", color = TextSecondary)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCardBorder),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isChecked) NeonGreen else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BgNearBlack,
                checkedTrackColor = NeonGreen,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = SurfaceCardBorder
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
