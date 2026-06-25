package com.project.voicetotask.di

import com.project.voicetotask.data.device.AndroidAudioRecorder
import com.project.voicetotask.data.device.ExoAudioPlayer
import com.project.voicetotask.domain.repository.AudioPlayer
import com.project.voicetotask.domain.repository.AudioRecorder
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {

    @Binds
    @Singleton
    abstract fun bindAudioRecorder(
        androidAudioRecorder: AndroidAudioRecorder
    ): AudioRecorder

    @Binds
    @Singleton
    abstract fun bindAudioPlayer(
        exoAudioPlayer: ExoAudioPlayer
    ): AudioPlayer
}
