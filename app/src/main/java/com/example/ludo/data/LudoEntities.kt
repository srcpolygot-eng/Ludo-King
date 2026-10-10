package com.example.ludo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_stats")
data class GameStatsEntity(
    @PrimaryKey val id: Int = 1,
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val matchesLost: Int = 0,
    val tokensCaptured: Int = 0,
    val tokensLost: Int = 0,
    val tokensHome: Int = 0,
    val totalSixesRolled: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val easyWins: Int = 0,
    val mediumWins: Int = 0,
    val hardWins: Int = 0,
    val expertWins: Int = 0
)

@Entity(tableName = "match_records")
data class MatchRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String,
    val winnerName: String,
    val winnerColor: String,
    val isHumanWinner: Boolean,
    val playerCount: Int,
    val aiDifficulty: String,
    val turnsCount: Int,
    val tokensCaptured: Int
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconKey: String,
    val currentProgress: Int,
    val maxProgress: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)
