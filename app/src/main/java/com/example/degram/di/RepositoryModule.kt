package com.example.degram.di

import android.content.Context
import com.example.degram.data.DegramRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideRepo(@ApplicationContext context: Context) : DegramRepository { return DegramRepository(context)}

}
