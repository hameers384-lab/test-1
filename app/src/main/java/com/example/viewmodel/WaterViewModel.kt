package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.WaterApplication
import com.example.data.UserProfile
import com.example.data.WaterLog
import com.example.data.CustomContainer
import com.example.data.WaterRepository
import com.example.receiver.WaterNotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppScreen {
    Onboarding,
    GoalDisplay,
    Dashboard
}

class WaterViewModel(
    private val repository: WaterRepository,
    private val appContext: Context
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.Onboarding)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val todayLogs: StateFlow<List<WaterLog>> = repository.getTodayLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current daily goal Ml
    val dailyGoal: StateFlow<Int> = userProfile
        .map { it?.dailyGoalMl ?: 2000 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 2000
        )

    // Total intake today in Ml
    val totalIntakeToday: StateFlow<Int> = todayLogs
        .map { logs -> logs.sumOf { it.amountMl } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Hydration progress (0.0 to 1.0)
    val intakeProgress: StateFlow<Float> = combine(totalIntakeToday, dailyGoal) { current, goal ->
        if (goal > 0) (current.toFloat() / goal.toFloat()).coerceIn(0f, 1.5f) else 0f
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    val allLogs: StateFlow<List<WaterLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val customContainers: StateFlow<List<CustomContainer>> = repository.customContainers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentStreak: StateFlow<Int> = combine(allLogs, dailyGoal) { logs, goal ->
        calculateCurrentStreak(logs, goal)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val maxStreak: StateFlow<Int> = combine(allLogs, dailyGoal) { logs, goal ->
        calculateMaxStreak(logs, goal)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val achievements: StateFlow<List<Achievement>> = combine(
        allLogs,
        dailyGoal,
        customContainers,
        currentStreak,
        maxStreak
    ) { logs, goal, containers, curStreak, mStreak ->
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val intakeByDate = logs.groupBy { sdf.format(Date(it.timestamp)) }
            .mapValues { it.value.sumOf { log -> log.amountMl } }

        val totalDaysMet = intakeByDate.filter { it.value >= goal }.size

        listOf(
            Achievement(
                id = "first_goal",
                title = "First Day Goal Met",
                description = "Meet your daily water intake goal for the first time.",
                emoji = "⭐",
                isUnlocked = totalDaysMet >= 1,
                progress = if (totalDaysMet >= 1) 1f else 0f,
                progressText = if (totalDaysMet >= 1) "Completed!" else "0 / 1 days"
            ),
            Achievement(
                id = "streak_7",
                title = "7-Day Streak",
                description = "Perform a streak of 7 consecutive days meeting your hydration goal.",
                emoji = "🔥",
                isUnlocked = mStreak >= 7,
                progress = (mStreak.toFloat() / 7f).coerceIn(0f, 1f),
                progressText = if (mStreak >= 7) "Completed!" else "$mStreak / 7 days"
            ),
            Achievement(
                id = "perfect_week",
                title = "Perfect Week",
                description = "Maintain a perfect 7-day consecutive streak of pure hydration.",
                emoji = "🏆",
                isUnlocked = mStreak >= 7,
                progress = (mStreak.toFloat() / 7f).coerceIn(0f, 1f),
                progressText = if (mStreak >= 7) "Completed!" else "$mStreak / 7 days"
            ),
            Achievement(
                id = "custom_cup",
                title = "Custom Creator",
                description = "Define and save at least one custom container.",
                emoji = "🧪",
                isUnlocked = containers.isNotEmpty(),
                progress = if (containers.isNotEmpty()) 1f else 0f,
                progressText = if (containers.isNotEmpty()) "Completed!" else "0 / 1 custom container"
            ),
            Achievement(
                id = "big_drinker",
                title = "Hydration Hero",
                description = "Log water 3 or more times on any single day.",
                emoji = "🌊",
                isUnlocked = logs.groupBy { sdf.format(Date(it.timestamp)) }.any { it.value.size >= 3 },
                progress = if (logs.groupBy { sdf.format(Date(it.timestamp)) }.any { it.value.size >= 3 }) 1f else 0f,
                progressText = if (logs.groupBy { sdf.format(Date(it.timestamp)) }.any { it.value.size >= 3 }) "Completed!" else "0 / 3 entries in 1 day"
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Temporary calculations on onboarding flow
    private val _tempCalculatedGoal = MutableStateFlow<Int?>(null)
    val tempCalculatedGoal: StateFlow<Int?> = _tempCalculatedGoal.asStateFlow()

    init {
        // Query profile to determine initial screen
        viewModelScope.launch {
            val profile = repository.getUserProfileDirect()
            if (profile != null) {
                _currentScreen.value = AppScreen.Dashboard
            } else {
                _currentScreen.value = AppScreen.Onboarding
            }
        }
    }

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun calculateRecommendedGoal(age: Int, heightCm: Double, weightKg: Double, gender: String): Int {
        val baseline = weightKg * 35.0
        val ageAdjustment = when {
            age < 30 -> 150.0
            age > 55 -> -150.0
            else -> 0.0
        }
        val genderAdjustment = when (gender) {
            "Male" -> 200.0
            "Female" -> -100.0
            else -> 0.0
        }
        // Round to nearest 50ml, keep within 1000ml to 5000ml
        val finalGoal = (baseline + ageAdjustment + genderAdjustment).toInt()
        val roundedGoal = ((finalGoal + 25) / 50) * 50
        return roundedGoal.coerceIn(1000, 5000)
    }

    fun saveOnboardingProfile(age: Int, heightCm: Double, weightKg: Double, gender: String, customGoalMl: Int? = null) {
        val calculated = calculateRecommendedGoal(age, heightCm, weightKg, gender)
        val goalToSave = customGoalMl ?: calculated

        viewModelScope.launch {
            val updatedProfile = UserProfile(
                age = age,
                heightCm = heightCm,
                weightKg = weightKg,
                gender = gender,
                dailyGoalMl = goalToSave,
                reminderIntervalMinutes = 60, // Default to 1 hour
                remindersEnabled = true
            )
            repository.saveUserProfile(updatedProfile)
            _tempCalculatedGoal.value = goalToSave
            _currentScreen.value = AppScreen.GoalDisplay
        }
    }

    fun completeGoalDisplay() {
        // Schedule default reminder notification
        viewModelScope.launch {
            val profile = repository.getUserProfileDirect()
            if (profile != null && profile.remindersEnabled) {
                WaterNotificationHelper.scheduleReminder(appContext, profile.reminderIntervalMinutes)
            }
        }
        _currentScreen.value = AppScreen.Dashboard
    }

    fun logWater(amountMl: Int) {
        viewModelScope.launch {
            repository.insertLog(amountMl)
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            repository.deleteLogById(id)
        }
    }

    fun updateReminders(enabled: Boolean, intervalMinutes: Int) {
        viewModelScope.launch {
            val curProfile = repository.getUserProfileDirect() ?: return@launch
            val updated = curProfile.copy(
                remindersEnabled = enabled,
                reminderIntervalMinutes = intervalMinutes.coerceIn(25, 105)
            )
            repository.saveUserProfile(updated)

            if (enabled) {
                WaterNotificationHelper.scheduleReminder(appContext, updated.reminderIntervalMinutes)
            } else {
                WaterNotificationHelper.cancelReminder(appContext)
            }
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun resetData() {
        viewModelScope.launch {
            WaterNotificationHelper.cancelReminder(appContext)
            repository.clearLogs()
            repository.deleteUserProfile()
            _tempCalculatedGoal.value = null
            _currentScreen.value = AppScreen.Onboarding
        }
    }

    fun addCustomContainer(name: String, volumeMl: Int, emoji: String = "🥤") {
        viewModelScope.launch {
            repository.insertCustomContainer(
                CustomContainer(
                    name = name,
                    volumeMl = volumeMl,
                    emoji = emoji
                )
            )
        }
    }

    fun deleteCustomContainer(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomContainerById(id)
        }
    }

    private fun calculateCurrentStreak(logs: List<WaterLog>, dailyGoal: Int): Int {
        if (logs.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val intakeByDate = logs.groupBy { sdf.format(Date(it.timestamp)) }
            .mapValues { it.value.sumOf { log -> log.amountMl } }

        val calendar = Calendar.getInstance()
        var streak = 0

        // Check today
        val todayStr = sdf.format(calendar.time)
        val todayIntake = intakeByDate[todayStr] ?: 0

        // Check yesterday
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(calendar.time)
        val yesterdayIntake = intakeByDate[yesterdayStr] ?: 0

        val startFromToday = todayIntake >= dailyGoal
        val startFromYesterday = !startFromToday && yesterdayIntake >= dailyGoal

        val checkCalendar = Calendar.getInstance()
        if (startFromToday) {
            var dateToCheck = todayStr
            while (true) {
                val intake = intakeByDate[dateToCheck] ?: 0
                if (intake >= dailyGoal) {
                    streak++
                    checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
                    dateToCheck = sdf.format(checkCalendar.time)
                } else {
                    break
                }
            }
        } else if (startFromYesterday) {
            checkCalendar.add(Calendar.DAY_OF_YEAR, -1) // Start from yesterday
            var dateToCheck = yesterdayStr
            while (true) {
                val intake = intakeByDate[dateToCheck] ?: 0
                if (intake >= dailyGoal) {
                    streak++
                    checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
                    dateToCheck = sdf.format(checkCalendar.time)
                } else {
                    break
                }
            }
        }
        return streak
    }

    private fun calculateMaxStreak(logs: List<WaterLog>, dailyGoal: Int): Int {
        if (logs.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val intakeByDate = logs.groupBy { sdf.format(Date(it.timestamp)) }
            .mapValues { it.value.sumOf { log -> log.amountMl } }

        // Extract dates that met the goal, sort them
        val metDates = intakeByDate.filter { it.value >= dailyGoal }.keys.mapNotNull {
            try { sdf.parse(it) } catch (e: Exception) { null }
        }.sorted()
        if (metDates.isEmpty()) return 0

        var maxStreak = 0
        var currentStreak = 1

        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()

        for (i in 0 until metDates.size - 1) {
            cal1.time = metDates[i]
            cal2.time = metDates[i+1]

            // Add 1 day to cal1 and check if it matches cal2
            cal1.add(Calendar.DAY_OF_YEAR, 1)

            // Compare year, month, day
            val isConsecutive = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)

            if (isConsecutive) {
                currentStreak++
            } else {
                maxStreak = maxOf(maxStreak, currentStreak)
                currentStreak = 1
            }
        }
        maxStreak = maxOf(maxStreak, currentStreak)
        return maxStreak
    }
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val isUnlocked: Boolean,
    val progress: Float = 0f,
    val progressText: String = ""
)

class WaterViewModelFactory(
    private val repository: WaterRepository,
    private val appContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaterViewModel(repository, appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
