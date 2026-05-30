package com.tarantino.xonarx.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tarantino.xonarx.domain.model.Bookmark

@Entity(
    tableName = "bookmarks",
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
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val identityId: String,
    val url: String,
    val title: String,
    val folderId: String?,
    val faviconUrl: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val sortOrder: Int,
    val isFavorite: Boolean = false
) {
    fun toDomainModel() = Bookmark(
        id = id,
        identityId = identityId,
        url = url,
        title = title,
        folderId = folderId,
        faviconUrl = faviconUrl,
        createdAt = createdAt,
        updatedAt = updatedAt,
        sortOrder = sortOrder,
        isFavorite = isFavorite
    )

    companion object {
        fun fromDomainModel(item: Bookmark) = BookmarkEntity(
            id = item.id,
            identityId = item.identityId,
            url = item.url,
            title = item.title,
            folderId = item.folderId,
            faviconUrl = item.faviconUrl,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt,
            sortOrder = item.sortOrder,
            isFavorite = item.isFavorite
        )
    }
}
