package com.tarantino.xonarx.domain.model

data class Tab(
    val id: String,
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
    val sessionStateBlob: ByteArray? = null
)
