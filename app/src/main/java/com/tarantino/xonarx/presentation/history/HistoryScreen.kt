package com.tarantino.xonarx.presentation.history

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarantino.xonarx.domain.model.HistoryItem
import com.tarantino.xonarx.domain.repository.HistoryRepository
import com.tarantino.xonarx.domain.usecase.IdentityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val identityManager: IdentityManager,
    private val historyRepository: HistoryRepository
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val historyItems: StateFlow<List<HistoryItem>> = identityManager.activeIdentity
        .flatMapLatest { identity ->
            if (identity == null) flowOf(emptyList()) else historyRepository.observeHistory(identity.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearHistory() {
        viewModelScope.launch {
            val identity = identityManager.activeIdentity.first()
            if (identity != null) {
                historyRepository.clearHistory(identity.id)
            }
        }
    }

    fun deleteItem(item: HistoryItem) {
        viewModelScope.launch {
            historyRepository.removeHistoryItem(item)
        }
    }
    fun deleteItems(items: Set<HistoryItem>) {
        viewModelScope.launch {
            items.forEach { historyRepository.removeHistoryItem(it) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val items by viewModel.historyItems.collectAsState()
    var selectedItems by remember { mutableStateOf(setOf<HistoryItem>()) }
    val isSelectionMode = selectedItems.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (isSelectionMode) Text("${selectedItems.size} selected")
                    else Text("History") 
                },
                navigationIcon = {
                    IconButton(onClick = if (isSelectionMode) { { selectedItems = emptySet() } } else onNavigateBack) {
                        if (isSelectionMode) Icon(Icons.Default.ArrowBack, contentDescription = "Cancel")
                        else Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = { 
                            viewModel.deleteItems(selectedItems)
                            selectedItems = emptySet()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete selected")
                        }
                    } else {
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Text("Clear Data")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No history yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(items) { item ->
                    val isSelected = selectedItems.contains(item)
                    ListItem(
                        headlineContent = { Text(item.title, maxLines = 1) },
                        supportingContent = { Text(item.url, maxLines = 1) },
                        trailingContent = {
                            if (isSelectionMode) {
                                androidx.compose.material3.Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null
                                )
                            } else {
                                IconButton(onClick = { viewModel.deleteItem(item) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        },
                        colors = if (isSelected) ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.3f)) else ListItemDefaults.colors(),
                        modifier = Modifier.combinedClickable(
                                onClick = { 
                                    if (isSelectionMode) {
                                        selectedItems = if (isSelected) selectedItems - item else selectedItems + item
                                    } else {
                                        /* Navigate to item */ 
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        selectedItems = selectedItems + item
                                    }
                                }
                            )
                        
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
