package com.nordicwalk.core.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nordicwalk.core.data.db.dao.StudentDao
import com.nordicwalk.core.data.db.dao.TrainingRecordDao
import com.nordicwalk.core.data.db.dao.AnalysisSessionDao
import com.nordicwalk.core.data.db.entity.StudentEntity
import com.nordicwalk.core.data.db.entity.TrainingRecordEntity
import com.nordicwalk.core.data.db.entity.AnalysisSessionEntity

@Database(
    entities = [
        StudentEntity::class,
        TrainingRecordEntity::class,
        AnalysisSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NordicWalkDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun trainingRecordDao(): TrainingRecordDao
    abstract fun analysisSessionDao(): AnalysisSessionDao

    companion object {
        private const val DATABASE_NAME = "nordic_walk_db"
        private var instance: NordicWalkDatabase? = null

        fun getInstance(context: Context): NordicWalkDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NordicWalkDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
        }
    }
}
