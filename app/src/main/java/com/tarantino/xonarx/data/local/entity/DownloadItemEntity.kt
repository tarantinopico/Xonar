package com.tarantino.xonarx.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tarantino.xonarx.domain.model.DownloadItem
import com.tarantino.xonarx.domain.model.DownloadStatus

@Entity(tableName = "download_items")
data class DownloadItemEntity(
    @PrimaryKey val id: String,
    val sourceUrl: String,
    val fileName: String,
    val mimeType: String,
    val destinationPath: String,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val speedBytesPerSecond: Long,
    val etaSeconds: Long,
    val status: String,
    val scheduledAt: Long,
    val startedAt: Long?,
    val completedAt: Long?,
    val errorMessage: String?,
    val identityId: String?
) {
    fun toDomainModel(): DownloadItem {
        return DownloadItem(
            id = id,
            sourceUrl = sourceUrl,
            fileName = fileName,
            mimeType = mimeType,
            destinationPath = destinationPath,
            totalBytes = totalBytes,
            downloadedBytes = downloadedBytes,
            speedBytesPerSecond = speedBytesPerSecond,
            etaSeconds = etaSeconds,
            status = DownloadStatus.valueOf(status),
            scheduledAt = scheduledAt,
            startedAt = startedAt,
            completedAt = completedAt,
            errorMessage = errorMessage,
            identityId = identityId
        )
    }

    companion object {
        fun fromDomainModel(item: DownloadItem): DownloadItemEntity {
            return DownloadItemEntity(
                id = item.id,
                sourceUrl = item.sourceUrl,
                fileName = item.fileName,
                mimeType = item.mimeType,
                destinationPath = item.destinationPath,
                totalBytes = item.totalBytes,
                downloadedBytes = item.downloadedBytes,
                speedBytesPerSecond = item.speedBytesPerSecond,
                etaSeconds = item.etaSeconds,
                status = item.status.name,
                scheduledAt = item.scheduledAt,
                startedAt = item.startedAt,
                completedAt = item.completedAt,
                errorMessage = item.errorMessage,
                identityId = item.identityId
            )
        }
    }
}
