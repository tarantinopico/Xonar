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
    onNavigateToFeeds: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferences by browserViewModel.preferences.collectAsState()
    val suggestions by browserViewModel.suggestions.collectAsState()
    var isEditingUrl by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showGroupDialog by remember { mutableStateOf(false) }
    var showBackHistorySheet by remember { mutableStateOf(false) }
    var showForwardHistorySheet by remember { mutableStateOf(false) }
    var showIdentitySelector by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    var suggestedClipboardUrl by remember { mutableStateOf<String?>(null) }
    var hasCheckedClipboard by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        if (!hasCheckedClipboard) {
            hasCheckedClipboard = true
            val clipUrl = com.tarantino.xonarx.presentation.util.ClipboardHelper.getClipboardUrl(context)
            if (clipUrl != null && clipUrl != uiState.activeTab?.url) {
                suggestedClipboardUrl = clipUrl
            }
        }
    }

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

    LaunchedEffect(preferences.defaultPageZoom, uiState.activeTab?.id) {
        uiState.activeTab?.id?.let { activeTabId ->
            val session = browserViewModel.sessionManager.getOrCreateSession(activeTabId, uiState.activeTab?.identityId ?: "")
            session.webView?.settings?.textZoom = preferences.defaultPageZoom
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
                                }
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (preferences.bottomControls && !isPipMode) {
                val activeWebView = uiState.activeTab?.id?.let { browserViewModel.sessionManager.getWebView(it) }
                val canGoBack = activeWebView?.canGoBack() == true
                val canGoForward = activeWebView?.canGoForward() == true
                
                BrowserBottomToolbar(
                    canGoBack = canGoBack,
                    canGoForward = canGoForward,
                    tabCount = uiState.tabs.size,
                    onBackClick = {
                        if (canGoBack) activeWebView?.goBack()
                    },
                    onBackLongClick = {
                        if (canGoBack) {
                            com.tarantino.xonarx.presentation.util.HapticFeedbackHelper.performLightHaptic(context, preferences.hapticFeedbackEnabled)
                            showBackHistorySheet = true
                        }
                    },
                    onForwardClick = {
                        if (canGoForward) activeWebView?.goForward()
                    },
                    onForwardLongClick = {
                        if (canGoForward) {
                            com.tarantino.xonarx.presentation.util.HapticFeedbackHelper.performLightHaptic(context, preferences.hapticFeedbackEnabled)
                            showForwardHistorySheet = true
                        }
                    },
                    onHomeClick = {
                        uiState.activeTab?.let {
                            viewModel.navigate("about:blank")
                        }
                    },
                    onSearchClick = {
                        com.tarantino.xonarx.presentation.util.HapticFeedbackHelper.performLightHaptic(context, preferences.hapticFeedbackEnabled)
                        isEditingUrl = true
                    },
                    onTabCountClick = {
                        uiState.activeTab?.let { activeTab ->
                            browserViewModel.capturePreviewForTab(activeTab.id, activeTab.identityId)
                        }
                        onNavigateToTabSwitcher()
                    },
                    onNewTabClick = {
                        uiState.activeIdentity?.let {
                            viewModel.openTab("about:blank", null)
                        }
                    },
                    onNewTabLongClick = {
                        com.tarantino.xonarx.presentation.util.HapticFeedbackHelper.performLightHaptic(context, preferences.hapticFeedbackEnabled)
                        showIdentitySelector = true
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
            
            // Clipboard Suggestion Banner
            AnimatedVisibility(
                visible = suggestedClipboardUrl != null,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                if (suggestedClipboardUrl != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        tonalElevation = 6.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    suggestedClipboardUrl?.let { url ->
                                        viewModel.navigate(url)
                                    }
                                    suggestedClipboardUrl = null
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Clipboard", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Link copied to clipboard", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                                Text(suggestedClipboardUrl!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { suggestedClipboardUrl = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            
            // Sheets
            val activeWebView = activeTab?.id?.let { browserViewModel.sessionManager.getWebView(it) }
            val backForwardList = activeWebView?.copyBackForwardList()
            if (showBackHistorySheet && backForwardList != null) {
                BrowserHistorySheet(
                    title = "Back History",
                    historyList = backForwardList,
                    currentIndex = backForwardList.currentIndex,
                    isForward = false,
                    onNavigateToIndex = { idx ->
                        val step = idx - backForwardList.currentIndex
                        activeWebView.goBackOrForward(step)
                        showBackHistorySheet = false
                    },
                    onDismissRequest = { showBackHistorySheet = false }
                )
            }
            if (showForwardHistorySheet && backForwardList != null) {
                BrowserHistorySheet(
                    title = "Forward History",
                    historyList = backForwardList,
                    currentIndex = backForwardList.currentIndex,
                    isForward = true,
                    onNavigateToIndex = { idx ->
                        val step = idx - backForwardList.currentIndex
                        activeWebView.goBackOrForward(step)
                        showForwardHistorySheet = false
                    },
                    onDismissRequest = { showForwardHistorySheet = false }
                )
            }
            
            if (showIdentitySelector) {
                ModalBottomSheet(onDismissRequest = { showIdentitySelector = false }) {
                    Column(Modifier.padding(bottom = 32.dp)) {
                        Text(
                            text = "New Tab in Identity",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        )
                        HorizontalDivider()
                        uiState.identities.forEach { identity ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.openTab("about:blank", null, identity.id)
                                        showIdentitySelector = false
                                    }
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(identity.color))
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = identity.displayName, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        FrostedGlassSurface(
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            borderColor = Color(group.color).copy(alpha = 0.3f),
            blurRadius = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Group Indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(group.color))
                )
                Spacer(modifier = Modifier.width(12.dp))

                // Tabs
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(tabs.size, key = { tabs[it].id }) { i ->
                        val tab = tabs[i]
                        val isSelected = tab.id == activeTabId
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            onClick = { onTabSelected(tab) },
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(group.color)) else null,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                            ) {
                                Text(
                                    text = tab.title.ifEmpty { "New Tab" },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    modifier = Modifier.widthIn(max = 80.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { onTabClosed(tab) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close, 
                                        contentDescription = "Close", 
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Add button
                IconButton(
                    onClick = onAddTab, 
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New tab", tint = Color(group.color), modifier = Modifier.size(20.dp))
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
    onAddToFavoritesClick: () -> Unit,
    onAddToGroupClick: () -> Unit,
    onNavigateForward: () -> Unit,
    onScanQrClick: () -> Unit,
    onPrintPdfClick: () -> Unit,
    onEnterPipClick: () -> Unit
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
