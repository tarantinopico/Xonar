package com.tarantino.xonarx.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tarantino.xonarx.data.local.entity.IdentityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IdentityDao {
    @Query("SELECT * FROM identities ORDER BY orderIndex ASC")
    fun getAllIdentities(): Flow<List<IdentityEntity>>

    @Query("SELECT * FROM identities WHERE id = :id LIMIT 1")
    suspend fun getIdentityById(id: String): IdentityEntity?

    @Query("SELECT * FROM identities WHERE id = :id LIMIT 1")
    fun observeIdentityById(id: String): Flow<IdentityEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdentity(identity: IdentityEntity)

    @Update
    suspend fun updateIdentity(identity: IdentityEntity)

    @Delete
    suspend fun deleteIdentity(identity: IdentityEntity)
}
