package com.example.ludo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ludo.data.AchievementEntity
import com.example.ludo.data.GameStatsEntity
import com.example.ludo.data.LudoRepository
import com.example.ludo.data.MatchRecordEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LudoRepository.getInstance(application)

    val stats: StateFlow<GameStatsEntity?> = repository.statsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentMatches: StateFlow<List<MatchRecordEntity>> = repository.recentMatchesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementEntity>> = repository.achievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun resetData() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }
}
