package com.tarantino.xonarx.data.local.dao

import androidx.room.*
import com.tarantino.xonarx.data.local.entity.TabGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TabGroupDao {
    @Query("SELECT * FROM tab_groups WHERE identityId = :identityId ORDER BY orderIndex ASC")
    fun observeGroups(identityId: String): Flow<List<TabGroupEntity>>

    @Query("SELECT * FROM tab_groups WHERE id = :groupId AND identityId = :identityId LIMIT 1")
    suspend fun getGroupById(groupId: String, identityId: String): TabGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: TabGroupEntity)

    @Update
    suspend fun update(group: TabGroupEntity)

    @Delete
    suspend fun delete(group: TabGroupEntity)
    
    @Query("UPDATE tabs SET groupId = NULL WHERE groupId = :groupId")
    suspend fun ungroupTabs(groupId: String)

    @Query("DELETE FROM tabs WHERE groupId = :groupId")
    suspend fun deleteTabsInGroup(groupId: String)
}
