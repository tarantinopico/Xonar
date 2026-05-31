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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import com.tarantino.xonarx.domain.model.Tab
import com.tarantino.xonarx.domain.model.Bookmark
import com.tarantino.xonarx.presentation.main.MainUiState
import com.tarantino.xonarx.presentation.main.MainViewModel

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
    val suggestions by browserViewModel.suggestions.collectAsState()
    var isEditingUrl by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BrowserTopBar(
                uiState = uiState,
                isEditingUrl = isEditingUrl,
                onUrlEditStateChange = { isEditingUrl = it },
                onNavigate = { url -> viewModel.navigate(url) },
                onSearchQueryChange = { query -> 
                    uiState.activeIdentity?.let { identity -> 
                        browserViewModel.updateSearchQuery(query, identity.id) 
                    } 
                },
                onMenuClick = { menuExpanded = true },
                onTabCountClick = {
                    uiState.activeTab?.let { activeTab ->
                        browserViewModel.capturePreviewForTab(activeTab.id, activeTab.identityId)
                    }
                    onNavigateToTabSwitcher()
                },
                onSwipeLeft = { viewModel.switchNextIdentity() },
                onSwipeRight = { viewModel.switchPreviousIdentity() },
                menuContent = {
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
                            onAddToFavoritesClick = { viewModel.addToFavorites() },
                            onNavigateForward = {
                                uiState.activeTab?.let { activeTab ->
                                    val session = browserViewModel.sessionManager.getOrCreateSession(activeTab.id, activeTab.identityId)
                                    session.webView?.goForward()
                                }
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val activeTab = uiState.activeTab
            if (activeTab != null && activeTab.url.isNotEmpty() && activeTab.url != "about:blank" && activeTab.url != "Loading...") {
                WebViewContainer(
                    tab = activeTab,
                    sessionManager = browserViewModel.sessionManager,
                    modifier = Modifier.fillMaxSize(),
                    onPageUpdate = { url, title -> 
                        viewModel.updateActiveTabUrl(url, title)
                        viewModel.addToHistory(url, title)
                    }
                )
            } else {
                EmptyBrowserState(
                    favorites = uiState.favorites,
                    onFavoriteClick = { url -> viewModel.navigate(url) }
                )
            }


            AnimatedVisibility(
                visible = isEditingUrl,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
                    ) {
                        if (suggestions.isNotEmpty()) {
                            Text("Suggestions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            suggestions.forEach { url ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.navigate(url)
                                            isEditingUrl = false
                                        }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = url,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        } else {
                            Text("No suggestions available", modifier = Modifier.padding(vertical = 8.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WebViewContainer(
    tab: Tab,
    sessionManager: BrowserSessionManager,
    modifier: Modifier = Modifier,
    onPageUpdate: (String, String?) -> Unit = { _, _ -> }
) {
    val session = remember(tab.id) { sessionManager.getOrCreateSession(tab.id, tab.identityId) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val wv = session.webView ?: error("WebView should not be null here")
            wv.onPageUpdate = onPageUpdate
            wv
        },
        update = { webView ->
            webView.onPageUpdate = onPageUpdate
            if (webView.url != tab.url && tab.url.isNotEmpty()) {
                webView.loadUrl(tab.url)
            }
        }
    )
}

@Composable
fun EmptyBrowserState(favorites: List<Bookmark>, onFavoriteClick: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Search or type web address", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(32.dp))
            if (favorites.isNotEmpty()) {
                // simple grid for favorites
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                ) {
                    items(favorites.size) { index ->
                        val fav = favorites[index]
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onFavoriteClick(fav.url) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                // Real app would load favicon, for now fallback to initial
                                Text(
                                    text = fav.title.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = fav.title,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserTopBar(
    uiState: MainUiState,
    isEditingUrl: Boolean,
    onUrlEditStateChange: (Boolean) -> Unit,
    onNavigate: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    onTabCountClick: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    menuContent: @Composable () -> Unit
) {
    val tabCount = uiState.tabs.size
    val currentUrl = uiState.activeTab?.url ?: ""
    var urlInput by remember(currentUrl) { 
        mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(currentUrl)) 
    }
    val focusRequester = remember { FocusRequester() }

    // When editing starts, select all
    LaunchedEffect(isEditingUrl) {
        if (isEditingUrl) {
            urlInput = urlInput.copy(selection = androidx.compose.ui.text.TextRange(0, urlInput.text.length))
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(urlInput.text) {
        if (isEditingUrl) {
            onSearchQueryChange(urlInput.text)
        }
    }

    var horizontalDragAmount by remember { mutableStateOf(0f) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .pointerInput(isEditingUrl) {
                if (isEditingUrl) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { horizontalDragAmount = 0f },
                    onDragEnd = {
                        if (horizontalDragAmount > 50) {
                            onSwipeRight()
                        } else if (horizontalDragAmount < -50) {
                            onSwipeLeft()
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        horizontalDragAmount += dragAmount
                    }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isEditingUrl) {
                Box {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    menuContent()
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
                                onNavigate(urlInput.text)
                                onUrlEditStateChange(false)
                            }
                        ),
                        placeholder = { Text("Search or type URL") }
                    )
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
    onAddToFavoritesClick: () -> Unit,
    onNavigateForward: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("Forward") },
            onClick = {
                onNavigateForward()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("Add to Favorites") },
            onClick = {
                onAddToFavoritesClick()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) }
        )
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
