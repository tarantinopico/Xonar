package com.tarantino.xonarx.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tarantino.xonarx.data.local.entity.TabEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TabDao {
    @Query("SELECT * FROM tabs WHERE identityId = :identityId ORDER BY createdAt ASC")
    fun observeTabsByIdentity(identityId: String): Flow<List<TabEntity>>

    @Query("SELECT * FROM tabs WHERE id = :tabId AND identityId = :identityId LIMIT 1")
    suspend fun getTab(tabId: String, identityId: String): TabEntity?

    @Query("SELECT * FROM tabs WHERE isActive = 1 AND identityId = :identityId LIMIT 1")
    suspend fun getActiveTab(identityId: String): TabEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tab: TabEntity)

    @Update
    suspend fun updateTab(tab: TabEntity)

    @Delete
    suspend fun deleteTab(tab: TabEntity)

    @Query("UPDATE tabs SET isActive = 0 WHERE identityId = :identityId")
    suspend fun deactivateAllTabs(identityId: String)

    @Query("UPDATE tabs SET isActive = 1 WHERE id = :tabId AND identityId = :identityId")
    suspend fun activateTab(tabId: String, identityId: String)
}
