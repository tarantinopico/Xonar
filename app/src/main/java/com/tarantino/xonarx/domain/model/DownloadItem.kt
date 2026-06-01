package com.tarantino.xonarx.domain.model

enum class DownloadStatus {
    PENDING, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED
}

data class DownloadItem(
    val id: String,
    val sourceUrl: String,
    val fileName: String,
    val mimeType: String,
    val destinationPath: String,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val speedBytesPerSecond: Long,
    val etaSeconds: Long,
    val status: DownloadStatus,
    val scheduledAt: Long,
    val startedAt: Long?,
    val completedAt: Long?,
    val errorMessage: String?,
    val identityId: String?
)
