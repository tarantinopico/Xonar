package com.tarantino.xonarx.data.repository

import com.tarantino.xonarx.data.local.dao.TabGroupDao
import com.tarantino.xonarx.data.local.entity.TabGroupEntity
import com.tarantino.xonarx.domain.model.TabGroup
import com.tarantino.xonarx.domain.repository.TabGroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TabGroupRepositoryImpl @Inject constructor(
    private val dao: TabGroupDao
) : TabGroupRepository {
    override fun observeGroups(identityId: String): Flow<List<TabGroup>> =
        dao.observeGroups(identityId).map { list -> list.map { it.toDomainModel() } }

    override suspend fun getGroupById(groupId: String, identityId: String): TabGroup? =
        dao.getGroupById(groupId, identityId)?.toDomainModel()

    override suspend fun addGroup(group: TabGroup) =
        dao.insert(TabGroupEntity.fromDomainModel(group))

    override suspend fun updateGroup(group: TabGroup) =
        dao.update(TabGroupEntity.fromDomainModel(group))

    override suspend fun removeGroup(group: TabGroup) =
        dao.delete(TabGroupEntity.fromDomainModel(group))

    override suspend fun ungroupTabs(groupId: String) =
        dao.ungroupTabs(groupId)

    override suspend fun deleteTabsInGroup(groupId: String) =
        dao.deleteTabsInGroup(groupId)
}
