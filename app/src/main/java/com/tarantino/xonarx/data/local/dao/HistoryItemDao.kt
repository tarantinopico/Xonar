package com.tarantino.xonarx.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tarantino.xonarx.data.local.entity.HistoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryItemDao {
    @Query("SELECT * FROM history_items WHERE identityId = :identityId ORDER BY lastVisitedAt DESC")
    fun observeHistoryByIdentity(identityId: String): Flow<List<HistoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItem(item: HistoryItemEntity)

    @Update
    suspend fun updateHistoryItem(item: HistoryItemEntity)

    @Delete
    suspend fun deleteHistoryItem(item: HistoryItemEntity)

    @Query("DELETE FROM history_items WHERE identityId = :identityId AND lastVisitedAt < :beforeTimestamp")
    suspend fun deleteHistoryBefore(identityId: String, beforeTimestamp: Long)

    @Query("DELETE FROM history_items WHERE identityId = :identityId")
    suspend fun clearHistory(identityId: String)
}
