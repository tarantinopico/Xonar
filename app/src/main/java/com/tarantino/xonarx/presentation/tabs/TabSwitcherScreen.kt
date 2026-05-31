package com.tarantino.xonarx.presentation.tabs

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tarantino.xonarx.domain.model.Tab
import com.tarantino.xonarx.domain.model.TabGroup
import com.tarantino.xonarx.presentation.main.MainViewModel
import com.tarantino.xonarx.presentation.browser.BrowserViewModel
import com.tarantino.xonarx.presentation.theme.futuristic.DepthCard
import com.tarantino.xonarx.presentation.theme.futuristic.FrostedGlassSurface
import com.tarantino.xonarx.presentation.theme.futuristic.AnimatedGradientBackdrop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSwitcherScreen(
    viewModel: MainViewModel = hiltViewModel(),
    browserViewModel: BrowserViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showRenameDialogForGroup by remember { mutableStateOf<TabGroup?>(null) }
    var showColorDialogForGroup by remember { mutableStateOf<TabGroup?>(null) }
    
    // Dialogs
    showRenameDialogForGroup?.let { group ->
        var newName by remember { mutableStateOf(group.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialogForGroup = null },
            title = { Text("Rename Group") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.renameAndColorTabGroup(group.id, newName.ifBlank { "Group" }, group.color)
                    showRenameDialogForGroup = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialogForGroup = null }) { Text("Cancel") }
            }
        )
    }

    showColorDialogForGroup?.let { group ->
        val colors = listOf(
            0xFF1976D2.toInt(), 0xFF388E3C.toInt(), 0xFFD32F2F.toInt(),
            0xFFFBC02D.toInt(), 0xFF7B1FA2.toInt(), 0xFFE64A19.toInt(),
            0xFF0097A7.toInt(), 0xFF689F38.toInt(), 0xFFF57C00.toInt(),
            0xFF5D4037.toInt(), 0xFFC2185B.toInt(), 0xFF455A64.toInt()
        )
        AlertDialog(
            onDismissRequest = { showColorDialogForGroup = null },
            title = { Text("Group Color") },
            text = {
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    colors.forEach { c ->
                        Box(modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(c))
                            .clickable {
                                viewModel.renameAndColorTabGroup(group.id, group.name, c)
                                showColorDialogForGroup = null
                            }
                            .border(if (c == group.color) 2.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorDialogForGroup = null }) { Text("Cancel") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AnimatedGradientBackdrop(
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
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
                        title = { Text("Tabs", fontWeight = FontWeight.SemiBold) },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        },
                        actions = {
                            var showMenu by remember { mutableStateOf(false) }
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More")
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                val ungroupedTabs = uiState.tabs.filter { it.groupId == null }
                                if (ungroupedTabs.size >= 2) {
                                    DropdownMenuItem(
                                        text = { Text("Group All Tabs") },
                                        onClick = {
                                            viewModel.createTabGroup("New Group", 0xFF1976D2.toInt(), ungroupedTabs.map { it.id })
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                            
                            IconButton(onClick = {
                                viewModel.openTab("about:blank")
                                onNavigateBack()
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "New Tab")
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            if (uiState.tabs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("No open tabs", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    val processedGroupIds = mutableSetOf<String>()
                    
                    for (tab in uiState.tabs) {
                        val groupId = tab.groupId
                        if (groupId != null) {
                            if (!processedGroupIds.contains(groupId)) {
                                processedGroupIds.add(groupId)
                                val group = uiState.tabGroups.find { it.id == groupId }
                                if (group != null) {
                                    val tabsInGroup = uiState.tabs.filter { it.groupId == groupId }
                                    
                                    if (group.isExpanded) {
                                        item(span = { GridItemSpan(2) }, key = "group_header_${group.id}") {
                                            TabGroupHeader(
                                                group = group,
                                                count = tabsInGroup.size,
                                                onToggle = { viewModel.toggleTabGroupExpanded(group.id) },
                                                onClose = { viewModel.closeTabGroup(group.id) },
                                                onRename = { showRenameDialogForGroup = group },
                                                onRecolor = { showColorDialogForGroup = group }
                                            )
                                        }
                                        
                                        items(tabsInGroup, key = { it.id }) { tabInGroup ->
                                            val session = browserViewModel.sessionManager.getOrCreateSession(tabInGroup.id, tabInGroup.identityId)
                                            Box(modifier = Modifier.padding(start = 12.dp)) {
                                                TabCard(
                                                    tab = tabInGroup,
                                                    isSelected = tabInGroup.id == uiState.activeTab?.id,
                                                    previewBitmap = session.previewBitmap,
                                                    borderColor = Color(group.color),
                                                    onClick = {
                                                        viewModel.selectTab(tabInGroup)
                                                        onNavigateBack()
                                                    },
                                                    onClose = { viewModel.closeTab(tabInGroup) },
                                                    onMoveOut = { viewModel.moveTabToGroup(tabInGroup.id, null) },
                                                    groupCount = uiState.tabGroups.size,
                                                    onMoveToGroup = { gid -> viewModel.moveTabToGroup(tabInGroup.id, gid) },
                                                    availableGroups = uiState.tabGroups.filter { it.id != group.id }
                                                )
                                            }
                                        }
                                    } else {
                                        item(key = "group_collapsed_${group.id}") {
                                            TabGroupCollapsedCard(
                                                group = group,
                                                count = tabsInGroup.size,
                                                onClick = { viewModel.toggleTabGroupExpanded(group.id) },
                                                onClose = { viewModel.closeTabGroup(group.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            item(key = tab.id) {
                                val session = browserViewModel.sessionManager.getOrCreateSession(tab.id, tab.identityId)
                                TabCard(
                                    tab = tab,
                                    isSelected = tab.id == uiState.activeTab?.id,
                                    previewBitmap = session.previewBitmap,
                                    borderColor = null,
                                    onClick = {
                                        viewModel.selectTab(tab)
                                        onNavigateBack()
                                    },
                                    onClose = { viewModel.closeTab(tab) },
                                    onMoveOut = null,
                                    groupCount = uiState.tabGroups.size,
                                    onMoveToGroup = { gid -> viewModel.moveTabToGroup(tab.id, gid) },
                                    availableGroups = uiState.tabGroups
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabGroupHeader(
    group: TabGroup,
    count: Int,
    onToggle: () -> Unit,
    onClose: () -> Unit,
    onRename: () -> Unit,
    onRecolor: () -> Unit
) {
    FrostedGlassSurface(
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(group.color).copy(alpha = 0.2f),
        borderColor = Color(group.color).copy(alpha = 0.4f),
        blurRadius = 16.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(group.color))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${group.name} ($count)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            var showMenu by remember { mutableStateOf(false) }
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "Group Options", tint = MaterialTheme.colorScheme.onSurface)
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(text = { Text("Rename") }, onClick = { showMenu = false; onRename() }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                DropdownMenuItem(text = { Text("Change Color") }, onClick = { showMenu = false; onRecolor() }, leadingIcon = { Icon(Icons.Default.Palette, null) })
            }
            
            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close Group", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Collapse", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun TabGroupCollapsedCard(
    group: TabGroup,
    count: Int,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    DepthCard(onClick = onClick) {
        FrostedGlassSurface(
            shape = RoundedCornerShape(24.dp),
            containerColor = Color(group.color).copy(alpha = 0.1f),
            borderColor = Color(group.color).copy(alpha = 0.3f),
            blurRadius = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.65f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(group.color).copy(alpha = 0.3f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(group.color)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close Group", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$count", style = MaterialTheme.typography.displayMedium, color = Color(group.color), fontWeight = FontWeight.Bold)
                        Text("Tabs", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun TabCard(
    tab: Tab,
    isSelected: Boolean,
    previewBitmap: Bitmap?,
    borderColor: Color?,
    onClick: () -> Unit,
    onClose: () -> Unit,
    onMoveOut: (() -> Unit)?,
    groupCount: Int,
    onMoveToGroup: (String) -> Unit,
    availableGroups: List<TabGroup>
) {
    DepthCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.65f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .let { if (borderColor != null) it.border(2.dp, borderColor, RoundedCornerShape(24.dp)) else if (isSelected) it.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)) else it }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f) else Color.Transparent)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Move", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                    }
                    
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        if (onMoveOut != null) {
                            DropdownMenuItem(text = { Text("Remove from Group") }, onClick = { showMenu = false; onMoveOut() })
                        }
                        if (availableGroups.isNotEmpty()) {
                            availableGroups.forEach {
                                DropdownMenuItem(text = { Text("Move to ${it.name}") }, onClick = { showMenu = false; onMoveToGroup(it.id) })
                            }
                        }
                    }

                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close Tab", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                ) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = "Page Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                            Text(tab.url, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
