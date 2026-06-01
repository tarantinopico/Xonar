package com.tarantino.xonarx.domain.repository

import com.tarantino.xonarx.domain.model.Feed
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    fun getFeedsForIdentity(identityId: String): Flow<List<Feed>>
    suspend fun saveFeed(feed: Feed)
    suspend fun deleteFeed(feed: Feed)
}
