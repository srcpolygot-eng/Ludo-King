package com.example.ludo.data

import android.content.Context
import com.example.ludo.model.AiDifficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class LudoRepository(private val dao: LudoDao) {

    val statsFlow: Flow<GameStatsEntity?> = dao.getGameStatsFlow()
    val recentMatchesFlow: Flow<List<MatchRecordEntity>> = dao.getRecentMatchesFlow()
    val achievementsFlow: Flow<List<AchievementEntity>> = dao.getAchievementsFlow()

    suspend fun initializeDefaults() {
        val currentStats = dao.getGameStats()
        if (currentStats == null) {
            dao.insertOrUpdateStats(GameStatsEntity(id = 1))
        }

        val initialAchievements = listOf(
            AchievementEntity(
                id = "first_victory",
                title = "First Victory",
                description = "Win your very first Ludo match",
                iconKey = "trophy",
                currentProgress = 0,
                maxProgress = 1
            ),
            AchievementEntity(
                id = "token_hunter",
                title = "Token Collector",
                description = "Capture 10 opponent tokens",
                iconKey = "swords",
                currentProgress = 0,
                maxProgress = 10
            ),
            AchievementEntity(
                id = "home_runner",
                title = "Home Runner",
                description = "Bring 12 tokens successfully to home",
                iconKey = "flag",
                currentProgress = 0,
                maxProgress = 12
            ),
            AchievementEntity(
                id = "winning_streak",
                title = "Hot Streak",
                description = "Achieve a 3-match winning streak",
                iconKey = "flame",
                currentProgress = 0,
                maxProgress = 3
            ),
            AchievementEntity(
                id = "expert_challenger",
                title = "Expert Slayer",
                description = "Defeat an Expert AI opponent",
                iconKey = "crown",
                currentProgress = 0,
                maxProgress = 1
            ),
            AchievementEntity(
                id = "six_machine",
                title = "Lucky Roller",
                description = "Roll the number six 25 times",
                iconKey = "dice",
                currentProgress = 0,
                maxProgress = 25
            ),
            AchievementEntity(
                id = "grand_master",
                title = "Ludo Master",
                description = "Win 10 total matches",
                iconKey = "medal",
                currentProgress = 0,
                maxProgress = 10
            )
        )
        dao.insertInitialAchievements(initialAchievements)
    }

    suspend fun recordSixRolled() {
        val stats = dao.getGameStats() ?: GameStatsEntity(id = 1)
        val newTotalSixes = stats.totalSixesRolled + 1
        dao.insertOrUpdateStats(stats.copy(totalSixesRolled = newTotalSixes))
        updateAchievementProgress("six_machine", newTotalSixes)
    }

    suspend fun recordTokensCaptured(count: Int) {
        if (count <= 0) return
        val stats = dao.getGameStats() ?: GameStatsEntity(id = 1)
        val newCaptured = stats.tokensCaptured + count
        dao.insertOrUpdateStats(stats.copy(tokensCaptured = newCaptured))
        updateAchievementProgress("token_hunter", newCaptured)
    }

    suspend fun recordTokenReachedHome() {
        val stats = dao.getGameStats() ?: GameStatsEntity(id = 1)
        val newHome = stats.tokensHome + 1
        dao.insertOrUpdateStats(stats.copy(tokensHome = newHome))
        updateAchievementProgress("home_runner", newHome)
    }

    suspend fun recordMatchFinished(
        mode: String,
        winnerName: String,
        winnerColor: String,
        isHumanWinner: Boolean,
        playerCount: Int,
        aiDifficulty: AiDifficulty,
        turnsCount: Int,
        tokensCaptured: Int
    ) {
        // Record match log
        dao.insertMatchRecord(
            MatchRecordEntity(
                mode = mode,
                winnerName = winnerName,
                winnerColor = winnerColor,
                isHumanWinner = isHumanWinner,
                playerCount = playerCount,
                aiDifficulty = aiDifficulty.name,
                turnsCount = turnsCount,
                tokensCaptured = tokensCaptured
            )
        )

        // Update stats
        val stats = dao.getGameStats() ?: GameStatsEntity(id = 1)
        val newMatchesPlayed = stats.matchesPlayed + 1
        val newWins = stats.matchesWon + (if (isHumanWinner) 1 else 0)
        val newLosses = stats.matchesLost + (if (!isHumanWinner) 1 else 0)
        val newStreak = if (isHumanWinner) stats.currentStreak + 1 else 0
        val newBestStreak = maxOf(stats.bestStreak, newStreak)

        val newEasyWins = stats.easyWins + (if (isHumanWinner && aiDifficulty == AiDifficulty.EASY) 1 else 0)
        val newMedWins = stats.mediumWins + (if (isHumanWinner && aiDifficulty == AiDifficulty.MEDIUM) 1 else 0)
        val newHardWins = stats.hardWins + (if (isHumanWinner && aiDifficulty == AiDifficulty.HARD) 1 else 0)
        val newExpertWins = stats.expertWins + (if (isHumanWinner && aiDifficulty == AiDifficulty.EXPERT) 1 else 0)

        dao.insertOrUpdateStats(
            stats.copy(
                matchesPlayed = newMatchesPlayed,
                matchesWon = newWins,
                matchesLost = newLosses,
                currentStreak = newStreak,
                bestStreak = newBestStreak,
                easyWins = newEasyWins,
                mediumWins = newMedWins,
                hardWins = newHardWins,
                expertWins = newExpertWins
            )
        )

        // Achievements check
        if (isHumanWinner) {
            updateAchievementProgress("first_victory", 1)
            updateAchievementProgress("winning_streak", newStreak)
            updateAchievementProgress("grand_master", newWins)
            if (aiDifficulty == AiDifficulty.EXPERT) {
                updateAchievementProgress("expert_challenger", 1)
            }
        }
    }

    private suspend fun updateAchievementProgress(achievementId: String, currentVal: Int) {
        val list = dao.getAllAchievements()
        val ach = list.find { it.id == achievementId } ?: return
        if (ach.isUnlocked) return

        val newProgress = minOf(currentVal, ach.maxProgress)
        val unlocked = newProgress >= ach.maxProgress
        dao.updateAchievement(
            ach.copy(
                currentProgress = newProgress,
                isUnlocked = unlocked,
                unlockedAt = if (unlocked) System.currentTimeMillis() else null
            )
        )
    }

    suspend fun resetAllData() {
        dao.clearMatchHistory()
        dao.clearStats()
        dao.insertOrUpdateStats(GameStatsEntity(id = 1))
        dao.resetAchievements()
    }

    companion object {
        @Volatile
        private var INSTANCE: LudoRepository? = null

        fun getInstance(context: Context): LudoRepository {
            return INSTANCE ?: synchronized(this) {
                val db = LudoDatabase.getDatabase(context)
                val repo = LudoRepository(db.ludoDao())
                INSTANCE = repo
                repo
            }
        }
    }
}
