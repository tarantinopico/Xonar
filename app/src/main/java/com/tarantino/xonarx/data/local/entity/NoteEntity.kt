package com.tarantino.xonarx.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tarantino.xonarx.domain.model.Note

@Entity(
    tableName = "notes",
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
data class NoteEntity(
    @PrimaryKey val id: String,
    val identityId: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val pinned: Boolean
) {
    fun toDomainModel() = Note(
        id = id,
        identityId = identityId,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
        pinned = pinned
    )

    companion object {
        fun fromDomainModel(item: Note) = NoteEntity(
            id = item.id,
            identityId = item.identityId,
            title = item.title,
            content = item.content,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt,
            pinned = item.pinned
        )
    }
}
