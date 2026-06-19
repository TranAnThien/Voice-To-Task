package com.project.voicetotask.di

import android.content.Context
import androidx.room.Room
import com.project.voicetotask.data.source.local.VoiceTaskDatabase
import com.project.voicetotask.data.source.local.dao.MeetingDao
import com.project.voicetotask.data.source.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VoiceTaskDatabase {
        return Room.databaseBuilder(
            context,
            VoiceTaskDatabase::class.java,
            "voice_task_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMeetingDao(database: VoiceTaskDatabase): MeetingDao {
        return database.meetingDao()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: VoiceTaskDatabase): TaskDao {
        return database.taskDao()
    }
}
