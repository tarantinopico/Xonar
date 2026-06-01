package com.tarantino.xonarx.data.local.dao

import androidx.room.*
import com.tarantino.xonarx.data.local.entity.FeedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Query("SELECT * FROM feeds WHERE identityId = :identityId")
    fun getFeedsForIdentity(identityId: String): Flow<List<FeedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeed(feed: FeedEntity)

    @Delete
    suspend fun deleteFeed(feed: FeedEntity)
}
