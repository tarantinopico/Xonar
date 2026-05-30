package com.tarantino.xonarx.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tarantino.xonarx.domain.model.HistoryItem

@Entity(
    tableName = "history_items",
    foreignKeys = [
        ForeignKey(
            entity = IdentityEntity::class,
            parentColumns = ["id"],
            childColumns = ["identityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["identityId"])]
)
data class HistoryItemEntity(
    @PrimaryKey val id: String,
    val identityId: String,
    val url: String,
    val title: String,
    val visitCount: Int,
    val lastVisitedAt: Long,
    val createdAt: Long
) {
    fun toDomainModel() = HistoryItem(
        id = id,
        identityId = identityId,
        url = url,
        title = title,
        visitCount = visitCount,
        lastVisitedAt = lastVisitedAt,
        createdAt = createdAt
    )

    companion object {
        fun fromDomainModel(item: HistoryItem) = HistoryItemEntity(
            id = item.id,
            identityId = item.identityId,
            url = item.url,
            title = item.title,
            visitCount = item.visitCount,
            lastVisitedAt = item.lastVisitedAt,
            createdAt = item.createdAt
        )
    }
}
