package com.example.dreamdoodler

import android.app.Application
import androidx.room.Room
import com.example.funfactassignment.DrawingDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DrawingApplication : Application() {

    val scope = CoroutineScope(SupervisorJob())

    val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            DrawingDatabase::class.java,
            "drawing-database"
        ).fallbackToDestructiveMigration().build()
    }

    val repository by lazy { DrawingRepository(scope, db.drawingDao(), filesDir) }
}