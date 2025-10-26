package com.example.test240402.di

import android.app.AlarmManager
import android.content.Context
import com.example.test240402.presentation.ui.AlarmScheduler
import com.example.test240402.presentation.ui.AlarmSchedulerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SchedulerModule {

    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    }
    @Provides
    @Singleton
    fun provideAlarmScheduler(
        @ApplicationContext context: Context,
        alarmManager: AlarmManager // Hilt가 위 함수(provideAlarmManager)를 통해 자동으로 주입해 줌
    ): AlarmScheduler {
        return AlarmSchedulerImpl(context, alarmManager)
    }

}