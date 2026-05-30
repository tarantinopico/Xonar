package com.tarantino.xonarx.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.tarantino.xonarx.domain.repository.AppPreferences
import com.tarantino.xonarx.domain.repository.SettingsRepository
import com.tarantino.xonarx.domain.repository.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "xonar_settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object Keys {
        val LAST_IDENTITY_ID = stringPreferencesKey("last_identity_id")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val MATERIAL_YOU = booleanPreferencesKey("material_you")
        val GESTURES_ENABLED = booleanPreferencesKey("gestures_enabled")
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val BIOMETRICS_ENABLED = booleanPreferencesKey("biometrics_enabled")
        val AUTO_CLEAR = booleanPreferencesKey("auto_clear")
    }

    override val preferences: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        val themeModeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        AppPreferences(
            lastActiveIdentityId = prefs[Keys.LAST_IDENTITY_ID],
            themeMode = runCatching { ThemeMode.valueOf(themeModeStr) }.getOrDefault(ThemeMode.SYSTEM),
            useMaterialYou = prefs[Keys.MATERIAL_YOU] ?: true,
            gesturesEnabled = prefs[Keys.GESTURES_ENABLED] ?: true,
            searchEngineUrl = prefs[Keys.SEARCH_ENGINE] ?: "https://www.google.com/search?q=",
            biometricsEnabled = prefs[Keys.BIOMETRICS_ENABLED] ?: true,
            autoClearOnExit = prefs[Keys.AUTO_CLEAR] ?: false
        )
    }

    override suspend fun updateLastActiveIdentityId(id: String?) {
        context.dataStore.edit { prefs ->
            if (id == null) {
                prefs.remove(Keys.LAST_IDENTITY_ID)
            } else {
                prefs[Keys.LAST_IDENTITY_ID] = id
            }
        }
    }

    override suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs -> prefs[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun updateMaterialYou(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.MATERIAL_YOU] = enabled }
    }

    override suspend fun updateGesturesEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.GESTURES_ENABLED] = enabled }
    }

    override suspend fun updateSearchEngine(url: String) {
        context.dataStore.edit { prefs -> prefs[Keys.SEARCH_ENGINE] = url }
    }

    override suspend fun updateBiometricsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.BIOMETRICS_ENABLED] = enabled }
    }

    override suspend fun updateAutoClearOnExit(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.AUTO_CLEAR] = enabled }
    }
}
