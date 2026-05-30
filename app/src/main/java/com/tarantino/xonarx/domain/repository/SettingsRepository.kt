package com.tarantino.xonarx.domain.repository

import kotlinx.coroutines.flow.Flow

data class AppPreferences(
    val lastActiveIdentityId: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useMaterialYou: Boolean = true
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }

interface SettingsRepository {
    val preferences: Flow<AppPreferences>
    suspend fun updateLastActiveIdentityId(id: String?)
    suspend fun updateThemeMode(mode: ThemeMode)
    suspend fun updateMaterialYou(enabled: Boolean)
}
