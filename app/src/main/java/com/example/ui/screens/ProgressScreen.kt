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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.AchievementEntity
import com.example.model.LevelFormula
import com.example.ui.components.ArcadeProgressBar
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
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.playerStats.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val recentRecords by viewModel.recentRecords.collectAsStateWithLifecycle()

    val level = stats?.currentLevel ?: 1
    val totalXp = stats?.totalXp ?: 0
    val xpForNext = LevelFormula.xpRequiredForLevel(level)
    val levelProgress = if (xpForNext > 0) totalXp.toFloat() / xpForNext else 0f
    val rankTitle = LevelFormula.getLevelTitle(level)

    val unlockedCount = achievements.count { it.isUnlocked }
    val totalAchievements = achievements.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgNearBlack)
            .statusBarsPadding()
            .testTag("progress_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "NEURAL PROGRESS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "Track your career milestones, ranks and achievements",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        // Rank & Level Card
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
                            Text(
                                text = "CURRENT RANK",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                            Text(
                                text = "Level $level • $rankTitle",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonGreen.copy(alpha = 0.15f))
                                .border(1.dp, NeonGreen, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${stats?.gamesPlayed ?: 0} GAMES",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Next Rank: Level ${level + 1} (${LevelFormula.getLevelTitle(level + 1)})",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Text(
                            text = "$totalXp / $xpForNext XP",
                            style = MaterialTheme.typography.labelSmall.copy(color = CyberCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ArcadeProgressBar(
                        progress = levelProgress,
                        barColor = NeonGreen,
                        height = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Best Scores Grid
        item {
            Column {
                Text(
                    text = "PERSONAL BESTS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScoreBadge(
                        label = "MATRIX",
                        value = "${stats?.memoryMatrixHighScore ?: 0} pts",
                        color = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                    val bestSpeed = stats?.brainSpeedBestMs ?: 0
                    ScoreBadge(
                        label = "SPEED",
                        value = if (bestSpeed > 0) "${bestSpeed}ms" else "--",
                        color = NeonGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScoreBadge(
                        label = "COLOR",
                        value = "${stats?.colorChaosHighScore ?: 0} pts",
                        color = ElectricPurple,
                        modifier = Modifier.weight(1f)
                    )
                    ScoreBadge(
                        label = "NINJA",
                        value = "${stats?.numberNinjaHighScore ?: 0} pts",
                        color = WarningYellow,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Achievements Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACHIEVEMENTS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "$unlockedCount / $totalAchievements UNLOCKED",
                    style = MaterialTheme.typography.labelSmall.copy(color = WarningYellow)
                )
            }
        }

        // Achievements List
        items(achievements) { ach ->
            AchievementItemCard(ach = ach)
        }

        // Recent Games Log Header
        if (recentRecords.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "RECENT SESSIONS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            items(recentRecords.take(8)) { record ->
                RecentRecordCard(record = record)
            }
        }
    }
}

@Composable
private fun ScoreBadge(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    NeonCard(
        modifier = modifier,
        borderColor = color.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun AchievementItemCard(ach: AchievementEntity) {
    val borderColor = if (ach.isUnlocked) WarningYellow.copy(alpha = 0.6f) else SurfaceCardBorder
    NeonCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = borderColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (ach.isUnlocked) WarningYellow.copy(alpha = 0.2f) else SurfaceCardBorder
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (ach.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (ach.isUnlocked) WarningYellow else TextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ach.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = if (ach.isUnlocked) TextPrimary else TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+${ach.xpReward} XP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (ach.isUnlocked) WarningYellow else TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Text(
                    text = ach.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (ach.isUnlocked) TextSecondary else TextMuted
                    )
                )
            }

            if (ach.isUnlocked) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Unlocked",
                    tint = NeonGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun RecentRecordCard(record: com.example.data.db.GameRecordEntity) {
    val formattedDate = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(record.timestamp))
    NeonCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = record.gameType.replace("_", " "),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${record.score} pts",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "+${record.xpEarned} XP",
                    style = MaterialTheme.typography.labelSmall.copy(color = CyberCyan)
                )
            }
        }
    }
}
