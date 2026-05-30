package com.tarantino.xonarx.domain.repository

import kotlinx.coroutines.flow.Flow

data class AppPreferences(
    val lastActiveIdentityId: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useMaterialYou: Boolean = true,
    val gesturesEnabled: Boolean = true,
    val searchEngineUrl: String = "https://www.google.com/search?q=",
    val biometricsEnabled: Boolean = true,
    val autoClearOnExit: Boolean = false
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }

interface SettingsRepository {
    val preferences: Flow<AppPreferences>
    suspend fun updateLastActiveIdentityId(id: String?)
    suspend fun updateThemeMode(mode: ThemeMode)
    suspend fun updateMaterialYou(enabled: Boolean)
    suspend fun updateGesturesEnabled(enabled: Boolean)
    suspend fun updateSearchEngine(url: String)
    suspend fun updateBiometricsEnabled(enabled: Boolean)
    suspend fun updateAutoClearOnExit(enabled: Boolean)
}
