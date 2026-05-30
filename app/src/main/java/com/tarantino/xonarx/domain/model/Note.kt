package com.tarantino.xonarx.domain.model

data class Note(
    val id: String,
    val identityId: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val pinned: Boolean
)
