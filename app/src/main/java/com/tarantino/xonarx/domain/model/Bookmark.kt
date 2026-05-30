package com.tarantino.xonarx.domain.model

data class Bookmark(
    val id: String,
    val identityId: String,
    val url: String,
    val title: String,
    val folderId: String?,
    val faviconUrl: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val sortOrder: Int,
    val isFavorite: Boolean = false
)
