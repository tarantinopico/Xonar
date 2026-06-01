package com.tarantino.xonarx.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tarantino.xonarx.domain.model.DownloadItem
import com.tarantino.xonarx.domain.model.DownloadStatus

@Entity(
    tableName = "download_items",
    foreignKeys = [
        ForeignKey(
            entity = IdentityEntity::class,
            parentColumns = ["id"],
            childColumns = ["identityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["identityId"])]
)
data class DownloadItemEntity(
    @PrimaryKey val id: String,
    val identityId: String,
    val url: String,
    val fileName: String,
    val mimeType: String,
    val destinationPath: String,
    val progress: Int,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val speedBytesPerSecond: Long,
    val etaSeconds: Long,
    val status: String,
    val scheduledAt: Long,
    val startedAt: Long?,
    val completedAt: Long?,
    val errorMessage: String?
) {
    fun toDomainModel() = DownloadItem(
        id = id,
        identityId = identityId,
        url = url,
        fileName = fileName,
        mimeType = mimeType,
        destinationPath = destinationPath,
        progress = progress,
        totalBytes = totalBytes,
        downloadedBytes = downloadedBytes,
        speedBytesPerSecond = speedBytesPerSecond,
        etaSeconds = etaSeconds,
        status = DownloadStatus.valueOf(status),
        scheduledAt = scheduledAt,
        startedAt = startedAt,
        completedAt = completedAt,
        errorMessage = errorMessage
    )

    companion object {
        fun fromDomainModel(item: DownloadItem) = DownloadItemEntity(
            id = item.id,
            identityId = item.identityId,
            url = item.url,
            fileName = item.fileName,
            mimeType = item.mimeType,
            destinationPath = item.destinationPath,
            progress = item.progress,
            totalBytes = item.totalBytes,
            downloadedBytes = item.downloadedBytes,
            speedBytesPerSecond = item.speedBytesPerSecond,
            etaSeconds = item.etaSeconds,
            status = item.status.name,
            scheduledAt = item.scheduledAt,
            startedAt = item.startedAt,
            completedAt = item.completedAt,
            errorMessage = item.errorMessage
        )
    }
}
