package com.tarantino.xonarx.domain.download

import com.tarantino.xonarx.domain.model.DownloadItem
import kotlinx.coroutines.flow.Flow

interface XonarDownloadManager {
    val activeDownloadsFlow: Flow<Map<String, DownloadState>>
    
    suspend fun enqueueDownload(
        url: String,
        fileName: String,
        mimeType: String,
        identityId: String
    ): String
    
    suspend fun pauseDownload(id: String)
    suspend fun resumeDownload(id: String)
    suspend fun cancelDownload(id: String)
    suspend fun retryDownload(id: String)
    
    suspend fun getDownloadState(id: String): DownloadState?
}

data class DownloadState(
    val id: String,
    val progress: Int,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val speedBytesPerSecond: Long,
    val etaSeconds: Long,
    val status: com.tarantino.xonarx.domain.model.DownloadStatus
)
