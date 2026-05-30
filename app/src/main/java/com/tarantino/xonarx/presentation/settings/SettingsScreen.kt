package com.tarantino.xonarx.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
                    modifier = Modifier.fillMaxWidth()
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
                    text = "Privacy",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
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
