package com.tarantino.xonarx.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class DownloadItemStateTest {

    @Test
    fun `download state transitions correctly`() {
        val item = DownloadItem(
            id = UUID.randomUUID().toString(),
            identityId = "1",
            url = "https://example.com/file.zip",
            fileName = "file.zip",
            mimeType = "application/zip",
            destinationPath = "",
            progress = 0,
            status = DownloadStatus.QUEUED,
            scheduledAt = System.currentTimeMillis(),
            startedAt = null,
            completedAt = null,
            errorMessage = null
        )

        assertEquals(DownloadStatus.QUEUED, item.status)

        val downloadingItem = item.copy(status = DownloadStatus.DOWNLOADING, progress = 10)
        assertEquals(DownloadStatus.DOWNLOADING, downloadingItem.status)

        val pausedItem = downloadingItem.copy(status = DownloadStatus.PAUSED)
        assertEquals(DownloadStatus.PAUSED, pausedItem.status)

        val completedItem = pausedItem.copy(status = DownloadStatus.COMPLETED, progress = 100)
        assertEquals(DownloadStatus.COMPLETED, completedItem.status)
        assertEquals(100, completedItem.progress)

        val failedItem = downloadingItem.copy(status = DownloadStatus.FAILED, errorMessage = "Network Error")
        assertEquals(DownloadStatus.FAILED, failedItem.status)
        assertEquals("Network Error", failedItem.errorMessage)
    }
}
