package com.tarantino.xonarx.domain.repository

import com.tarantino.xonarx.domain.model.*
import kotlinx.coroutines.flow.Flow

interface IdentityRepository {
    fun getAllIdentities(): Flow<List<Identity>>
    fun observeIdentityById(id: String): Flow<Identity?>
    suspend fun getIdentityById(id: String): Identity?
    suspend fun createIdentity(identity: Identity)
    suspend fun updateIdentity(identity: Identity)
    suspend fun deleteIdentity(identity: Identity)
}

interface TabRepository {
    fun observeTabs(identityId: String): Flow<List<Tab>>
    suspend fun getTab(tabId: String, identityId: String): Tab?
    suspend fun getActiveTab(identityId: String): Tab?
    suspend fun addTab(tab: Tab)
    suspend fun updateTab(tab: Tab)
    suspend fun removeTab(tab: Tab)
    suspend fun activateTab(tabId: String, identityId: String)
}

interface HistoryRepository {
    fun observeHistory(identityId: String): Flow<List<HistoryItem>>
    suspend fun addHistoryItem(item: HistoryItem)
    suspend fun removeHistoryItem(item: HistoryItem)
    suspend fun clearHistory(identityId: String)
}

interface BookmarkRepository {
    fun observeBookmarks(identityId: String): Flow<List<Bookmark>>
    suspend fun addBookmark(bookmark: Bookmark)
    suspend fun removeBookmark(bookmark: Bookmark)
}

interface NoteRepository {
    fun observeNotes(identityId: String): Flow<List<Note>>
    suspend fun addNote(note: Note)
    suspend fun removeNote(note: Note)
}

interface DownloadRepository {
    fun observeDownloads(identityId: String): Flow<List<DownloadItem>>
    suspend fun addDownload(item: DownloadItem)
    suspend fun removeDownload(item: DownloadItem)
}
