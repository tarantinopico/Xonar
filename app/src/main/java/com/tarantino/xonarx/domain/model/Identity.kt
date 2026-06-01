package com.tarantino.xonarx.domain.model

data class Identity(
    val id: String,
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
)
