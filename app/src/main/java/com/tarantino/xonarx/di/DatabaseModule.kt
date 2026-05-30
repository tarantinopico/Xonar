package com.tarantino.xonarx.di

import android.content.Context
import androidx.room.Room
import com.tarantino.xonarx.data.local.dao.*
import com.tarantino.xonarx.data.local.database.XonarDatabase
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
    fun provideXonarDatabase(@ApplicationContext context: Context): XonarDatabase {
        return Room.databaseBuilder(
            context,
            XonarDatabase::class.java,
            "xonar_db"
        ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    fun provideIdentityDao(db: XonarDatabase): IdentityDao = db.identityDao()

    @Provides
    fun provideTabDao(db: XonarDatabase): TabDao = db.tabDao()

    @Provides
    fun provideHistoryItemDao(db: XonarDatabase): HistoryItemDao = db.historyItemDao()

    @Provides
    fun provideBookmarkDao(db: XonarDatabase): BookmarkDao = db.bookmarkDao()

    @Provides
    fun provideDownloadItemDao(db: XonarDatabase): DownloadItemDao = db.downloadItemDao()

    @Provides
    fun provideNoteDao(db: XonarDatabase): NoteDao = db.noteDao()
}
