package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import androidx.room.Database
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val age: Int,
    val heightCm: Double,
    val weightKg: Double,
    val gender: String, // "Male", "Female", "Other"
    val dailyGoalMl: Int,
    val reminderIntervalMinutes: Int = 60,
    val remindersEnabled: Boolean = true
)

@Entity(tableName = "water_log")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_containers")
data class CustomContainer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val volumeMl: Int,
    val emoji: String = "🥤"
)

@Dao
interface WaterDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    @Query("SELECT * FROM water_log ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WaterLog>>

    @Query("SELECT * FROM water_log WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getTodayLogs(startOfDay: Long): Flow<List<WaterLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WaterLog)

    @Query("DELETE FROM water_log WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM water_log")
    suspend fun clearAllLogs()

    @Query("DELETE FROM user_profile")
    suspend fun deleteUserProfile()

    // Custom Containers Queries
    @Query("SELECT * FROM custom_containers ORDER BY id ASC")
    fun getAllCustomContainersFlow(): Flow<List<CustomContainer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomContainer(container: CustomContainer)

    @Query("DELETE FROM custom_containers WHERE id = :id")
    suspend fun deleteCustomContainerById(id: Long)
}

@Database(entities = [UserProfile::class, WaterLog::class, CustomContainer::class], version = 2, exportSchema = false)
abstract class WaterDatabase : RoomDatabase() {
    abstract fun waterDao(): WaterDao
}
