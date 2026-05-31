package com.tarantino.xonarx.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tarantino.xonarx.domain.model.Feed

@Entity(tableName = "feeds")
data class FeedEntity(
    @PrimaryKey val id: String,
    val identityId: String,
    val title: String,
    val url: String,
    val lastItemTitle: String?,
    val lastItemUrl: String?
) {
    fun toDomainModel() = Feed(id, identityId, title, url, lastItemTitle, lastItemUrl)
    companion object {
        fun fromDomainModel(model: Feed) = FeedEntity(
            model.id, model.identityId, model.title, model.url, model.lastItemTitle, model.lastItemUrl
        )
    }
}
