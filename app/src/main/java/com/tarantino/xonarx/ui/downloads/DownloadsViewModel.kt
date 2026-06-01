package com.tarantino.xonarx.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarantino.xonarx.domain.download.XonarDownloadManager
import com.tarantino.xonarx.domain.model.DownloadItem
import com.tarantino.xonarx.domain.model.DownloadStatus
import com.tarantino.xonarx.domain.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
