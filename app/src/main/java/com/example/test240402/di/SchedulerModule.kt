package com.example.test240402.di

import android.app.AlarmManager
import android.content.Context
import com.example.test240402.presentation.ui.AlarmScheduler
import com.example.test240402.presentation.ui.AlarmSchedulerImpl
import com.example.test240402.presentation.ui.GeofenceScheduler
import com.example.test240402.presentation.ui.GeofenceSchedulerImpl
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
        alarmManager: AlarmManager
    ): AlarmScheduler {
        return AlarmSchedulerImpl(context, alarmManager)
    }

    @Provides
    @Singleton
    fun provideGeofenceScheduler(
        @ApplicationContext context: Context
    ): GeofenceScheduler {
        return GeofenceSchedulerImpl(context)
    }
}
