package com.tarantino.xonarx.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.tarantino.xonarx.presentation.theme.futuristic.AnimatedGradientBackdrop
import com.tarantino.xonarx.presentation.theme.futuristic.DepthCard
import com.tarantino.xonarx.presentation.theme.futuristic.FrostedGlassSurface


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

    fun updateThumbnailSize(size: String) {
        viewModelScope.launch {
            settingsRepository.updateThumbnailSize(size)
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
    
    fun toggleDataSaver(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateDataSaverEnabled(enabled)
        }
    }

    fun toggleSmartUrlCopy(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSmartUrlCopyEnabled(enabled)
        }
    }

    fun toggleBackgroundVideoPlayback(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateBackgroundVideoPlayback(enabled)
        }
    }

    fun toggleAskBeforeDownloading(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateAskBeforeDownloading(enabled)
        }
    }

    fun toggleWifiOnlyDownloads(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateWifiOnlyDownloads(enabled)
        }
    }

    fun toggleShowDownloadNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateShowDownloadNotifications(enabled)
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

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AnimatedGradientBackdrop(
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
            )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                FrostedGlassSurface(
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    blurRadius = 24.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TopAppBar(
                        title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    SettingsCategory(title = "Appearance") {
                        SettingsItem(
                            title = "Theme Mode",
                            subtitle = prefs.themeMode.name,
                            onClick = {
                                val newMode = when (prefs.themeMode) {
                                    ThemeMode.LIGHT -> ThemeMode.DARK
                                    ThemeMode.DARK -> ThemeMode.SYSTEM
                                    ThemeMode.SYSTEM -> ThemeMode.LIGHT
                                }
                                viewModel.updateTheme(newMode)
                            }
                        )
                        SettingsItem(
                            title = "Use Material You Colors",
                            trailing = {
                                Switch(
                                    checked = prefs.useMaterialYou,
                                    onCheckedChange = { viewModel.toggleMaterialYou(it) }
                                )
                            }
                        )
                        SettingsItem(
                            title = "Tab Thumbnail Size",
                            subtitle = prefs.thumbnailSize.replaceFirstChar { it.uppercase() },
                            onClick = {
                                val nextSize = when(prefs.thumbnailSize) {
                                    "small" -> "medium"
                                    "medium" -> "large"
                                    else -> "small"
                                }
                                viewModel.updateThumbnailSize(nextSize)
                            }
                        )
                    }
                }
                
                item {
                    SettingsCategory(title = "Browser Navigation") {
                        SettingsItem(
                            title = "Data Saver",
                            subtitle = "Reduce data usage by blocking images",
                            trailing = {
                                Switch(
                                    checked = prefs.dataSaverEnabled,
                                    onCheckedChange = { viewModel.toggleDataSaver(it) }
                                )
                            }
                        )
                        SettingsItem(
                            title = "Smart URL Copy",
                            subtitle = "Strip scheme and slash when copying URLs",
                            trailing = {
                                Switch(
                                    checked = prefs.smartUrlCopyEnabled,
                                    onCheckedChange = { viewModel.toggleSmartUrlCopy(it) }
                                )
                            }
                        )
                        SettingsItem(
                            title = "Background Video",
                            subtitle = "Allow video playback when app is in background",
                            trailing = {
                                Switch(
                                    checked = prefs.backgroundVideoPlayback,
                                    onCheckedChange = { viewModel.toggleBackgroundVideoPlayback(it) }
                                )
                            }
                        )
                        SettingsItem(
                            title = "Bottom Controls",
                            subtitle = "Move toolbar to bottom",
                            trailing = {
                                Switch(
                                    checked = prefs.bottomControls,
                                    onCheckedChange = { viewModel.toggleBottomControls(it) }
                                )
                            }
                        )
                        SettingsItem(
                            title = "Edge swipe to close",
                            subtitle = "Swipe inward to close tabs",
                            trailing = {
                                Switch(
                                    checked = prefs.edgeSwipeToClose,
                                    onCheckedChange = { viewModel.toggleEdgeSwipeToClose(it) }
                                )
                            }
                        )
                        SettingsItem(
                            title = "Double tap quick switch",
                            subtitle = "Double tap toolbar to switch tabs",
                            trailing = {
                                Switch(
                                    checked = prefs.doubleTapQuickSwitch,
                                    onCheckedChange = { viewModel.toggleDoubleTapQuickSwitch(it) }
                                )
                            }
                        )
                        SettingsItem(
                            title = "Reachability",
                            subtitle = "Swipe down on toolbar",
                            trailing = {
                                Switch(
                                    checked = prefs.reachabilityEnabled,
                                    onCheckedChange = { viewModel.toggleReachability(it) }
                                )
                            }
                        )
                        SettingsItem(
                            title = "Haptic Feedback",
                            subtitle = "Vibrate on interactions",
                            trailing = {
                                Switch(
                                    checked = prefs.hapticFeedbackEnabled,
                                    onCheckedChange = { viewModel.toggleHapticFeedback(it) }
                                )
                            }
                        )
                    }
                }
                
                item {
                    SettingsCategory(title = "Downloads") {
                        SettingsItem(
                            title = "Ask Before Downloading",
                            subtitle = "Show confirmation dialog",
                            trailing = {
                                Switch(
                                    checked = prefs.askBeforeDownloading,
                                    onCheckedChange = { viewModel.toggleAskBeforeDownloading(it) }
                                )
                            }
                        )
                        SettingsItem(
                            title = "Wi-Fi Only Downloads",
                            subtitle = "Pause downloads on mobile data",
                            trailing = {
                                Switch(
                                    checked = prefs.wifiOnlyDownloads,
                                    onCheckedChange = { viewModel.toggleWifiOnlyDownloads(it) }
                                )
                            }
                        )
                        SettingsItem(
                            title = "Show Notifications",
                            subtitle = "Display progress in status bar",
                            trailing = {
                                Switch(
                                    checked = prefs.showDownloadNotifications,
                                    onCheckedChange = { viewModel.toggleShowDownloadNotifications(it) }
                                )
                            }
                        )
                    }
                }

                item {
                    SettingsCategory(title = "Data & Privacy") {
                        SettingsItem(
                            title = "Require Biometric Unlock",
                            trailing = {
                                Switch(
                                    checked = prefs.biometricsEnabled,
                                    onCheckedChange = { viewModel.toggleBiometrics(it) }
                                )
                            }
                        )
                        SettingsItem(
                            title = "Auto-Clear Data on Exit",
                            trailing = {
                                Switch(
                                    checked = prefs.autoClearOnExit,
                                    onCheckedChange = { viewModel.toggleAutoClear(it) }
                                )
                            }
                        )
                        SettingsItem(
                            title = "Ad Blocker Engine",
                            subtitle = if (isBlockingEnabled) "Enabled" else "Disabled",
                            trailing = {
                                Switch(
                                    checked = isBlockingEnabled,
                                    onCheckedChange = { viewModel.toggleAdBlocking(it) }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCategory(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title.uppercase(),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 16.dp)
        )
        DepthCard(modifier = Modifier.fillMaxWidth()) {
            FrostedGlassSurface(
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                blurRadius = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (trailing != null) {
            Box(modifier = Modifier.padding(start = 8.dp)) {
                trailing()
            }
        }
    }
}
