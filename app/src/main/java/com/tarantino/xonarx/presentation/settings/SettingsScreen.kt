package com.tarantino.xonarx.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarantino.xonarx.domain.repository.AppPreferences
import com.tarantino.xonarx.domain.repository.SettingsRepository
import com.tarantino.xonarx.domain.repository.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.tarantino.xonarx.domain.usecase.AdBlockerEngine

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val adBlockerEngine: AdBlockerEngine
) : ViewModel() {
    val preferences: StateFlow<AppPreferences> = settingsRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferences())
        
    val isBlockingEnabled: StateFlow<Boolean> = adBlockerEngine.isBlockingEnabled

    fun updateTheme(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(mode)
        }
    }

    fun toggleMaterialYou(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateMaterialYou(enabled)
        }
    }
    
    fun toggleAdBlocking(enabled: Boolean) {
        adBlockerEngine.setBlockingEnabled(enabled)
    }

    fun toggleGestures(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateGesturesEnabled(enabled)
        }
    }
    
    fun toggleBiometrics(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateBiometricsEnabled(enabled)
        }
    }
    
    fun toggleAutoClear(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateAutoClearOnExit(enabled)
        }
    }
    
    fun setSearchEngine(url: String) {
        viewModelScope.launch {
            settingsRepository.updateSearchEngine(url)
        }
    }

    // New preferences
    fun toggleBottomControls(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateBottomControls(enabled)
        }
    }
    fun toggleEdgeSwipeToClose(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateEdgeSwipeToClose(enabled)
        }
    }
    fun toggleDoubleTapQuickSwitch(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateDoubleTapQuickSwitch(enabled)
        }
    }
    fun toggleReachability(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateReachabilityEnabled(enabled)
        }
    }
    fun toggleHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateHapticFeedbackEnabled(enabled)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val prefs by viewModel.preferences.collectAsState()
    val isBlockingEnabled by viewModel.isBlockingEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Text(
                    text = "Appearance",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Theme Mode") },
                    supportingContent = { Text(prefs.themeMode.name) },
                    modifier = Modifier.fillMaxWidth().clickable {
                        // For the future: Add Theme mode selector
                        val newMode = when (prefs.themeMode) {
                            ThemeMode.LIGHT -> ThemeMode.DARK
                            ThemeMode.DARK -> ThemeMode.SYSTEM
                            ThemeMode.SYSTEM -> ThemeMode.LIGHT
                        }
                        viewModel.updateTheme(newMode)
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Use Material You Colors") },
                    trailingContent = {
                        Switch(
                            checked = prefs.useMaterialYou,
                            onCheckedChange = { viewModel.toggleMaterialYou(it) }
                        )
                    }
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Browser Navigation",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Bottom Controls") },
                    supportingContent = { Text("Move toolbar and omnibox to the bottom") },
                    trailingContent = {
                        Switch(
                            checked = prefs.bottomControls,
                            onCheckedChange = { viewModel.toggleBottomControls(it) }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Edge swipe to close tab") },
                    supportingContent = { Text("Swipe inward from edges to close the current tab") },
                    trailingContent = {
                        Switch(
                            checked = prefs.edgeSwipeToClose,
                            onCheckedChange = { viewModel.toggleEdgeSwipeToClose(it) }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Double tap to quick switch") },
                    supportingContent = { Text("Double tap the toolbar to switch to previous tab") },
                    trailingContent = {
                        Switch(
                            checked = prefs.doubleTapQuickSwitch,
                            onCheckedChange = { viewModel.toggleDoubleTapQuickSwitch(it) }
                        )
                    }
                )
            }
             item {
                ListItem(
                    headlineContent = { Text("Reachability (One-handed mode)") },
                    supportingContent = { Text("Swipe down on toolbar to lower the screen") },
                    trailingContent = {
                        Switch(
                            checked = prefs.reachabilityEnabled,
                            onCheckedChange = { viewModel.toggleReachability(it) }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Haptic Feedback") },
                    supportingContent = { Text("Vibrate on key interactions") },
                    trailingContent = {
                        Switch(
                            checked = prefs.hapticFeedbackEnabled,
                            onCheckedChange = { viewModel.toggleHapticFeedback(it) }
                        )
                    }
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Search",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Default Search Engine") },
                    supportingContent = { Text(if (prefs.searchEngineUrl.contains("google", ignoreCase = true)) "Google" else "Custom") }
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Privacy & Security",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Require Biometrics") },
                    supportingContent = { Text("Lock identities behind biometric prompt") },
                    trailingContent = {
                        Switch(
                            checked = prefs.biometricsEnabled,
                            onCheckedChange = { viewModel.toggleBiometrics(it) }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Clear data on exit") },
                    supportingContent = { Text("Automatically clear history and cache") },
                    trailingContent = {
                        Switch(
                            checked = prefs.autoClearOnExit,
                            onCheckedChange = { viewModel.toggleAutoClear(it) }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Ad and Tracker Blocking") },
                    supportingContent = { Text("Block known trackers and ads") },
                    trailingContent = {
                        Switch(
                            checked = isBlockingEnabled,
                            onCheckedChange = { viewModel.toggleAdBlocking(it) }
                        )
                    }
                )
            }
             item {
                ListItem(
                    headlineContent = { Text("Clear browsing data") },
                    supportingContent = { Text("Clear history, cookies, cache, and more") }
                )
            }
        }
    }
}
