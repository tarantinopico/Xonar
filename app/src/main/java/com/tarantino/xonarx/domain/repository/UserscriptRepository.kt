package com.tarantino.xonarx.domain.repository

import com.tarantino.xonarx.domain.model.Userscript
import kotlinx.coroutines.flow.Flow

interface UserscriptRepository {
    fun getScriptsForIdentity(identityId: String): Flow<List<Userscript>>
    suspend fun saveScript(script: Userscript)
    suspend fun deleteScript(script: Userscript)
    suspend fun getAllScripts(): List<Userscript>
}
