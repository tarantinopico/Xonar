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

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `userscripts` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `code` TEXT NOT NULL,
                    `domain` TEXT,
                    `isEnabled` INTEGER NOT NULL,
                    `identityId` TEXT NOT NULL,
                    `isCss` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `feeds` (
                    `id` TEXT NOT NULL,
                    `identityId` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `url` TEXT NOT NULL,
                    `lastItemTitle` TEXT,
                    `lastItemUrl` TEXT,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `userscripts`")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `download_items` ADD COLUMN `totalBytes` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `download_items` ADD COLUMN `downloadedBytes` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `download_items` ADD COLUMN `speedBytesPerSecond` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `download_items` ADD COLUMN `etaSeconds` INTEGER NOT NULL DEFAULT -1")
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
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
        .fallbackToDestructiveMigration(false)
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

    @Provides
    fun provideFeedDao(db: XonarDatabase): FeedDao = db.feedDao()
}
