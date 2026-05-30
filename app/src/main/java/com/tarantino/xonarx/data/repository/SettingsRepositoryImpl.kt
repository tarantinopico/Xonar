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
    }

    override val preferences: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        val themeModeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        AppPreferences(
            lastActiveIdentityId = prefs[Keys.LAST_IDENTITY_ID],
            themeMode = runCatching { ThemeMode.valueOf(themeModeStr) }.getOrDefault(ThemeMode.SYSTEM),
            useMaterialYou = prefs[Keys.MATERIAL_YOU] ?: true
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
}
