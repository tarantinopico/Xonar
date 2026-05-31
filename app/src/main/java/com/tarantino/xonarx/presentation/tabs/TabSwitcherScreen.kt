package com.tarantino.xonarx.presentation.tabs

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tarantino.xonarx.domain.model.Tab
import com.tarantino.xonarx.presentation.main.MainViewModel
import com.tarantino.xonarx.presentation.browser.BrowserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSwitcherScreen(
    viewModel: MainViewModel = hiltViewModel(),
    browserViewModel: BrowserViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tabs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.openTab("https://google.com")
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "New Tab")
                    }
                }
            )
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                items(uiState.tabs) { tab ->
                    val session = browserViewModel.sessionManager.getOrCreateSession(tab.id, tab.identityId)
                    TabCard(
                        tab = tab,
                        isSelected = tab.id == uiState.activeTab?.id,
                        previewBitmap = session.previewBitmap,
                        onClick = {
                            viewModel.selectTab(tab)
                            onNavigateBack()
                        },
                        onClose = { viewModel.closeTab(tab) }
                    )
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
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Close Tab", modifier = Modifier.size(16.dp))
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
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
                    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) {
                        Text(tab.url, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(4.dp), color = Color.DarkGray)
                    }
                }
            }
        }
    }
}
