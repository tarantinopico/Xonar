package com.tarantino.xonarx.data.repository

import com.tarantino.xonarx.data.local.dao.DownloadItemDao
import com.tarantino.xonarx.data.local.entity.DownloadItemEntity
import com.tarantino.xonarx.domain.model.DownloadItem
import com.tarantino.xonarx.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DownloadRepositoryImpl @Inject constructor(
    private val dao: DownloadItemDao
) : DownloadRepository {
    override fun observeAllDownloads(): Flow<List<DownloadItem>> = 
        dao.observeAllDownloads().map { list -> list.map { it.toDomainModel() } }
        
    override fun observeDownloads(identityId: String): Flow<List<DownloadItem>> = 
        dao.observeDownloadsByIdentity(identityId).map { list -> list.map { it.toDomainModel() } }
        
    override suspend fun getDownloadById(id: String): DownloadItem? = 
        dao.getDownloadById(id)?.toDomainModel()
        
    override suspend fun addDownload(item: DownloadItem) = 
        dao.insertDownload(DownloadItemEntity.fromDomainModel(item))
        
    override suspend fun updateDownload(item: DownloadItem) = 
        dao.updateDownload(DownloadItemEntity.fromDomainModel(item))
        
    override suspend fun removeDownload(item: DownloadItem) = 
        dao.deleteDownload(DownloadItemEntity.fromDomainModel(item))
        
    override suspend fun clearCompletedDownloads() = dao.clearCompletedDownloads()
    
    override suspend fun clearAllDownloads() = dao.clearAllDownloads()
}
