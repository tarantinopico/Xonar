package com.tarantino.xonarx.data.repository

import com.tarantino.xonarx.data.local.dao.*
import com.tarantino.xonarx.data.local.entity.*
import com.tarantino.xonarx.domain.model.*
import com.tarantino.xonarx.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IdentityRepositoryImpl @Inject constructor(
    private val dao: IdentityDao
) : IdentityRepository {
    override fun getAllIdentities() = dao.getAllIdentities().map { list -> list.map { it.toDomainModel() } }
    override fun observeIdentityById(id: String) = dao.observeIdentityById(id).map { it?.toDomainModel() }
    override suspend fun getIdentityById(id: String) = dao.getIdentityById(id)?.toDomainModel()
    override suspend fun createIdentity(identity: Identity) = dao.insertIdentity(IdentityEntity.fromDomainModel(identity))
    override suspend fun updateIdentity(identity: Identity) = dao.updateIdentity(IdentityEntity.fromDomainModel(identity))
    override suspend fun deleteIdentity(identity: Identity) = dao.deleteIdentity(IdentityEntity.fromDomainModel(identity))
}

class TabRepositoryImpl @Inject constructor(
    private val dao: TabDao
) : TabRepository {
    override fun observeTabs(identityId: String) = dao.observeTabsByIdentity(identityId).map { list -> list.map { it.toDomainModel() } }
    override suspend fun getTab(tabId: String, identityId: String) = dao.getTab(tabId, identityId)?.toDomainModel()
    override suspend fun getActiveTab(identityId: String) = dao.getActiveTab(identityId)?.toDomainModel()
    override suspend fun addTab(tab: Tab) = dao.insertTab(TabEntity.fromDomainModel(tab))
    override suspend fun updateTab(tab: Tab) = dao.updateTab(TabEntity.fromDomainModel(tab))
    override suspend fun removeTab(tab: Tab) = dao.deleteTab(TabEntity.fromDomainModel(tab))
    override suspend fun activateTab(tabId: String, identityId: String) {
        dao.deactivateAllTabs(identityId)
        dao.activateTab(tabId, identityId)
    }
}

class HistoryRepositoryImpl @Inject constructor(
    private val dao: HistoryItemDao
) : HistoryRepository {
    override fun observeHistory(identityId: String) = dao.observeHistoryByIdentity(identityId).map { list -> list.map { it.toDomainModel() } }
    override suspend fun addHistoryItem(item: HistoryItem) = dao.insertHistoryItem(HistoryItemEntity.fromDomainModel(item))
    override suspend fun removeHistoryItem(item: HistoryItem) = dao.deleteHistoryItem(HistoryItemEntity.fromDomainModel(item))
    override suspend fun clearHistory(identityId: String) = dao.clearHistory(identityId)
}

class BookmarkRepositoryImpl @Inject constructor(
    private val dao: BookmarkDao
) : BookmarkRepository {
    override fun observeBookmarks(identityId: String) = dao.observeBookmarksByIdentity(identityId).map { list -> list.map { it.toDomainModel() } }
    override suspend fun addBookmark(bookmark: Bookmark) = dao.insertBookmark(BookmarkEntity.fromDomainModel(bookmark))
    override suspend fun removeBookmark(bookmark: Bookmark) = dao.deleteBookmark(BookmarkEntity.fromDomainModel(bookmark))
    override suspend fun updateBookmark(bookmark: Bookmark) = dao.updateBookmark(BookmarkEntity.fromDomainModel(bookmark))
}

class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao
) : NoteRepository {
    override fun observeNotes(identityId: String) = dao.observeNotesByIdentity(identityId).map { list -> list.map { it.toDomainModel() } }
    override suspend fun addNote(note: Note) = dao.insertNote(NoteEntity.fromDomainModel(note))
    override suspend fun removeNote(note: Note) = dao.deleteNote(NoteEntity.fromDomainModel(note))
}

class DownloadRepositoryImpl @Inject constructor(
    private val dao: DownloadItemDao
) : DownloadRepository {
    override fun observeDownloads(identityId: String) = dao.observeDownloadsByIdentity(identityId).map { list -> list.map { it.toDomainModel() } }
    override suspend fun addDownload(item: DownloadItem) = dao.insertDownload(DownloadItemEntity.fromDomainModel(item))
    override suspend fun removeDownload(item: DownloadItem) = dao.deleteDownload(DownloadItemEntity.fromDomainModel(item))
}
