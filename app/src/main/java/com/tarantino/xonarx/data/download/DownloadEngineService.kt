package com.tarantino.xonarx.data.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.tarantino.xonarx.MainActivity
import com.tarantino.xonarx.domain.download.DownloadState
import com.tarantino.xonarx.domain.model.DownloadItem
import com.tarantino.xonarx.domain.model.DownloadStatus
import com.tarantino.xonarx.domain.repository.DownloadRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
class DownloadEngineService : Service() {

    @Inject
    lateinit var downloadRepository: DownloadRepository

    @Inject
    lateinit var downloadManagerImpl: XonarDownloadManagerImpl

    @Inject
    lateinit var okHttpClient: OkHttpClient

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeJobs = mutableMapOf<String, Job>()
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_CANCEL = "ACTION_CANCEL"
        const val ACTION_RETRY = "ACTION_RETRY"

        const val EXTRA_DOWNLOAD_ID = "EXTRA_DOWNLOAD_ID"

        private const val CHANNEL_ID = "xonar_downloads_channel"
        private const val NOTIFICATION_ID_BASE = 1000
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_STICKY
        
        val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID) ?: return START_STICKY
        val action = intent.action

        when (action) {
            ACTION_START, ACTION_RETRY, ACTION_RESUME -> {
                if (!activeJobs.containsKey(downloadId)) {
                    startForegroundIfNeeded(downloadId)
                    activeJobs[downloadId] = serviceScope.launch {
                        processDownload(downloadId)
                    }
                }
            }
            ACTION_PAUSE -> {
                activeJobs[downloadId]?.cancel()
                activeJobs.remove(downloadId)
                serviceScope.launch {
                    val item = downloadRepository.getDownloadById(downloadId)
                    if (item != null) {
                        updateItemAndNotification(item.copy(status = DownloadStatus.PAUSED))
                    }
                }
            }
            ACTION_CANCEL -> {
                activeJobs[downloadId]?.cancel()
                activeJobs.remove(downloadId)
                serviceScope.launch {
                    val item = downloadRepository.getDownloadById(downloadId)
                    if (item != null) {
                        updateItemAndNotification(item.copy(status = DownloadStatus.CANCELLED))
                        // Clean up file if cancelled
                        val file = File(item.destinationPath)
                        if (file.exists()) file.delete()
                    }
                }
            }
        }

        return START_STICKY
    }

    private fun startForegroundIfNeeded(initialId: String) {
        val notification = buildNotification(initialId, "Starting...", 0, 0, DownloadStatus.QUEUED, 0, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID_BASE + initialId.hashCode(), notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID_BASE + initialId.hashCode(), notification)
        }
    }

    private suspend fun processDownload(downloadId: String) {
        var item = downloadRepository.getDownloadById(downloadId) ?: return
        
        if (item.status == DownloadStatus.COMPLETED || item.status == DownloadStatus.CANCELLED) {
            return
        }

        item = item.copy(status = DownloadStatus.DOWNLOADING, startedAt = System.currentTimeMillis())
        updateItemAndNotification(item)

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        val speedCalculator = com.tarantino.xonarx.domain.usecase.SpeedEtaCalculator()
        val fileGenerator = com.tarantino.xonarx.domain.usecase.FileNameGenerator()

        // Create a unique file name if it doesn't exist, or use the existing if resuming
        var fileName = item.fileName
        
        // If it's a fresh start, ensure unique name
        if (item.downloadedBytes == 0L) {
            fileName = fileGenerator.generateUniqueFileName(downloadsDir, fileName)
        }
        var file = File(downloadsDir, fileName)
        
        // Ensure path uses actual file chosen
        item = item.copy(destinationPath = file.absolutePath, fileName = fileName)
        updateItemAndNotification(item)

        var downloadedBytes = file.length()
        if (item.totalBytes > 0 && downloadedBytes > item.totalBytes) {
            downloadedBytes = 0
            file.delete()
        }

        val requestBuilder = Request.Builder().url(item.url)
        if (downloadedBytes > 0) {
            requestBuilder.header("Range", "bytes=$downloadedBytes-")
        }

        try {
            val response: Response = okHttpClient.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: ${response.message}")
            }

            val body = response.body
            if (body == null) throw Exception("Empty response body")

            val contentLength = body.contentLength()
            val totalBytes = if (contentLength != -1L) downloadedBytes + contentLength else item.totalBytes
            item = item.copy(totalBytes = totalBytes)

            val inputStream: InputStream = body.byteStream()
            val outputStream = FileOutputStream(file, downloadedBytes > 0)

            var lastUpdateTime = System.currentTimeMillis()
            var bytesSinceLastUpdate = 0L

            inputStream.use { input ->
                outputStream.use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        // Check if cancelled
                        if (activeJobs[downloadId]?.isActive != true) break

                        output.write(buffer, 0, read)
                        downloadedBytes += read
                        bytesSinceLastUpdate += read

                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastUpdateTime >= 1000) {
                            val timeDeltaMs = currentTime - lastUpdateTime
                            val speed = speedCalculator.calculateSpeed(bytesSinceLastUpdate, timeDeltaMs)
                            val remainingBytes = totalBytes - downloadedBytes
                            val eta = speedCalculator.calculateEta(remainingBytes, speed)
                            val progress = if (totalBytes > 0) ((downloadedBytes * 100f) / totalBytes).toInt() else 0

                            item = item.copy(
                                downloadedBytes = downloadedBytes,
                                progress = progress,
                                speedBytesPerSecond = speed,
                                etaSeconds = eta
                            )
                            updateItemAndNotification(item)

                            lastUpdateTime = currentTime
                            bytesSinceLastUpdate = 0
                        }
                    }
                }
            }

            if (activeJobs[downloadId]?.isActive == true) {
                item = item.copy(
                    status = DownloadStatus.COMPLETED,
                    progress = 100,
                    downloadedBytes = item.totalBytes,
                    completedAt = System.currentTimeMillis()
                )
                updateItemAndNotification(item)
            }

        } catch (e: Exception) {
            if (e is CancellationException) {
                // Handled in actions
            } else {
                item = item.copy(
                    status = DownloadStatus.FAILED,
                    errorMessage = e.message
                )
                updateItemAndNotification(item)
            }
        } finally {
            activeJobs.remove(downloadId)
            stopForegroundIfNoActiveJobs()
        }
    }

    private suspend fun updateItemAndNotification(item: DownloadItem) {
        downloadRepository.updateDownload(item)
        downloadManagerImpl.updateActiveState(
            DownloadState(
                id = item.id,
                progress = item.progress,
                totalBytes = item.totalBytes,
                downloadedBytes = item.downloadedBytes,
                speedBytesPerSecond = item.speedBytesPerSecond,
                etaSeconds = item.etaSeconds,
                status = item.status
            )
        )
        val notification = buildNotification(
            item.id,
            item.fileName,
            item.progress,
            item.totalBytes,
            item.status,
            item.speedBytesPerSecond,
            item.etaSeconds,
            item.destinationPath,
            item.mimeType
        )
        notificationManager.notify(NOTIFICATION_ID_BASE + item.id.hashCode(), notification)

        if (item.status == DownloadStatus.COMPLETED || item.status == DownloadStatus.FAILED || item.status == DownloadStatus.CANCELLED) {
             downloadManagerImpl.removeActiveState(item.id)
        }
    }

    private fun stopForegroundIfNoActiveJobs() {
        if (activeJobs.isEmpty()) {
            stopForeground(false)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active and completed downloads"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        downloadId: String,
        title: String,
        progress: Int,
        totalBytes: Long,
        status: DownloadStatus,
        speed: Long,
        eta: Long,
        filePath: String? = null,
        mimeType: String? = null
    ): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOnlyAlertOnce(true)
            .setOngoing(status == DownloadStatus.DOWNLOADING)

        val openIntent = Intent(this, MainActivity::class.java).apply {
             flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingOpen = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(pendingOpen)

        when (status) {
            DownloadStatus.DOWNLOADING -> {
                val speedText = formatBytes(speed) + "/s"
                val progressText = if (totalBytes > 0) "${formatBytes(totalBytes * progress / 100)} / ${formatBytes(totalBytes)}" else "Downloading..."
                var contentText = "$progressText  •  $speedText"
                if (eta > 0) contentText += "  •  ${formatTime(eta)}"
                builder.setContentText(contentText)
                builder.setProgress(100, progress, totalBytes == 0L)

                val pauseIntent = PendingIntent.getService(this, downloadId.hashCode(), Intent(this, DownloadEngineService::class.java).apply {
                    action = ACTION_PAUSE
                    putExtra(EXTRA_DOWNLOAD_ID, downloadId)
                }, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                
                val cancelIntent = PendingIntent.getService(this, downloadId.hashCode() + 1, Intent(this, DownloadEngineService::class.java).apply {
                    action = ACTION_CANCEL
                    putExtra(EXTRA_DOWNLOAD_ID, downloadId)
                }, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

                builder.addAction(android.R.drawable.ic_media_pause, "Pause", pauseIntent)
                builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelIntent)
            }
            DownloadStatus.PAUSED -> {
                builder.setContentText("Paused")
                builder.setSmallIcon(android.R.drawable.stat_sys_warning)

                val resumeIntent = PendingIntent.getService(this, downloadId.hashCode() + 2, Intent(this, DownloadEngineService::class.java).apply {
                    action = ACTION_RESUME
                    putExtra(EXTRA_DOWNLOAD_ID, downloadId)
                }, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                
                val cancelIntent = PendingIntent.getService(this, downloadId.hashCode() + 1, Intent(this, DownloadEngineService::class.java).apply {
                    action = ACTION_CANCEL
                    putExtra(EXTRA_DOWNLOAD_ID, downloadId)
                }, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

                builder.addAction(android.R.drawable.ic_media_play, "Resume", resumeIntent)
                builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelIntent)
            }
            DownloadStatus.COMPLETED -> {
                builder.setContentText("Download complete")
                builder.setSmallIcon(android.R.drawable.stat_sys_download_done)
                builder.setProgress(0, 0, false)

                if (filePath != null && mimeType != null) {
                    val file = File(filePath)
                    if (file.exists()) {
                        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
                        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, mimeType)
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        val pendingView = PendingIntent.getActivity(this, downloadId.hashCode() + 3, viewIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                        builder.setContentIntent(pendingView)
                        builder.addAction(android.R.drawable.ic_menu_view, "Open", pendingView)
                    }
                }
            }
            DownloadStatus.FAILED -> {
                builder.setContentText("Download failed")
                builder.setSmallIcon(android.R.drawable.stat_notify_error)
                val retryIntent = PendingIntent.getService(this, downloadId.hashCode() + 4, Intent(this, DownloadEngineService::class.java).apply {
                    action = ACTION_RETRY
                    putExtra(EXTRA_DOWNLOAD_ID, downloadId)
                }, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                builder.addAction(android.R.drawable.ic_popup_sync, "Retry", retryIntent)
            }
            DownloadStatus.CANCELLED -> {
                builder.setContentText("Cancelled")
                builder.setSmallIcon(android.R.drawable.stat_notify_error)
            }
            DownloadStatus.QUEUED -> {
                builder.setContentText("Waiting...")
                builder.setProgress(0, 0, true)
            }
        }
        return builder.build()
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(bytes / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    private fun formatTime(seconds: Long): String {
        if (seconds < 60) return "${seconds}s"
        if (seconds < 3600) return "${seconds / 60}m ${seconds % 60}s"
        return "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
