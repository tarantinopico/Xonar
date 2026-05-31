package com.tarantino.xonarx.data.local.dao

import androidx.room.*
import com.tarantino.xonarx.data.local.entity.UserscriptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserscriptDao {
    @Query("SELECT * FROM userscripts WHERE identityId = :identityId")
    fun getScriptsForIdentity(identityId: String): Flow<List<UserscriptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: UserscriptEntity)

    @Delete
    suspend fun deleteScript(script: UserscriptEntity)

    @Query("SELECT * FROM userscripts")
    suspend fun getAllScripts(): List<UserscriptEntity>
}
