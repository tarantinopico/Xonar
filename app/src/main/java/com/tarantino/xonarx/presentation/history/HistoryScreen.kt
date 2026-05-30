package com.tarantino.xonarx.presentation.history

import androidx.compose.foundation.clickable
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val items by viewModel.historyItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.clearHistory() }) {
                        Text("Clear Browsing Data")
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
                    ListItem(
                        headlineContent = { Text(item.title, maxLines = 1) },
                        supportingContent = { Text(item.url, maxLines = 1) },
                        trailingContent = {
                            IconButton(onClick = { viewModel.deleteItem(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        },
                        modifier = Modifier.clickable { /* Handle open */ }
                    )
                    Divider()
                }
            }
        }
    }
}
