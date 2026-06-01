package com.tarantino.xonarx.domain.model

data class DownloadItem(
    val id: String,
    val identityId: String,
    val url: String,
    val fileName: String,
    val mimeType: String,
    val destinationPath: String,
    val progress: Int,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val speedBytesPerSecond: Long = 0L,
    val etaSeconds: Long = -1L,
    val status: DownloadStatus,
    val scheduledAt: Long,
    val startedAt: Long?,
    val completedAt: Long?,
    val errorMessage: String?
)

enum class DownloadStatus {
    QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED
}
