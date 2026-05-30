package com.tarantino.xonarx.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tarantino.xonarx.data.local.entity.DownloadItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadItemDao {
    @Query("SELECT * FROM download_items WHERE identityId = :identityId ORDER BY scheduledAt DESC")
    fun observeDownloadsByIdentity(identityId: String): Flow<List<DownloadItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(item: DownloadItemEntity)

    @Update
    suspend fun updateDownload(item: DownloadItemEntity)

    @Delete
    suspend fun deleteDownload(item: DownloadItemEntity)
}
