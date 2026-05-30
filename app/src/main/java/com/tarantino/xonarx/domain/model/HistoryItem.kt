package com.tarantino.xonarx.domain.model

data class HistoryItem(
    val id: String,
    val identityId: String,
    val url: String,
    val title: String,
    val visitCount: Int,
    val lastVisitedAt: Long,
    val createdAt: Long
)
