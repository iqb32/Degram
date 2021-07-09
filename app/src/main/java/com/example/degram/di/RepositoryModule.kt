package com.example.degram.di

import com.example.degram.data.DegramRepository
import com.example.degram.database.DegramDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideRepo(database : DegramDb) : DegramRepository { return DegramRepository(database)}
}