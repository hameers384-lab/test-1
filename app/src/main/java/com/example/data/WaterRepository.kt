package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class WaterRepository(private val waterDao: WaterDao) {

    val userProfile: Flow<UserProfile?> = waterDao.getUserProfileFlow()
    val allLogs: Flow<List<WaterLog>> = waterDao.getAllLogs()
    val customContainers: Flow<List<CustomContainer>> = waterDao.getAllCustomContainersFlow()

    suspend fun getUserProfileDirect(): UserProfile? {
        return waterDao.getUserProfile()
    }

    fun getTodayLogs(): Flow<List<WaterLog>> {
        val midnight = getMidnightTodayTimestamp()
        return waterDao.getTodayLogs(midnight)
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        waterDao.saveUserProfile(profile)
    }

    suspend fun insertLog(amountMl: Int) {
        val log = WaterLog(amountMl = amountMl, timestamp = System.currentTimeMillis())
        waterDao.insertLog(log)
    }

    suspend fun deleteLogById(id: Long) {
        waterDao.deleteLogById(id)
    }

    suspend fun clearLogs() {
        waterDao.clearAllLogs()
    }

    suspend fun deleteUserProfile() {
        waterDao.deleteUserProfile()
    }

    suspend fun insertCustomContainer(container: CustomContainer) {
        waterDao.insertCustomContainer(container)
    }

    suspend fun deleteCustomContainerById(id: Long) {
        waterDao.deleteCustomContainerById(id)
    }

    fun getMidnightTodayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
