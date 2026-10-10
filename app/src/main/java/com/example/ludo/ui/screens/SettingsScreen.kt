package com.example.ludo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.audio.SoundManager
import com.example.ludo.model.BoardTheme
import com.example.ludo.ui.theme.LudoThemePalettes
import com.example.ludo.viewmodel.LudoGameViewModel
import com.example.ludo.viewmodel.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    gameViewModel: LudoGameViewModel,
    statsViewModel: StatsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by gameViewModel.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Game Settings",
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
                        modifier = Modifier.testTag("settings_back_button")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Board Theme Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Visual Themes",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BoardTheme.entries.forEach { theme ->
                        val palette = LudoThemePalettes.getPalette(theme)
                        val isSelected = uiState.theme == theme

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    SoundManager.playButtonClick()
                                    gameViewModel.setTheme(theme)
                                }
                                .testTag("settings_theme_${theme.name.lowercase()}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF2E3458) else Color(0xFF1A1D2E)
                            ),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFD54F)),
                                width = 2.dp
                            ) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = theme.displayName,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color(0xFFFFD54F) else Color.White
                                    )
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(palette.redColor)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(palette.greenColor)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(palette.yellowColor)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(palette.blueColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Audio & Feedback
            item {
                Text(
                    text = "Sound & Audio",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2E)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Game Sound Effects", color = Color.White, fontSize = 14.sp)
                            Switch(
                                checked = uiState.isSoundEnabled,
                                onCheckedChange = { gameViewModel.toggleSound() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF43A047),
                                    uncheckedThumbColor = Color.LightGray,
                                    uncheckedTrackColor = Color(0xFF2E3458)
                                )
                            )
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Volume Level", color = Color.LightGray, fontSize = 12.sp)
                                Text("${(uiState.soundVolume * 100).toInt()}%", color = Color(0xFFFFD54F), fontSize = 12.sp)
                            }
                            Slider(
                                value = uiState.soundVolume,
                                onValueChange = { gameViewModel.setVolume(it) },
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFFFD54F),
                                    activeTrackColor = Color(0xFFFFD54F)
                                )
                            )
                        }
                    }
                }
            }

            // Performance & Animation
            item {
                Text(
                    text = "Animation Settings",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2E)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reduced Animations", color = Color.White, fontSize = 14.sp)
                            Text(
                                "Instant token movement for ultra fast play",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = uiState.reducedAnimations,
                            onCheckedChange = { gameViewModel.toggleReducedAnimations() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF43A047),
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = Color(0xFF2E3458)
                            )
                        )
                    }
                }
            }

            // Reset Data
            item {
                Text(
                    text = "Data Management",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("settings_reset_data_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = Color(0xFFE53935)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear All Local Data & Reset Stats", color = Color(0xFFE53935))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Confirm Data Reset") },
            text = { Text("Are you sure you want to clear all match history, achievements, and stats? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        statsViewModel.resetData()
                        showResetDialog = false
                    }
                ) { Text("Confirm Clear", color = Color(0xFFE53935)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}
