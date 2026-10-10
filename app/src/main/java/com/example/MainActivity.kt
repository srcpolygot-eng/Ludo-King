package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ludo.model.BoardTheme
import com.example.ludo.model.GameMode
import com.example.ludo.model.GameRules
import com.example.ludo.ui.screens.AboutScreen
import com.example.ludo.ui.screens.AchievementsScreen
import com.example.ludo.ui.screens.GameScreen
import com.example.ludo.ui.screens.HomeScreen
import com.example.ludo.ui.screens.RulesScreen
import com.example.ludo.ui.screens.SettingsScreen
import com.example.ludo.ui.screens.SetupScreen
import com.example.ludo.ui.screens.StatisticsScreen
import com.example.ludo.viewmodel.LudoGameViewModel
import com.example.ludo.viewmodel.StatsViewModel
import com.example.ui.theme.MyApplicationTheme

enum class Screen {
    HOME,
    SETUP,
    GAME,
    RULES,
    STATISTICS,
    ACHIEVEMENTS,
    SETTINGS,
    ABOUT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    LudoAppRoot(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LudoAppRoot(
    modifier: Modifier = Modifier,
    gameViewModel: LudoGameViewModel = viewModel(),
    statsViewModel: StatsViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var selectedModeForSetup by remember { mutableStateOf(GameMode.VS_COMPUTER) }

    val gameState by gameViewModel.uiState.collectAsState()

    // Back handling for navigation
    BackHandler(enabled = currentScreen != Screen.HOME) {
        when (currentScreen) {
            Screen.GAME -> {
                if (!gameState.isPaused && gameState.winner == null) {
                    gameViewModel.togglePause()
                } else {
                    currentScreen = Screen.HOME
                }
            }
            else -> {
                currentScreen = Screen.HOME
            }
        }
    }

    when (currentScreen) {
        Screen.HOME -> {
            HomeScreen(
                onSelectMode = { mode ->
                    selectedModeForSetup = mode
                    currentScreen = Screen.SETUP
                },
                onQuickPlay = {
                    gameViewModel.startNewGame(
                        mode = GameMode.QUICK_GAME,
                        playerCount = 4,
                        humanPlayerName = "Player 1",
                        theme = gameState.theme,
                        rules = GameRules()
                    )
                    currentScreen = Screen.GAME
                },
                onOpenRules = { currentScreen = Screen.RULES },
                onOpenStats = { currentScreen = Screen.STATISTICS },
                onOpenAchievements = { currentScreen = Screen.ACHIEVEMENTS },
                onOpenThemes = { currentScreen = Screen.SETTINGS },
                onOpenSettings = { currentScreen = Screen.SETTINGS },
                onOpenAbout = { currentScreen = Screen.ABOUT },
                modifier = modifier
            )
        }

        Screen.SETUP -> {
            SetupScreen(
                gameMode = selectedModeForSetup,
                onBack = { currentScreen = Screen.HOME },
                onStartGame = { count, humanName, difficulty, theme, rules, customNames ->
                    gameViewModel.startNewGame(
                        mode = selectedModeForSetup,
                        playerCount = count,
                        humanPlayerName = humanName,
                        aiDifficulty = difficulty,
                        theme = theme,
                        rules = rules,
                        customPlayerNames = customNames
                    )
                    currentScreen = Screen.GAME
                },
                modifier = modifier
            )
        }

        Screen.GAME -> {
            GameScreen(
                viewModel = gameViewModel,
                onExitToHome = { currentScreen = Screen.HOME },
                modifier = modifier
            )
        }

        Screen.RULES -> {
            RulesScreen(
                onBack = { currentScreen = Screen.HOME },
                modifier = modifier
            )
        }

        Screen.STATISTICS -> {
            StatisticsScreen(
                statsViewModel = statsViewModel,
                onBack = { currentScreen = Screen.HOME },
                modifier = modifier
            )
        }

        Screen.ACHIEVEMENTS -> {
            AchievementsScreen(
                statsViewModel = statsViewModel,
                onBack = { currentScreen = Screen.HOME },
                modifier = modifier
            )
        }

        Screen.SETTINGS -> {
            SettingsScreen(
                gameViewModel = gameViewModel,
                statsViewModel = statsViewModel,
                onBack = { currentScreen = Screen.HOME },
                modifier = modifier
            )
        }

        Screen.ABOUT -> {
            AboutScreen(
                onBack = { currentScreen = Screen.HOME },
                modifier = modifier
            )
        }
    }
}
