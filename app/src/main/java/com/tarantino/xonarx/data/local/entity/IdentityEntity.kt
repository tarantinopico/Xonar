package com.tarantino.xonarx.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tarantino.xonarx.domain.model.Identity

@Entity(tableName = "identities")
data class IdentityEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val color: Int,
    val iconName: String?,
    val defaultSearchEngine: String,
    val privacyFlags: Int,
    val lockSettings: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val orderIndex: Int,
    val isIncognitoTemplate: Boolean
) {
    fun toDomainModel() = Identity(
        id = id,
        displayName = displayName,
        color = color,
        iconName = iconName,
        defaultSearchEngine = defaultSearchEngine,
        privacyFlags = privacyFlags,
        lockSettings = lockSettings,
        createdAt = createdAt,
        updatedAt = updatedAt,
        orderIndex = orderIndex,
        isIncognitoTemplate = isIncognitoTemplate
    )

    companion object {
        fun fromDomainModel(identity: Identity) = IdentityEntity(
            id = identity.id,
            displayName = identity.displayName,
            color = identity.color,
            iconName = identity.iconName,
            defaultSearchEngine = identity.defaultSearchEngine,
            privacyFlags = identity.privacyFlags,
            lockSettings = identity.lockSettings,
            createdAt = identity.createdAt,
            updatedAt = identity.updatedAt,
            orderIndex = identity.orderIndex,
            isIncognitoTemplate = identity.isIncognitoTemplate
        )
    }
}
