package com.example.ludo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ludo.audio.SoundManager
import com.example.ludo.model.GameMode

@Composable
fun HomeScreen(
    onSelectMode: (GameMode) -> Unit,
    onQuickPlay: () -> Unit,
    onOpenRules: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenThemes: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF0F111A)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HeroHeaderView()
            }

            item {
                Button(
                    onClick = {
                        SoundManager.playButtonClick()
                        onQuickPlay()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .testTag("quick_play_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quick Game",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    )
                }
            }

            item {
                Text(
                    text = "Game Modes",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                GameModeCard(
                    title = GameMode.VS_COMPUTER.title,
                    description = "Challenge intelligent AI with Easy, Medium, Hard, or Expert tacticians.",
                    icon = Icons.Default.Computer,
                    accentColor = Color(0xFF1E88E5),
                    tag = "mode_vs_computer",
                    onClick = {
                        SoundManager.playButtonClick()
                        onSelectMode(GameMode.VS_COMPUTER)
                    }
                )
            }

            item {
                GameModeCard(
                    title = GameMode.PASS_AND_PLAY.title,
                    description = "Share this device with 2 to 4 friends and family offline.",
                    icon = Icons.Default.Group,
                    accentColor = Color(0xFF43A047),
                    tag = "mode_pass_and_play",
                    onClick = {
                        SoundManager.playButtonClick()
                        onSelectMode(GameMode.PASS_AND_PLAY)
                    }
                )
            }

            item {
                GameModeCard(
                    title = GameMode.COMPUTER_ONLY.title,
                    description = "Watch AI bots battle with pause and up to 4x fast-forward speed.",
                    icon = Icons.Default.SmartDisplay,
                    accentColor = Color(0xFFFDD835),
                    tag = "mode_computer_only",
                    onClick = {
                        SoundManager.playButtonClick()
                        onSelectMode(GameMode.COMPUTER_ONLY)
                    }
                )
            }

            item {
                GameModeCard(
                    title = GameMode.TEAM_MODE.title,
                    description = "2v2 alliance — Red & Yellow vs Green & Blue. Team up to win!",
                    icon = Icons.Default.Group,
                    accentColor = Color(0xFFE91E63),
                    tag = "mode_team",
                    onClick = {
                        SoundManager.playButtonClick()
                        onSelectMode(GameMode.TEAM_MODE)
                    }
                )
            }

            item {
                GameModeCard(
                    title = GameMode.ARCADE.title,
                    description = "Power-ups, turn timer & special dice. High-energy Ludo!",
                    icon = Icons.Default.Casino,
                    accentColor = Color(0xFFFF6D00),
                    tag = "mode_arcade",
                    onClick = {
                        SoundManager.playButtonClick()
                        onSelectMode(GameMode.ARCADE)
                    }
                )
            }

            item {
                Text(
                    text = "Explore & Records",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NavTile(
                        title = "Statistics",
                        icon = Icons.Default.Insights,
                        color = Color(0xFF26A69A),
                        tag = "nav_statistics",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            SoundManager.playButtonClick()
                            onOpenStats()
                        }
                    )
                    NavTile(
                        title = "Achievements",
                        icon = Icons.Default.EmojiEvents,
                        color = Color(0xFFFFB300),
                        tag = "nav_achievements",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            SoundManager.playButtonClick()
                            onOpenAchievements()
                        }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NavTile(
                        title = "Themes",
                        icon = Icons.Default.Palette,
                        color = Color(0xFFAB47BC),
                        tag = "nav_themes",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            SoundManager.playButtonClick()
                            onOpenThemes()
                        }
                    )
                    NavTile(
                        title = "Rules & Guide",
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        color = Color(0xFF42A5F5),
                        tag = "nav_rules",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            SoundManager.playButtonClick()
                            onOpenRules()
                        }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NavTile(
                        title = "Settings",
                        icon = Icons.Default.Settings,
                        color = Color(0xFF78909C),
                        tag = "nav_settings",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            SoundManager.playButtonClick()
                            onOpenSettings()
                        }
                    )
                    NavTile(
                        title = "About",
                        icon = Icons.Default.Info,
                        color = Color(0xFF8D6E63),
                        tag = "nav_about",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            SoundManager.playButtonClick()
                            onOpenAbout()
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun HeroHeaderView() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2E))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF2E3458), Color(0xFF141724)),
                        radius = 800f
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFE53935)))
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF43A047)))
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFDD835)))
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF1E88E5)))
                        Text(
                            text = "OFFLINE EDITION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ludo Kingdom",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 28.sp
                        )
                    )
                    Text(
                        text = "Pure Classic Strategy • 100% Offline",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.LightGray.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF101322))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ludo_launcher_icon_1791624512920),
                        contentDescription = "App Icon",
                        modifier = Modifier.size(68.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GameModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.2f))
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                Text(text = description, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 12.sp))
            }
        }
    }
}

@Composable
private fun NavTile(
    title: String,
    icon: ImageVector,
    color: Color,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).clickable { onClick() }.testTag(tag),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2E))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(42.dp).clip(CircleShape).background(color.copy(alpha = 0.2f))
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontSize = 13.sp
                )
            )
        }
    }
}
