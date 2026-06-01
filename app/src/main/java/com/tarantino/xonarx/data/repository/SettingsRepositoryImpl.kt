package com.tarantino.xonarx.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.tarantino.xonarx.domain.repository.AppPreferences
import com.tarantino.xonarx.domain.repository.CustomSearchEngine
import com.tarantino.xonarx.domain.repository.SettingsRepository
import com.tarantino.xonarx.domain.repository.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
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
        
        val BOTTOM_CONTROLS = booleanPreferencesKey("bottom_controls")
        val EDGE_SWIPE_TO_CLOSE = booleanPreferencesKey("edge_swipe_to_close")
        val DOUBLE_TAP_QUICK_SWITCH = booleanPreferencesKey("double_tap_quick_switch")
        val REACHABILITY = booleanPreferencesKey("reachability")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val CUSTOM_SEARCH_ENGINES = stringPreferencesKey("custom_search_engines")
        
        val DATA_SAVER = booleanPreferencesKey("data_saver")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val NTP_WIDGETS = stringPreferencesKey("ntp_widgets")
        val WEB_NOTIFICATIONS = booleanPreferencesKey("web_notifications")
        val DEFAULT_PAGE_ZOOM = intPreferencesKey("default_page_zoom")
        val THUMBNAIL_SIZE = stringPreferencesKey("thumbnail_size")

        val SMART_URL_COPY = booleanPreferencesKey("smart_url_copy")
        val BACKGROUND_VIDEO_PLAYBACK = booleanPreferencesKey("background_video_playback")
    }

    override val preferences: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        val themeModeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val customSearchEnginesJson = prefs[Keys.CUSTOM_SEARCH_ENGINES] ?: "[]"
        val customSearchEnginesList = parseCustomSearchEngines(customSearchEnginesJson)
        val ntpWidgetsStr = prefs[Keys.NTP_WIDGETS] ?: "favorites,recent_tabs,quick_actions"
        val ntpWidgetsList = ntpWidgetsStr.split(",").filter { it.isNotBlank() }
        
        AppPreferences(
            lastActiveIdentityId = prefs[Keys.LAST_IDENTITY_ID],
            themeMode = runCatching { ThemeMode.valueOf(themeModeStr) }.getOrDefault(ThemeMode.SYSTEM),
            useMaterialYou = prefs[Keys.MATERIAL_YOU] ?: true,
            gesturesEnabled = prefs[Keys.GESTURES_ENABLED] ?: true,
            searchEngineUrl = prefs[Keys.SEARCH_ENGINE] ?: "https://www.google.com/search?q=",
            biometricsEnabled = prefs[Keys.BIOMETRICS_ENABLED] ?: true,
            autoClearOnExit = prefs[Keys.AUTO_CLEAR] ?: false,
            bottomControls = prefs[Keys.BOTTOM_CONTROLS] ?: false,
            edgeSwipeToClose = prefs[Keys.EDGE_SWIPE_TO_CLOSE] ?: false,
            doubleTapQuickSwitch = prefs[Keys.DOUBLE_TAP_QUICK_SWITCH] ?: true,
            reachabilityEnabled = prefs[Keys.REACHABILITY] ?: true,
            hapticFeedbackEnabled = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
            customSearchEngines = customSearchEnginesList,
            dataSaverEnabled = prefs[Keys.DATA_SAVER] ?: false,
            hasCompletedOnboarding = prefs[Keys.HAS_COMPLETED_ONBOARDING] ?: false,
            ntpWidgets = ntpWidgetsList,
            webNotificationsEnabled = prefs[Keys.WEB_NOTIFICATIONS] ?: true,
            defaultPageZoom = prefs[Keys.DEFAULT_PAGE_ZOOM] ?: 100,
            thumbnailSize = prefs[Keys.THUMBNAIL_SIZE] ?: "medium",
            smartUrlCopyEnabled = prefs[Keys.SMART_URL_COPY] ?: true,
            backgroundVideoPlayback = prefs[Keys.BACKGROUND_VIDEO_PLAYBACK] ?: false
        )
    }

    private fun parseCustomSearchEngines(json: String): List<CustomSearchEngine> {
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<CustomSearchEngine>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CustomSearchEngine(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        urlTemplate = obj.getString("urlTemplate")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeCustomSearchEngines(list: List<CustomSearchEngine>): String {
        val array = JSONArray()
        list.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("name", it.name)
            obj.put("urlTemplate", it.urlTemplate)
            array.put(obj)
        }
        return array.toString()
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

    override suspend fun updateBottomControls(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.BOTTOM_CONTROLS] = enabled }
    }

    override suspend fun updateEdgeSwipeToClose(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.EDGE_SWIPE_TO_CLOSE] = enabled }
    }

    override suspend fun updateDoubleTapQuickSwitch(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.DOUBLE_TAP_QUICK_SWITCH] = enabled }
    }

    override suspend fun updateReachabilityEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.REACHABILITY] = enabled }
    }

    override suspend fun updateHapticFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.HAPTIC_FEEDBACK] = enabled }
    }

    override suspend fun addCustomSearchEngine(engine: CustomSearchEngine) {
        context.dataStore.edit { prefs ->
            val json = prefs[Keys.CUSTOM_SEARCH_ENGINES] ?: "[]"
            val current = parseCustomSearchEngines(json).toMutableList()
            current.add(engine)
            prefs[Keys.CUSTOM_SEARCH_ENGINES] = serializeCustomSearchEngines(current)
        }
    }

    override suspend fun removeCustomSearchEngine(id: String) {
        context.dataStore.edit { prefs ->
            val json = prefs[Keys.CUSTOM_SEARCH_ENGINES] ?: "[]"
            val current = parseCustomSearchEngines(json).toMutableList()
            current.removeAll { it.id == id }
            prefs[Keys.CUSTOM_SEARCH_ENGINES] = serializeCustomSearchEngines(current)
        }
    }

    override suspend fun updateDataSaverEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.DATA_SAVER] = enabled }
    }

    override suspend fun completeOnboarding() {
        context.dataStore.edit { prefs -> prefs[Keys.HAS_COMPLETED_ONBOARDING] = true }
    }

    override suspend fun updateNtpWidgets(widgets: List<String>) {
        context.dataStore.edit { prefs -> prefs[Keys.NTP_WIDGETS] = widgets.joinToString(",") }
    }

    override suspend fun updateWebNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.WEB_NOTIFICATIONS] = enabled }
    }

    override suspend fun updateDefaultPageZoom(zoom: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.DEFAULT_PAGE_ZOOM] = zoom }
    }

    override suspend fun updateThumbnailSize(size: String) {
        context.dataStore.edit { prefs -> prefs[Keys.THUMBNAIL_SIZE] = size }
    }

    override suspend fun updateSmartUrlCopyEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SMART_URL_COPY] = enabled }
    }

    override suspend fun updateBackgroundVideoPlayback(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.BACKGROUND_VIDEO_PLAYBACK] = enabled }
    }
}

