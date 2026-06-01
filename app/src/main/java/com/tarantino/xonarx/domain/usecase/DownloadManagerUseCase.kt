package com.tarantino.xonarx.domain.usecase

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.tarantino.xonarx.domain.model.DownloadItem
import com.tarantino.xonarx.domain.model.DownloadStatus
import com.tarantino.xonarx.domain.repository.DownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManagerUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: DownloadRepository
) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startDownload(url: String, fileName: String, mimeType: String, identityId: String?) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Downloading file...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadId = downloadManager.enqueue(request)

        val item = DownloadItem(
            id = downloadId.toString(),
            sourceUrl = url,
            fileName = fileName,
            mimeType = mimeType,
            destinationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/" + fileName,
            totalBytes = 0L,
            downloadedBytes = 0L,
            speedBytesPerSecond = 0L,
            etaSeconds = -1L,
            status = DownloadStatus.PENDING,
            scheduledAt = System.currentTimeMillis(),
            startedAt = null,
            completedAt = null,
            errorMessage = null,
            identityId = identityId
        )
        
        scope.launch {
            repository.addDownload(item)
        }
    }
    
    fun observeAllDownloads() = repository.observeAllDownloads()

    // Stub for pausing/resuming as DownloadManager handles it mostly natively, 
    // but we can remove/cancel.
    fun cancelDownload(id: String) {
        downloadManager.remove(id.toLong())
        scope.launch {
            repository.getDownloadById(id)?.let {
                repository.updateDownload(it.copy(status = DownloadStatus.CANCELLED))
            }
        }
    }
}
