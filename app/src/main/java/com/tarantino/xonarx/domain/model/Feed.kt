package com.tarantino.xonarx.domain.model

data class Feed(
    val id: String,
    val identityId: String,
    val title: String,
    val url: String,
    val lastItemTitle: String? = null,
    val lastItemUrl: String? = null
)
