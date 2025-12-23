package com.nordicwalk.core.data.di

import android.content.Context
import com.nordicwalk.core.data.db.NordicWalkDatabase
import com.nordicwalk.core.data.repository.StudentRepository
import com.nordicwalk.core.data.repository.StudentRepositoryImpl
import com.nordicwalk.core.data.repository.TrainingRecordRepository
import com.nordicwalk.core.data.repository.TrainingRecordRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): NordicWalkDatabase = NordicWalkDatabase.getInstance(context)

    @Singleton
    @Provides
    fun provideStudentRepository(
        database: NordicWalkDatabase
    ): StudentRepository = StudentRepositoryImpl(database.studentDao())

    @Singleton
    @Provides
    fun provideTrainingRecordRepository(
        database: NordicWalkDatabase
    ): TrainingRecordRepository = TrainingRecordRepositoryImpl(database.trainingRecordDao())
}
