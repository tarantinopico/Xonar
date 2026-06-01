package com.tarantino.xonarx.domain.repository

import com.tarantino.xonarx.domain.model.DownloadItem
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun observeAllDownloads(): Flow<List<DownloadItem>>
    fun observeDownloads(identityId: String): Flow<List<DownloadItem>>
    suspend fun getDownloadById(id: String): DownloadItem?
    suspend fun addDownload(item: DownloadItem)
    suspend fun updateDownload(item: DownloadItem)
    suspend fun removeDownload(item: DownloadItem)
    suspend fun clearCompletedDownloads()
    suspend fun clearAllDownloads()
}
