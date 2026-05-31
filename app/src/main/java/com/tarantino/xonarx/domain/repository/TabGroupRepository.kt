package com.tarantino.xonarx.domain.repository

import com.tarantino.xonarx.domain.model.TabGroup
import kotlinx.coroutines.flow.Flow

interface TabGroupRepository {
    fun observeGroups(identityId: String): Flow<List<TabGroup>>
    suspend fun getGroupById(groupId: String, identityId: String): TabGroup?
    suspend fun addGroup(group: TabGroup)
    suspend fun updateGroup(group: TabGroup)
    suspend fun removeGroup(group: TabGroup)
    suspend fun ungroupTabs(groupId: String)
    suspend fun deleteTabsInGroup(groupId: String)
}
