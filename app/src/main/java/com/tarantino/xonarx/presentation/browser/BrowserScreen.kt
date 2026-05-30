package com.tarantino.xonarx.presentation.browser

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.viewinterop.AndroidView
import com.tarantino.xonarx.domain.model.Tab
import com.tarantino.xonarx.presentation.main.MainUiState
import com.tarantino.xonarx.presentation.main.MainViewModel

import com.tarantino.xonarx.domain.usecase.ReaderModeEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    viewModel: MainViewModel = hiltViewModel(),
    browserViewModel: BrowserViewModel = hiltViewModel(),
    onNavigateToTabSwitcher: () -> Unit,
    onNavigateToIdentityManager: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToNotes: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isEditingUrl by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BrowserBottomBar(
                uiState = uiState,
                isEditingUrl = isEditingUrl,
                onUrlEditStateChange = { isEditingUrl = it },
                onNavigate = { url -> viewModel.openTab(url) },
                onMenuClick = { menuExpanded = true },
                onTabCountClick = onNavigateToTabSwitcher
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val activeTab = uiState.activeTab
            if (activeTab != null) {
                WebViewContainer(
                    tab = activeTab,
                    sessionManager = browserViewModel.sessionManager,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                EmptyBrowserState()
            }

            if (menuExpanded) {
                BrowserMenu(
                    expanded = menuExpanded,
                    onDismiss = { menuExpanded = false },
                    onNavigateToIdentityManager = onNavigateToIdentityManager,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToBookmarks = onNavigateToBookmarks,
                    onNavigateToDownloads = onNavigateToDownloads,
                    onNavigateToNotes = onNavigateToNotes,
                    onReaderModeClick = {
                        val session = activeTab?.id?.let { browserViewModel.sessionManager.getOrCreateSession(it, activeTab.identityId) }
                        session?.webView?.let { wv ->
                            // Inject reader mode Engine here maybe via a static method or ViewModel instance.
                            // We will supply it via BrowserViewModel.
                            browserViewModel.readerModeEngine.enableReaderMode(wv)
                        }
                    }
                )
            }
            
            // Render Suggestions if editing
            AnimatedVisibility(
                visible = isEditingUrl,
                enter = expandVertically(expandFrom = Alignment.Bottom),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Search Suggestions", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        // Mocked suggestions
                        listOf("https://google.com", "https://news.ycombinator.com", "https://github.com").forEach { url ->
                            Text(
                                text = url,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.openTab(url)
                                        isEditingUrl = false
                                    }
                                    .padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WebViewContainer(tab: Tab, sessionManager: BrowserSessionManager, modifier: Modifier = Modifier) {
    val session = remember(tab.id) { sessionManager.getOrCreateSession(tab.id, tab.identityId) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            session.webView ?: error("WebView should not be null here")
        },
        update = { webView ->
            if (webView.url != tab.url && tab.url.isNotEmpty()) {
                webView.loadUrl(tab.url)
            }
        }
    )
}

@Composable
fun EmptyBrowserState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Search or type web address", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserBottomBar(
    uiState: MainUiState,
    isEditingUrl: Boolean,
    onUrlEditStateChange: (Boolean) -> Unit,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit,
    onTabCountClick: () -> Unit
) {
    val tabCount = uiState.tabs.size
    val currentUrl = uiState.activeTab?.url ?: ""
    var urlInput by remember(currentUrl) { mutableStateOf(currentUrl) }
    val focusRequester = remember { FocusRequester() }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isEditingUrl) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
            } else {
                IconButton(onClick = { onUrlEditStateChange(false) }) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        if (!isEditingUrl) onUrlEditStateChange(true)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (isEditingUrl) {
                    TextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                onNavigate(if (urlInput.startsWith("http")) urlInput else "https://$urlInput")
                                onUrlEditStateChange(false)
                            }
                        ),
                        placeholder = { Text("Search or type URL") }
                    )
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = "Secure", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentUrl.isEmpty()) "Search or type URL" else currentUrl,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (!isEditingUrl) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onTabCountClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = tabCount.toString(), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun BrowserMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onNavigateToIdentityManager: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onReaderModeClick: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("Identities") },
            onClick = {
                onNavigateToIdentityManager()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("History") },
            onClick = {
                onNavigateToHistory()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.History, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("Bookmarks") },
            onClick = {
                onNavigateToBookmarks()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("Downloads") },
            onClick = {
                onNavigateToDownloads()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("Notes") },
            onClick = {
                onNavigateToNotes()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("Reader Mode") },
            onClick = {
                onReaderModeClick()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Settings") },
            onClick = {
                onNavigateToSettings()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
        )
    }
}
