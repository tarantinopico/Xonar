package com.tarantino.xonarx.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `tab_groups` (
                    `id` TEXT NOT NULL,
                    `identityId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `color` INTEGER NOT NULL,
                    `isExpanded` INTEGER NOT NULL,
                    `orderIndex` INTEGER NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`identityId`) REFERENCES `identities`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tab_groups_identityId` ON `tab_groups` (`identityId`)")
        }
    }

    @Provides
    @Singleton
    fun provideXonarDatabase(@ApplicationContext context: Context): XonarDatabase {
        return Room.databaseBuilder(
            context,
            XonarDatabase::class.java,
            "xonar_db"
        )
        .addMigrations(MIGRATION_1_2)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideIdentityDao(db: XonarDatabase): IdentityDao = db.identityDao()

    @Provides
    fun provideTabDao(db: XonarDatabase): TabDao = db.tabDao()

    @Provides
    fun provideTabGroupDao(db: XonarDatabase): TabGroupDao = db.tabGroupDao()

    @Provides
    fun provideHistoryItemDao(db: XonarDatabase): HistoryItemDao = db.historyItemDao()

    @Provides
    fun provideBookmarkDao(db: XonarDatabase): BookmarkDao = db.bookmarkDao()

    @Provides
    fun provideDownloadItemDao(db: XonarDatabase): DownloadItemDao = db.downloadItemDao()

    @Provides
    fun provideNoteDao(db: XonarDatabase): NoteDao = db.noteDao()
}
