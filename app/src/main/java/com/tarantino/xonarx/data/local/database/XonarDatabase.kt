package com.tarantino.xonarx.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tarantino.xonarx.data.local.dao.*
import com.tarantino.xonarx.data.local.entity.*

@Database(
    entities = [
        IdentityEntity::class,
        TabEntity::class,
        HistoryItemEntity::class,
        BookmarkEntity::class,
        DownloadItemEntity::class,
        NoteEntity::class,
        TabGroupEntity::class,
        FeedEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class XonarDatabase : RoomDatabase() {
    abstract fun identityDao(): IdentityDao
    abstract fun tabDao(): TabDao
    abstract fun tabGroupDao(): TabGroupDao
    abstract fun historyItemDao(): HistoryItemDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun downloadItemDao(): DownloadItemDao
    abstract fun noteDao(): NoteDao
    abstract fun feedDao(): FeedDao
}
