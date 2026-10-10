package com.example.ludo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LudoDao {
    // Stats
    @Query("SELECT * FROM game_stats WHERE id = 1")
    fun getGameStatsFlow(): Flow<GameStatsEntity?>

    @Query("SELECT * FROM game_stats WHERE id = 1")
    suspend fun getGameStats(): GameStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: GameStatsEntity)

    // Matches
    @Query("SELECT * FROM match_records ORDER BY timestamp DESC LIMIT 30")
    fun getRecentMatchesFlow(): Flow<List<MatchRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchRecord(record: MatchRecordEntity)

    // Achievements
    @Query("SELECT * FROM achievements")
    fun getAchievementsFlow(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievements(): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    // Clear
    @Query("DELETE FROM match_records")
    suspend fun clearMatchHistory()

    @Query("DELETE FROM game_stats")
    suspend fun clearStats()

    @Query("UPDATE achievements SET currentProgress = 0, isUnlocked = 0, unlockedAt = NULL")
    suspend fun resetAchievements()
}
