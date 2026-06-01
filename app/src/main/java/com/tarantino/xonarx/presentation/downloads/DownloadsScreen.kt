package com.tarantino.xonarx.presentation.downloads

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarantino.xonarx.domain.download.XonarDownloadManager
import com.tarantino.xonarx.domain.model.DownloadItem
import com.tarantino.xonarx.domain.model.DownloadStatus
import com.tarantino.xonarx.domain.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.DecimalFormat
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val downloadManager: XonarDownloadManager
) : ViewModel() {

    private val _filter = MutableStateFlow(DownloadFilter.ALL)
    val filter = _filter.asStateFlow()

    // We observe all downloads from Room, then mix in real-time speed/eta from DownloadManager
    val downloads: StateFlow<List<DownloadItem>> = combine(
        downloadRepository.observeAllDownloads(),
        downloadManager.activeDownloadsFlow,
        _filter
    ) { dbDownloads, activeStates, currentFilter ->
        val mergedList = dbDownloads.map { dbItem ->
            val activeState = activeStates[dbItem.id]
            if (activeState != null && 
                (activeState.status == DownloadStatus.DOWNLOADING || 
                 activeState.status == DownloadStatus.PAUSED)) {
                 
                dbItem.copy(
                    progress = activeState.progress,
                    totalBytes = activeState.totalBytes,
                    downloadedBytes = activeState.downloadedBytes,
                    speedBytesPerSecond = activeState.speedBytesPerSecond,
                    etaSeconds = activeState.etaSeconds,
                    status = activeState.status
                )
            } else {
                dbItem
            }
        }

        when (currentFilter) {
            DownloadFilter.ALL -> mergedList
            DownloadFilter.ACTIVE -> mergedList.filter { 
                it.status == DownloadStatus.DOWNLOADING || 
                it.status == DownloadStatus.QUEUED || 
                it.status == DownloadStatus.PAUSED 
            }
            DownloadFilter.COMPLETED -> mergedList.filter { it.status == DownloadStatus.COMPLETED }
            DownloadFilter.FAILED -> mergedList.filter { it.status == DownloadStatus.FAILED || it.status == DownloadStatus.CANCELLED }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: DownloadFilter) {
        _filter.value = filter
    }

    fun pauseDownload(id: String) {
        viewModelScope.launch {
            downloadManager.pauseDownload(id)
        }
    }

    fun resumeDownload(id: String) {
        viewModelScope.launch {
            downloadManager.resumeDownload(id)
        }
    }

    fun cancelDownload(id: String) {
        viewModelScope.launch {
            downloadManager.cancelDownload(id)
        }
    }

    fun retryDownload(id: String) {
        viewModelScope.launch {
            downloadManager.retryDownload(id)
        }
    }

    fun deleteDownload(item: DownloadItem) {
        viewModelScope.launch {
            if (item.status == DownloadStatus.DOWNLOADING || item.status == DownloadStatus.PAUSED) {
                downloadManager.cancelDownload(item.id)
            }
            downloadRepository.removeDownload(item)
            val file = File(item.destinationPath)
            if (file.exists()) file.delete()
        }
    }

    fun clearCompleted() {
        viewModelScope.launch {
            downloadRepository.clearCompletedDownloads()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            // Cancel active ones first
            downloads.value.filter { 
                it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PAUSED
            }.forEach {
                downloadManager.cancelDownload(it.id)
            }
            downloadRepository.clearAllDownloads()
        }
    }
}

enum class DownloadFilter { ALL, ACTIVE, COMPLETED, FAILED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val downloads by viewModel.downloads.collectAsState()
    val filter by viewModel.filter.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("downloads_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.testTag("downloads_menu_button")) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear completed") },
                            onClick = {
                                viewModel.clearCompleted()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear all") },
                            onClick = {
                                viewModel.clearAll()
                                showMenu = false
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = filter.ordinal,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                DownloadFilter.values().forEachIndexed { index, title ->
                    Tab(
                        selected = filter.ordinal == index,
                        onClick = { viewModel.setFilter(title) },
                        text = { Text(title.name.lowercase().capitalize()) }
                    )
                }
            }

            if (downloads.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No downloads found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(downloads, key = { it.id }) { item ->
                        DownloadItemCard(
                            item = item,
                            onPause = { viewModel.pauseDownload(item.id) },
                            onResume = { viewModel.resumeDownload(item.id) },
                            onCancel = { viewModel.cancelDownload(item.id) },
                            onRetry = { viewModel.retryDownload(item.id) },
                            onDelete = { viewModel.deleteDownload(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadItemCard(
    item: DownloadItem,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = item.status == DownloadStatus.COMPLETED) {
                if (item.status == DownloadStatus.COMPLETED && item.destinationPath.isNotEmpty()) {
                    val file = File(item.destinationPath)
                    if (file.exists()) {
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, item.mimeType)
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        try {
                            context.startActivity(viewIntent)
                        } catch (e: Exception) {
                            // Ignored or handle no app found
                        }
                    }
                }
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.fileName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getStatusText(item),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }

            if (item.status == DownloadStatus.DOWNLOADING || item.status == DownloadStatus.PAUSED) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = if (item.totalBytes > 0) item.downloadedBytes.toFloat() / item.totalBytes else 0f,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${formatBytes(item.downloadedBytes)} / ${formatBytes(item.totalBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row {
                        if (item.status == DownloadStatus.DOWNLOADING) {
                            IconButton(onClick = onPause) {
                                Icon(Icons.Default.Pause, contentDescription = "Pause", tint = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            IconButton(onClick = onResume) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            } else if (item.status == DownloadStatus.FAILED) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

private fun getStatusText(item: DownloadItem): String {
    return when (item.status) {
        DownloadStatus.COMPLETED -> "${formatBytes(item.totalBytes)} • Completed"
        DownloadStatus.QUEUED -> "Waiting..."
        DownloadStatus.PAUSED -> "Paused"
        DownloadStatus.FAILED -> "Failed: ${item.errorMessage ?: "Unknown error"}"
        DownloadStatus.CANCELLED -> "Cancelled"
        DownloadStatus.DOWNLOADING -> {
            val speed = "${formatBytes(item.speedBytesPerSecond)}/s"
            val eta = if (item.etaSeconds > 0) " • ${formatTime(item.etaSeconds)} left" else ""
            "$speed$eta"
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(bytes / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}

private fun formatTime(seconds: Long): String {
    if (seconds < 60) return "${seconds}s"
    if (seconds < 3600) return "${seconds / 60}m"
    return "${seconds / 3600}h ${(seconds % 3600) / 60}m"
}
