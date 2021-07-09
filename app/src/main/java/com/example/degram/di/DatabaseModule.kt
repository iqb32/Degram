package com.example.degram.di

import android.content.Context
import androidx.room.Room
import com.example.degram.database.DegramDb
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
    fun provideDatabase(
        @ApplicationContext appContext : Context
    ) : DegramDb {
        return Room.databaseBuilder(
            appContext,
            DegramDb::class.java,
            "degram"
        ).fallbackToDestructiveMigration().build()
    }
}
