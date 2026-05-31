package com.tarantino.xonarx.data.repository

import com.tarantino.xonarx.data.local.dao.FeedDao
import com.tarantino.xonarx.data.local.entity.FeedEntity
import com.tarantino.xonarx.domain.model.Feed
import com.tarantino.xonarx.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepositoryImpl @Inject constructor(
    private val dao: FeedDao
) : FeedRepository {
    override fun getFeedsForIdentity(identityId: String): Flow<List<Feed>> {
        return dao.getFeedsForIdentity(identityId).map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun saveFeed(feed: Feed) {
        dao.insertFeed(FeedEntity.fromDomainModel(feed))
    }

    override suspend fun deleteFeed(feed: Feed) {
        dao.deleteFeed(FeedEntity.fromDomainModel(feed))
    }
}
