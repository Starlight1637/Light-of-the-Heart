package com.mindful.companion.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        KnowledgeEntry::class
    ],
    version = 4,
    exportSchema = false
)
abstract class MindfulDatabase : RoomDatabase() {

    abstract fun knowledgeDao(): KnowledgeDao
}
