package com.tarantino.xonarx.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tarantino.xonarx.domain.model.TabGroup

@Entity(
    tableName = "tab_groups",
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
data class TabGroupEntity(
    @PrimaryKey val id: String,
    val identityId: String,
    val name: String,
    val color: Int,
    val isExpanded: Boolean,
    val orderIndex: Int,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomainModel() = TabGroup(
        id = id,
        identityId = identityId,
        name = name,
        color = color,
        isExpanded = isExpanded,
        orderIndex = orderIndex,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomainModel(group: TabGroup) = TabGroupEntity(
            id = group.id,
            identityId = group.identityId,
            name = group.name,
            color = group.color,
            isExpanded = group.isExpanded,
            orderIndex = group.orderIndex,
            createdAt = group.createdAt,
            updatedAt = group.updatedAt
        )
    }
}
