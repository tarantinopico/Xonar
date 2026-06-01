package com.tarantino.xonarx.data.download

import android.content.Context
import android.content.Intent
import com.tarantino.xonarx.domain.download.DownloadState
import com.tarantino.xonarx.domain.download.XonarDownloadManager
import com.tarantino.xonarx.domain.model.DownloadItem
import com.tarantino.xonarx.domain.model.DownloadStatus
import com.tarantino.xonarx.domain.repository.DownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XonarDownloadManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadRepository: DownloadRepository
) : XonarDownloadManager {

    private val _activeDownloadsFlow = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    override val activeDownloadsFlow: Flow<Map<String, DownloadState>> = _activeDownloadsFlow.asStateFlow()

    fun updateActiveState(state: DownloadState) {
        _activeDownloadsFlow.update { it + (state.id to state) }
    }
    
    fun removeActiveState(id: String) {
        _activeDownloadsFlow.update { it - id }
    }

    override suspend fun enqueueDownload(
        url: String,
        fileName: String,
        mimeType: String,
        identityId: String
    ): String {
        val id = UUID.randomUUID().toString()
        val item = DownloadItem(
            id = id,
            identityId = identityId,
            url = url,
            fileName = fileName,
            mimeType = mimeType,
            destinationPath = "", // will be set by service
            progress = 0,
            totalBytes = 0L,
            downloadedBytes = 0L,
            speedBytesPerSecond = 0L,
            etaSeconds = -1L,
            status = DownloadStatus.QUEUED,
            scheduledAt = System.currentTimeMillis(),
            startedAt = null,
            completedAt = null,
            errorMessage = null
        )
        downloadRepository.addDownload(item)
        
        sendCommandToService(DownloadEngineService.ACTION_START, id)
        return id
    }

    override suspend fun pauseDownload(id: String) {
        sendCommandToService(DownloadEngineService.ACTION_PAUSE, id)
    }

    override suspend fun resumeDownload(id: String) {
        sendCommandToService(DownloadEngineService.ACTION_RESUME, id)
    }

    override suspend fun cancelDownload(id: String) {
        sendCommandToService(DownloadEngineService.ACTION_CANCEL, id)
    }

    override suspend fun retryDownload(id: String) {
        sendCommandToService(DownloadEngineService.ACTION_RETRY, id)
    }

    override suspend fun getDownloadState(id: String): DownloadState? {
        return _activeDownloadsFlow.value[id]
    }

    private fun sendCommandToService(action: String, downloadId: String) {
        val intent = Intent(context, DownloadEngineService::class.java).apply {
            this.action = action
            putExtra(DownloadEngineService.EXTRA_DOWNLOAD_ID, downloadId)
        }
        try {
            context.startForegroundService(intent)
        } catch (e: Exception) {
            context.startService(intent)
        }
    }
}
