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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.audio.SoundManager
import com.example.ludo.model.AiDifficulty
import com.example.ludo.model.BoardTheme
import com.example.ludo.model.GameMode
import com.example.ludo.model.GameRules
import com.example.ludo.ui.theme.LudoThemePalettes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    gameMode: GameMode,
    onBack: () -> Unit,
    onStartGame: (
        playerCount: Int,
        humanPlayerName: String,
        difficulty: AiDifficulty,
        theme: BoardTheme,
        rules: GameRules,
        customNames: List<String>
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var playerCount by remember { mutableIntStateOf(4) }
    var humanName by remember { mutableStateOf("Player 1") }
    var p2Name by remember { mutableStateOf("Player 2") }
    var p3Name by remember { mutableStateOf("Player 3") }
    var p4Name by remember { mutableStateOf("Player 4") }

    var selectedDifficulty by remember { mutableStateOf(AiDifficulty.MEDIUM) }
    var selectedTheme by remember { mutableStateOf(BoardTheme.CLASSIC) }

    var extraTurnOnSix by remember { mutableStateOf(true) }
    var extraTurnOnCapture by remember { mutableStateOf(true) }
    var extraTurnOnHome by remember { mutableStateOf(true) }
    var threeSixesForfeit by remember { mutableStateOf(true) }
    var safeSquaresEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Match Setup: ${gameMode.title}",
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
                        modifier = Modifier.testTag("setup_back_button")
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Player Count Selection
            item {
                Text(
                    text = "Number of Players",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(2, 3, 4).forEach { count ->
                        val isSelected = playerCount == count
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFFE53935) else Color(0xFF1A1D2E))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFFE53935) else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    SoundManager.playButtonClick()
                                    playerCount = count
                                }
                                .testTag("player_count_$count")
                        ) {
                            Text(
                                text = "$count Players",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color.LightGray
                                )
                            )
                        }
                    }
                }
            }

            // AI Difficulty (for VS_COMPUTER or COMPUTER_ONLY)
            if (gameMode == GameMode.VS_COMPUTER || gameMode == GameMode.COMPUTER_ONLY) {
                item {
                    Text(
                        text = "AI Difficulty",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AiDifficulty.entries.forEach { diff ->
                            val isSelected = selectedDifficulty == diff
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        SoundManager.playButtonClick()
                                        selectedDifficulty = diff
                                    }
                                    .testTag("difficulty_${diff.name.lowercase()}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF1E88E5).copy(alpha = 0.25f) else Color(0xFF1A1D2E)
                                ),
                                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E88E5)),
                                    width = 2.dp
                                ) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = diff.label,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color(0xFF90CAF9) else Color.White
                                            )
                                        )
                                        Text(
                                            text = diff.description,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color.Gray,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFF1E88E5) else Color.Transparent)
                                            .border(2.dp, if (isSelected) Color(0xFF1E88E5) else Color.Gray, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Player Names
            item {
                Text(
                    text = if (gameMode == GameMode.PASS_AND_PLAY) "Player Names" else "Your Name",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = humanName,
                    onValueChange = { humanName = it },
                    label = { Text("Player 1 (Red)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_name_1_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFE53935),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                if (gameMode == GameMode.PASS_AND_PLAY) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = p2Name,
                        onValueChange = { p2Name = it },
                        label = { Text("Player 2 (${if (playerCount == 2) "Yellow" else "Green"})") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_name_2_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF43A047),
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    if (playerCount >= 3) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = p3Name,
                            onValueChange = { p3Name = it },
                            label = { Text("Player 3 (Yellow)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("player_name_3_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFDD835),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }

                    if (playerCount >= 4) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = p4Name,
                            onValueChange = { p4Name = it },
                            label = { Text("Player 4 (Blue)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("player_name_4_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF1E88E5),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }
                }
            }

            // Board Theme Selection
            item {
                Text(
                    text = "Board Visual Theme",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BoardTheme.entries.forEach { theme ->
                        val palette = LudoThemePalettes.getPalette(theme)
                        val isSelected = selectedTheme == theme
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1A1D2E))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFFFD54F) else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    SoundManager.playButtonClick()
                                    selectedTheme = theme
                                }
                                .padding(vertical = 10.dp)
                                .testTag("theme_chip_${theme.name.lowercase()}")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(palette.redColor)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(palette.greenColor)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(palette.blueColor)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = theme.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color(0xFFFFD54F) else Color.LightGray,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Rules Configuration
            item {
                Text(
                    text = "Rules & Variations",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2E)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RuleToggleRow(
                            title = "Bonus turn on rolling a 6",
                            checked = extraTurnOnSix,
                            onCheckedChange = { extraTurnOnSix = it }
                        )
                        RuleToggleRow(
                            title = "Bonus turn on capturing opponent",
                            checked = extraTurnOnCapture,
                            onCheckedChange = { extraTurnOnCapture = it }
                        )
                        RuleToggleRow(
                            title = "Bonus turn on reaching home",
                            checked = extraTurnOnHome,
                            onCheckedChange = { extraTurnOnHome = it }
                        )
                        RuleToggleRow(
                            title = "Three 6s forfeit turn rule",
                            checked = threeSixesForfeit,
                            onCheckedChange = { threeSixesForfeit = it }
                        )
                        RuleToggleRow(
                            title = "Safe squares protection",
                            checked = safeSquaresEnabled,
                            onCheckedChange = { safeSquaresEnabled = it }
                        )
                    }
                }
            }

            // Launch Button
            item {
                Button(
                    onClick = {
                        SoundManager.playButtonClick()
                        val customNames = listOf(humanName, p2Name, p3Name, p4Name)
                        val rules = GameRules(
                            extraTurnOnSix = extraTurnOnSix,
                            extraTurnOnCapture = extraTurnOnCapture,
                            extraTurnOnHome = extraTurnOnHome,
                            threeConsecutiveSixesForfeit = threeSixesForfeit,
                            safeSquaresEnabled = safeSquaresEnabled
                        )
                        onStartGame(
                            playerCount,
                            humanName,
                            selectedDifficulty,
                            selectedTheme,
                            rules,
                            customNames
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_match_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Match",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RuleToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontSize = 13.sp
            ),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = {
                SoundManager.playButtonClick()
                onCheckedChange(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF43A047),
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = Color(0xFF2E3458)
            )
        )
    }
}
