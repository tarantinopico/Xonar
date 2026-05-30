package com.tarantino.xonarx.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tarantino.xonarx.domain.model.Tab

@Entity(
    tableName = "tabs",
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
data class TabEntity(
    @PrimaryKey val id: String,
    val identityId: String,
    val url: String,
    val title: String,
    val faviconUrl: String?,
    val isActive: Boolean,
    val isIncognito: Boolean,
    val isPinned: Boolean,
    val groupId: String?,
    val lastVisitedAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val sessionStateBlob: ByteArray?
) {
    fun toDomainModel() = Tab(
        id = id,
        identityId = identityId,
        url = url,
        title = title,
        faviconUrl = faviconUrl,
        isActive = isActive,
        isIncognito = isIncognito,
        isPinned = isPinned,
        groupId = groupId,
        lastVisitedAt = lastVisitedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        sessionStateBlob = sessionStateBlob
    )

    companion object {
        fun fromDomainModel(tab: Tab) = TabEntity(
            id = tab.id,
            identityId = tab.identityId,
            url = tab.url,
            title = tab.title,
            faviconUrl = tab.faviconUrl,
            isActive = tab.isActive,
            isIncognito = tab.isIncognito,
            isPinned = tab.isPinned,
            groupId = tab.groupId,
            lastVisitedAt = tab.lastVisitedAt,
            createdAt = tab.createdAt,
            updatedAt = tab.updatedAt,
            sessionStateBlob = tab.sessionStateBlob
        )
    }
}
