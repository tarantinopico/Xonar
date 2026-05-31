package com.tarantino.xonarx.domain.repository

import kotlinx.coroutines.flow.Flow

data class CustomSearchEngine(
    val id: String,
    val name: String,
    val urlTemplate: String
)

data class AppPreferences(
    val lastActiveIdentityId: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useMaterialYou: Boolean = true,
    val gesturesEnabled: Boolean = true,
    val searchEngineUrl: String = "https://www.google.com/search?q=",
    val biometricsEnabled: Boolean = true,
    val autoClearOnExit: Boolean = false,
    
    // New features
    val bottomControls: Boolean = false,
    val edgeSwipeToClose: Boolean = false,
    val doubleTapQuickSwitch: Boolean = true,
    val reachabilityEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val customSearchEngines: List<CustomSearchEngine> = emptyList(),
    
    // Premium features
    val dataSaverEnabled: Boolean = false,
    val hasCompletedOnboarding: Boolean = false,
    val ntpWidgets: List<String> = listOf("favorites", "recent_tabs", "quick_actions"),
    val webNotificationsEnabled: Boolean = true,
    val defaultPageZoom: Int = 100
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
    
    // New Feature updates
    suspend fun updateBottomControls(enabled: Boolean)
    suspend fun updateEdgeSwipeToClose(enabled: Boolean)
    suspend fun updateDoubleTapQuickSwitch(enabled: Boolean)
    suspend fun updateReachabilityEnabled(enabled: Boolean)
    suspend fun updateHapticFeedbackEnabled(enabled: Boolean)
    suspend fun addCustomSearchEngine(engine: CustomSearchEngine)
    suspend fun removeCustomSearchEngine(id: String)
    
    suspend fun updateDataSaverEnabled(enabled: Boolean)
    suspend fun completeOnboarding()
    suspend fun updateNtpWidgets(widgets: List<String>)
    suspend fun updateWebNotificationsEnabled(enabled: Boolean)
    suspend fun updateDefaultPageZoom(zoom: Int)
}
