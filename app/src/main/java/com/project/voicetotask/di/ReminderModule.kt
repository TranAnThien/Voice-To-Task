package com.project.voicetotask.di

import com.project.voicetotask.data.reminder.AlarmTaskReminderScheduler
import com.project.voicetotask.domain.reminder.TaskReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderModule {

    @Binds
    @Singleton
    abstract fun bindTaskReminderScheduler(
        scheduler: AlarmTaskReminderScheduler
    ): TaskReminderScheduler
}
