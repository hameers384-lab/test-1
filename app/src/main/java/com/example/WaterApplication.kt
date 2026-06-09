package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.WaterDatabase
import com.example.data.WaterRepository

class WaterApplication : Application() {

    val database: WaterDatabase by lazy {
        Room.databaseBuilder(
            this,
            WaterDatabase::class.java,
            "water_tracker_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    val repository: WaterRepository by lazy {
        WaterRepository(database.waterDao())
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: WaterApplication
            private set
    }
}
