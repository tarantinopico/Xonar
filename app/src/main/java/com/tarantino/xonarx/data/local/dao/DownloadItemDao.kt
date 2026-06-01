package com.tarantino.xonarx.data.local.dao

import androidx.room.*
import com.tarantino.xonarx.data.local.entity.DownloadItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadItemDao {
    @Query("SELECT * FROM download_items ORDER BY scheduledAt DESC")
    fun observeAllDownloads(): Flow<List<DownloadItemEntity>>

    @Query("SELECT * FROM download_items WHERE identityId = :identityId ORDER BY scheduledAt DESC")
    fun observeDownloadsByIdentity(identityId: String): Flow<List<DownloadItemEntity>>

    @Query("SELECT * FROM download_items WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: String): DownloadItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(item: DownloadItemEntity)

    @Update
    suspend fun updateDownload(item: DownloadItemEntity)

    @Delete
    suspend fun deleteDownload(item: DownloadItemEntity)

    @Query("DELETE FROM download_items WHERE status IN ('COMPLETED', 'FAILED', 'CANCELLED')")
    suspend fun clearCompletedDownloads()

    @Query("DELETE FROM download_items")
    suspend fun clearAllDownloads()
}
