package com.tarantino.xonarx.domain.model

data class Userscript(
    val id: String,
    val name: String,
    val code: String,
    val domain: String?,
    val isEnabled: Boolean,
    val identityId: String,
    val isCss: Boolean = false
)
