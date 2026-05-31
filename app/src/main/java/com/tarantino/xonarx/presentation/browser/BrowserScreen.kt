package com.tarantino.xonarx.presentation.browser

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.input.key.*
import androidx.compose.foundation.focusable
import com.tarantino.xonarx.domain.model.Tab
import com.tarantino.xonarx.domain.model.Bookmark
import com.tarantino.xonarx.presentation.main.MainUiState
import com.tarantino.xonarx.presentation.main.MainViewModel
import com.tarantino.xonarx.presentation.theme.futuristic.AnimatedGradientBackdrop
import com.tarantino.xonarx.presentation.theme.futuristic.DepthCard
import com.tarantino.xonarx.presentation.theme.futuristic.FrostedGlassSurface

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
    onNavigateToNotes: () -> Unit,
    onNavigateToPrivacyStats: () -> Unit,
    onNavigateToUserscripts: () -> Unit,
    onNavigateToFeeds: () -> Unit,
    onNavigateToAmbientMode: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferences by browserViewModel.preferences.collectAsState()
    val suggestions by browserViewModel.suggestions.collectAsState()
    var isEditingUrl by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showGroupDialog by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isPipMode = remember(configuration) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            (context as? android.app.Activity)?.isInPictureInPictureMode == true
        } else false
    }

    LaunchedEffect(uiState.activeIdentity?.id) {
        uiState.activeIdentity?.id?.let { identityId ->
            browserViewModel.updateIdentity(identityId)
        }
    }

    LaunchedEffect(preferences.dataSaverEnabled, uiState.activeTab?.id) {
        uiState.activeTab?.id?.let { activeTabId ->
            val session = browserViewModel.sessionManager.getOrCreateSession(activeTabId, uiState.activeTab?.identityId ?: "")
            session.webView?.setDataSaverEnabled(preferences.dataSaverEnabled)
        }
    }

    if (showGroupDialog) {
        val activeIdentityId = uiState.activeIdentity?.id
        val availableGroups = uiState.tabGroups.filter { it.identityId == activeIdentityId }
        AlertDialog(
            onDismissRequest = { showGroupDialog = false },
            title = { Text("Add to Group") },
            text = {
                Column {
                    if (availableGroups.isEmpty()) {
                        Text("No groups available.")
                    } else {
                        availableGroups.forEach { group ->
                            Text(
                                text = group.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        uiState.activeTab?.let { tab ->
                                            viewModel.moveTabToGroup(tab.id, group.id)
                                        }
                                        showGroupDialog = false
                                    }
                                    .padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGroupDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.isCtrlPressed && event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.T -> {
                            viewModel.openTab("")
                            true
                        }
                        Key.W -> {
                            uiState.activeTab?.let { viewModel.closeTab(it) }
                            true
                        }
                        Key.L -> {
                            isEditingUrl = true
                            true
                        }
                        Key.R -> {
                            uiState.activeTab?.let {
                                browserViewModel.sessionManager.getOrCreateSession(it.id, it.identityId).webView?.reload()
                            }
                            true
                        }
                        Key.H -> {
                            onNavigateToHistory()
                            true
                        }
                        Key.B -> {
                            onNavigateToBookmarks()
                            true
                        }
                        Key.J -> {
                            onNavigateToDownloads()
                            true
                        }
                        else -> false
                    }
                } else false
            },
        topBar = {
            if (!preferences.bottomControls && !isPipMode) {
                BrowserTopBar(
                    uiState = uiState,
                    isEditingUrl = isEditingUrl,
                    onUrlEditStateChange = { isEditingUrl = it },
                    onNavigate = { url -> 
                        if (url.startsWith("xonar://")) {
                            when(url) {
                                "xonar://settings" -> onNavigateToSettings()
                                "xonar://history" -> onNavigateToHistory()
                                "xonar://bookmarks" -> onNavigateToBookmarks()
                                "xonar://downloads" -> onNavigateToDownloads()
                                "xonar://notes" -> onNavigateToNotes()
                                "xonar://userscripts" -> onNavigateToUserscripts()
                                "xonar://feeds" -> onNavigateToFeeds()
                            }
                        } else {
                            viewModel.navigate(url) 
                        }
                    },
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
                        // ... menu ...
                        if (menuExpanded) {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            BrowserMenu(
                                expanded = menuExpanded,
                                onDismiss = { menuExpanded = false },
                                onNavigateToIdentityManager = onNavigateToIdentityManager,
                                onNavigateToSettings = onNavigateToSettings,
                                onNavigateToHistory = onNavigateToHistory,
                                onNavigateToBookmarks = onNavigateToBookmarks,
                                onNavigateToDownloads = onNavigateToDownloads,
                                onNavigateToNotes = onNavigateToNotes,
                                onNavigateToPrivacyStats = onNavigateToPrivacyStats,
                                onNavigateToUserscripts = onNavigateToUserscripts,
                                onAddToFavoritesClick = { viewModel.addToFavorites() },
                                onAddToGroupClick = { showGroupDialog = true },
                                onNavigateForward = {
                                    uiState.activeTab?.let { activeTab ->
                                        val session = browserViewModel.sessionManager.getOrCreateSession(activeTab.id, activeTab.identityId)
                                        session.webView?.goForward()
                                    }
                                },
                                onScanQrClick = {
                                    browserViewModel.startQrScan { result ->
                                        viewModel.navigate(result)
                                    }
                                },
                                onPrintPdfClick = {
                                    uiState.activeTab?.let { activeTab ->
                                        val session = browserViewModel.sessionManager.getOrCreateSession(activeTab.id, activeTab.identityId)
                                        val wv = session.webView
                                        if (wv != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                            val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as? android.print.PrintManager
                                            val printAdapter = wv.createPrintDocumentAdapter("Xonar_${activeTab.title}")
                                            val printAttributes = android.print.PrintAttributes.Builder()
                                                .setMediaSize(android.print.PrintAttributes.MediaSize.ISO_A4)
                                                .build()
                                            printManager?.print("Xonar Document", printAdapter, printAttributes)
                                        }
                                    }
                                },
                                onEnterPipClick = {
                                    val activity = context as? android.app.Activity
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        val params = android.app.PictureInPictureParams.Builder()
                                            .build()
                                        try {
                                            activity?.enterPictureInPictureMode(params)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                onArPreviewClick = {
                                    android.widget.Toast.makeText(context, "AR Preview not available on this device", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onNavigateToAmbientMode = onNavigateToAmbientMode
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (preferences.bottomControls && !isPipMode) {
                BrowserTopBar(
                    uiState = uiState,
                    isEditingUrl = isEditingUrl,
                    onUrlEditStateChange = { isEditingUrl = it },
                    onNavigate = { url -> 
                        if (url.startsWith("xonar://")) {
                            when(url) {
                                "xonar://settings" -> onNavigateToSettings()
                                "xonar://history" -> onNavigateToHistory()
                                "xonar://bookmarks" -> onNavigateToBookmarks()
                                "xonar://downloads" -> onNavigateToDownloads()
                                "xonar://notes" -> onNavigateToNotes()
                                "xonar://userscripts" -> onNavigateToUserscripts()
                                "xonar://feeds" -> onNavigateToFeeds()
                            }
                        } else {
                            viewModel.navigate(url) 
                        }
                    },
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
                        // ... menu ...
                        if (menuExpanded) {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            BrowserMenu(
                                expanded = menuExpanded,
                                onDismiss = { menuExpanded = false },
                                onNavigateToIdentityManager = onNavigateToIdentityManager,
                                onNavigateToSettings = onNavigateToSettings,
                                onNavigateToHistory = onNavigateToHistory,
                                onNavigateToBookmarks = onNavigateToBookmarks,
                                onNavigateToDownloads = onNavigateToDownloads,
                                onNavigateToNotes = onNavigateToNotes,
                                onNavigateToPrivacyStats = onNavigateToPrivacyStats,
                                onNavigateToUserscripts = onNavigateToUserscripts,
                                onAddToFavoritesClick = { viewModel.addToFavorites() },
                                onAddToGroupClick = { showGroupDialog = true },
                                onNavigateForward = {
                                    uiState.activeTab?.let { activeTab ->
                                        val session = browserViewModel.sessionManager.getOrCreateSession(activeTab.id, activeTab.identityId)
                                        session.webView?.goForward()
                                    }
                                },
                                onScanQrClick = {
                                    browserViewModel.startQrScan { result ->
                                        viewModel.navigate(result)
                                    }
                                },
                                onPrintPdfClick = {
                                    uiState.activeTab?.let { activeTab ->
                                        val session = browserViewModel.sessionManager.getOrCreateSession(activeTab.id, activeTab.identityId)
                                        val wv = session.webView
                                        if (wv != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                            val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as? android.print.PrintManager
                                            val printAdapter = wv.createPrintDocumentAdapter("Xonar_${activeTab.title}")
                                            val printAttributes = android.print.PrintAttributes.Builder()
                                                .setMediaSize(android.print.PrintAttributes.MediaSize.ISO_A4)
                                                .build()
                                            printManager?.print("Xonar Document", printAdapter, printAttributes)
                                        }
                                    }
                                },
                                onEnterPipClick = {
                                    val activity = context as? android.app.Activity
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        val params = android.app.PictureInPictureParams.Builder()
                                            .build()
                                        try {
                                            activity?.enterPictureInPictureMode(params)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                onArPreviewClick = {
                                    android.widget.Toast.makeText(context, "AR Preview not available on this device", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onNavigateToAmbientMode = onNavigateToAmbientMode
                            )
                        }
                    }
                )
            }
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
                NewTabDashboard(
                    favorites = uiState.favorites,
                    onFavoriteClick = { url -> viewModel.navigate(url) },
                    onVoiceSearchClick = { /* Not fully implemented */ },
                    onQrScanClick = {
                        browserViewModel.startQrScan { result ->
                            viewModel.navigate(result)
                        }
                    },
                    onNewGroupClick = {
                        val activeIdentityId = uiState.activeIdentity?.id
                        if (activeIdentityId != null) {
                            viewModel.createTabGroup("New Group", android.graphics.Color.BLUE, emptyList())
                        }
                    },
                    onRecentTabsClick = onNavigateToHistory,
                    onNotesClick = onNavigateToNotes
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
                                            if (url.startsWith("xonar://")) {
                                                when(url) {
                                                    "xonar://settings" -> onNavigateToSettings()
                                                    "xonar://history" -> onNavigateToHistory()
                                                    "xonar://bookmarks" -> onNavigateToBookmarks()
                                                    "xonar://downloads" -> onNavigateToDownloads()
                                                    "xonar://notes" -> onNavigateToNotes()
                                                    "xonar://userscripts" -> onNavigateToUserscripts()
                                                    "xonar://feeds" -> onNavigateToFeeds()
                                                }
                                            } else {
                                                viewModel.navigate(url)
                                            }
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

            // Tab Group Strip
            val activeGroup = activeTab?.groupId?.let { gid -> uiState.tabGroups.find { it.id == gid } }
            if (activeGroup != null && !isPipMode) {
                val groupTabs = uiState.tabs.filter { it.groupId == activeGroup.id }
                TabGroupStrip(
                    group = activeGroup,
                    tabs = groupTabs,
                    activeTabId = activeTab.id,
                    onTabSelected = { viewModel.selectTab(it) },
                    onTabClosed = { viewModel.closeTab(it) },
                    onAddTab = { viewModel.openTab("about:blank", activeGroup.id) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun TabGroupStrip(
    group: com.tarantino.xonarx.domain.model.TabGroup,
    tabs: List<Tab>,
    activeTabId: String,
    onTabSelected: (Tab) -> Unit,
    onTabClosed: (Tab) -> Unit,
    onAddTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        FrostedGlassSurface(
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            borderColor = Color(group.color).copy(alpha = 0.3f),
            blurRadius = 24.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(group.color)))
                        Spacer(Modifier.width(8.dp))
                        Text(text = group.name, style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = onAddTab, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "New tab in group", tint = Color(group.color))
                    }
                }
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(tabs.size, key = { tabs[it].id }) { i ->
                        val tab = tabs[i]
                        val isSelected = tab.id == activeTabId
                        DepthCard(
                            onClick = { onTabSelected(tab) },
                            modifier = Modifier
                                .width(110.dp)
                                .height(80.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(2.dp, if (isSelected) Color(group.color) else Color.Transparent, RoundedCornerShape(16.dp))
                            ) {
                                coil.compose.AsyncImage(
                                    model = java.io.File(context.filesDir, "preview_${tab.id}.jpg"),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    alpha = if (isSelected) 1f else 0.8f
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                        ))
                                )
                                Text(
                                    text = tab.title.ifEmpty { "New Tab" },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    maxLines = 1,
                                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                                )
                                IconButton(
                                    onClick = { onTabClosed(tab) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(12.dp), tint = Color.White)
                                }
                            }
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        FrostedGlassSurface(
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
            blurRadius = 24.dp,
            modifier = Modifier
                .fillMaxWidth()
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
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isEditingUrl) {
                    Box {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        menuContent()
                    }
                } else {
                    IconButton(onClick = { onUrlEditStateChange(false) }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isEditingUrl) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f) else Color.Transparent)
                        .clickable {
                            if (!isEditingUrl) onUrlEditStateChange(true)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
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
                            Icon(Icons.Default.Lock, contentDescription = "Secure", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentUrl.isEmpty()) "Search or type URL" else currentUrl,
                                maxLines = 1,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (!isEditingUrl) {
                    DepthCard(
                        onClick = onTabCountClick,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(40.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabCount.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
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
    onNavigateToPrivacyStats: () -> Unit,
    onNavigateToUserscripts: () -> Unit,
    onNavigateToAmbientMode: () -> Unit,
    onAddToFavoritesClick: () -> Unit,
    onAddToGroupClick: () -> Unit,
    onNavigateForward: () -> Unit,
    onScanQrClick: () -> Unit,
    onPrintPdfClick: () -> Unit,
    onEnterPipClick: () -> Unit,
    onArPreviewClick: () -> Unit
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
            text = { Text("Add to Group") },
            onClick = {
                onAddToGroupClick()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("Scan QR Code") },
            onClick = {
                onScanQrClick()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("Picture-in-Picture") },
            onClick = {
                onEnterPipClick()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.PictureInPictureAlt, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("Save as PDF") },
            onClick = {
                onPrintPdfClick()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("AR Spatial Preview") },
            onClick = {
                onArPreviewClick()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.ViewInAr, contentDescription = null) }
        )
        HorizontalDivider()
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
            text = { Text("Privacy Stats") },
            onClick = {
                onNavigateToPrivacyStats()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("Userscripts") },
            onClick = {
                onNavigateToUserscripts()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Ambient Mode") },
            onClick = {
                onNavigateToAmbientMode()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.Bedtime, contentDescription = null) }
        )
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
