package com.example.ludo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.audio.SoundManager
import com.example.ludo.data.AchievementEntity
import com.example.ludo.viewmodel.StatsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    statsViewModel: StatsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val achievements by statsViewModel.achievements.collectAsState()
    val unlockedCount = achievements.count { it.isUnlocked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Achievements ($unlockedCount/${achievements.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            SoundManager.playButtonClick()
                            onBack()
                        },
                        modifier = Modifier.testTag("achievements_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F111A))
            )
        },
        containerColor = Color(0xFF0F111A)
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFB300).copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ludo Trophies",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Earn medals through offline play against AI and local matches.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                        }
                    }
                }
            }

            items(achievements) { ach ->
                AchievementCard(ach)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AchievementCard(ach: AchievementEntity) {
    val icon = when (ach.iconKey) {
        "trophy" -> Icons.Default.EmojiEvents
        "swords" -> Icons.Default.SportsEsports
        "flag" -> Icons.Default.Flag
        "flame" -> Icons.Default.Whatshot
        "crown", "medal" -> Icons.Default.MilitaryTech
        else -> Icons.Default.EmojiEvents
    }

    val progressRatio = if (ach.maxProgress > 0) {
        (ach.currentProgress.toFloat() / ach.maxProgress).coerceIn(0f, 1f)
    } else 0f

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val unlockedDate = remember(ach.unlockedAt) {
        ach.unlockedAt?.let { dateFormat.format(Date(it)) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (ach.isUnlocked) Color(0xFF1F2438) else Color(0xFF161824)
        ),
        shape = RoundedCornerShape(14.dp),
        border = if (ach.isUnlocked) CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFB300).copy(alpha = 0.6f)),
            width = 1.dp
        ) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (ach.isUnlocked) Color(0xFFFFB300).copy(alpha = 0.25f)
                        else Color.Gray.copy(alpha = 0.12f)
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (ach.isUnlocked) Color(0xFFFFB300) else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ach.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (ach.isUnlocked) Color.White else Color.LightGray
                        )
                    )
                    if (ach.isUnlocked) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Unlocked",
                                tint = Color(0xFF43A047),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Unlocked",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF81C784),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Text(
                    text = ach.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (!ach.isUnlocked) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFFFB300),
                            trackColor = Color(0xFF2E3458)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${ach.currentProgress}/${ach.maxProgress}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        )
                    }
                } else if (unlockedDate != null) {
                    Text(
                        text = "Unlocked on $unlockedDate",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
