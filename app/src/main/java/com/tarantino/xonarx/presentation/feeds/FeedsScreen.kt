package com.tarantino.xonarx.presentation.feeds

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarantino.xonarx.domain.model.Feed
import com.tarantino.xonarx.domain.repository.FeedRepository
import com.tarantino.xonarx.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedsViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val identityFlow = settingsRepository.preferences.map { it.lastActiveIdentityId ?: "" }
    
    // Simplification for the viewmodel
    private val _feeds = MutableStateFlow<List<Feed>>(emptyList())
    val feeds: StateFlow<List<Feed>> = _feeds

    init {
        viewModelScope.launch {
            identityFlow.collect { identityId ->
                if (identityId.isNotEmpty()) {
                    feedRepository.getFeedsForIdentity(identityId).collect {
                        _feeds.value = it
                    }
                }
            }
        }
    }

    fun removeFeed(feed: Feed) {
        viewModelScope.launch {
            feedRepository.deleteFeed(feed)
        }
    }

    fun refreshFeeds() {
        // In a full implementation, this triggers a sync service 
        // that downloads the feed URLs and parses them
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedsScreen(
    onNavigateBack: () -> Unit,
    viewModel: FeedsViewModel = hiltViewModel()
) {
    val feeds by viewModel.feeds.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RSS Feeds") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshFeeds() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        if (feeds.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No feeds added yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(feeds, key = { it.id }) { feed ->
                    FeedListItem(
                        feed = feed,
                        onDeleteClick = { viewModel.removeFeed(feed) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun FeedListItem(feed: Feed, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Could expand to show items */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = feed.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = feed.url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Feed", tint = MaterialTheme.colorScheme.error)
        }
    }
}
