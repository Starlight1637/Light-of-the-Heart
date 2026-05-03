package com.mindful.companion.di

import android.content.Context
import androidx.room.Room
import com.mindful.companion.data.database.KnowledgeDao
import com.mindful.companion.data.database.MindfulDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): MindfulDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            MindfulDatabase::class.java,
            "mindful_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideKnowledgeDao(database: MindfulDatabase): KnowledgeDao {
        return database.knowledgeDao()
    }
}
