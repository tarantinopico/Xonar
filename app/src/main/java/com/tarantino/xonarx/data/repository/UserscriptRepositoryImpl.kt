package com.tarantino.xonarx.data.repository

import com.tarantino.xonarx.data.local.dao.UserscriptDao
import com.tarantino.xonarx.data.local.entity.UserscriptEntity
import com.tarantino.xonarx.domain.model.Userscript
import com.tarantino.xonarx.domain.repository.UserscriptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserscriptRepositoryImpl @Inject constructor(
    private val dao: UserscriptDao
) : UserscriptRepository {
    override fun getScriptsForIdentity(identityId: String): Flow<List<Userscript>> {
        return dao.getScriptsForIdentity(identityId).map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun saveScript(script: Userscript) {
        dao.insertScript(UserscriptEntity.fromDomainModel(script))
    }

    override suspend fun deleteScript(script: Userscript) {
        dao.deleteScript(UserscriptEntity.fromDomainModel(script))
    }

    override suspend fun getAllScripts(): List<Userscript> {
        return dao.getAllScripts().map { it.toDomainModel() }
    }
}
