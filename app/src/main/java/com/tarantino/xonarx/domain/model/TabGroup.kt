package com.tarantino.xonarx.domain.model

data class TabGroup(
    val id: String,
    val identityId: String,
    val name: String,
    val color: Int,
    val isExpanded: Boolean,
    val orderIndex: Int,
    val createdAt: Long,
    val updatedAt: Long
)
