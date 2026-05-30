package com.tarantino.xonarx.domain.usecase

import com.tarantino.xonarx.domain.model.*
import com.tarantino.xonarx.domain.repository.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateIdentityUseCase @Inject constructor(private val identityManager: IdentityManager) {
    suspend operator fun invoke(displayName: String, color: Int, searchEngine: String): Identity =
        identityManager.createIdentity(displayName, color, searchEngine)
}

class DeleteIdentityUseCase @Inject constructor(private val identityManager: IdentityManager) {
    suspend operator fun invoke(identity: Identity) = identityManager.deleteIdentity(identity)
}

class UpdateIdentityUseCase @Inject constructor(private val repository: IdentityRepository) {
    suspend operator fun invoke(identity: Identity) = repository.updateIdentity(identity)
}

class SwitchIdentityUseCase @Inject constructor(private val identityManager: IdentityManager) {
    suspend operator fun invoke(identityId: String) = identityManager.switchIdentity(identityId)
}

class ObserveActiveIdentityUseCase @Inject constructor(private val identityManager: IdentityManager) {
    operator fun invoke(): Flow<Identity?> = identityManager.activeIdentity
}

class OpenTabUseCase @Inject constructor(private val repository: TabRepository) {
    suspend operator fun invoke(tab: Tab, identityId: String) {
        repository.addTab(tab)
        repository.activateTab(tab.id, identityId)
    }
}

class CloseTabUseCase @Inject constructor(private val repository: TabRepository) {
    suspend operator fun invoke(tab: Tab) = repository.removeTab(tab)
}

class RestoreClosedTabUseCase @Inject constructor(private val repository: TabRepository) {
    suspend operator fun invoke(tab: Tab) = repository.addTab(tab)
}

class SaveHistoryEntryUseCase @Inject constructor(private val repository: HistoryRepository) {
    suspend operator fun invoke(item: HistoryItem) = repository.addHistoryItem(item)
}

class AddBookmarkUseCase @Inject constructor(private val repository: BookmarkRepository) {
    suspend operator fun invoke(bookmark: Bookmark) = repository.addBookmark(bookmark)
}

class SaveNoteUseCase @Inject constructor(private val repository: NoteRepository) {
    suspend operator fun invoke(note: Note) = repository.addNote(note)
}

class UpdateSettingsUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {
    suspend fun updateThemeMode(mode: ThemeMode) = settingsRepository.updateThemeMode(mode)
    suspend fun updateMaterialYou(enabled: Boolean) = settingsRepository.updateMaterialYou(enabled)
}

class ObserveTabsUseCase @Inject constructor(private val repository: TabRepository) {
    operator fun invoke(identityId: String): Flow<List<Tab>> = repository.observeTabs(identityId)
}

class ObserveBookmarksUseCase @Inject constructor(private val repository: BookmarkRepository) {
    operator fun invoke(identityId: String): Flow<List<Bookmark>> = repository.observeBookmarks(identityId)
}

class ObserveHistoryUseCase @Inject constructor(private val repository: HistoryRepository) {
    operator fun invoke(identityId: String): Flow<List<HistoryItem>> = repository.observeHistory(identityId)
}
