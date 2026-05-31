package com.tarantino.xonarx.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarantino.xonarx.domain.model.Identity
import com.tarantino.xonarx.domain.model.Tab
import com.tarantino.xonarx.domain.model.Bookmark
import com.tarantino.xonarx.domain.model.HistoryItem
import com.tarantino.xonarx.domain.repository.TabRepository
import com.tarantino.xonarx.domain.repository.BookmarkRepository
import com.tarantino.xonarx.domain.repository.HistoryRepository
import com.tarantino.xonarx.domain.repository.SettingsRepository
import com.tarantino.xonarx.domain.usecase.IdentityManager
import com.tarantino.xonarx.domain.usecase.UrlHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

import com.tarantino.xonarx.presentation.browser.BrowserSessionManager

data class MainUiState(
    val activeIdentity: Identity? = null,
    val tabs: List<Tab> = emptyList(),
    val favorites: List<Bookmark> = emptyList(),
    val activeTab: Tab? = null,
    val isLoading: Boolean = false,
    val isReady: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val identityManager: IdentityManager,
    private val tabRepository: TabRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val historyRepository: HistoryRepository,
    private val urlHelper: UrlHelper,
    private val settingsRepository: SettingsRepository,
    private val sessionManager: BrowserSessionManager
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MainUiState> = identityManager.activeIdentity
        .flatMapLatest { identity ->
            if (identity == null) {
                flowOf(MainUiState())
            } else {
                combine(
                    tabRepository.observeTabs(identity.id),
                    bookmarkRepository.observeBookmarks(identity.id)
                ) { tabs, bookmarks ->
                    MainUiState(
                        activeIdentity = identity,
                        tabs = tabs,
                        favorites = bookmarks.filter { it.isFavorite },
                        activeTab = tabs.find { it.isActive },
                        isReady = true
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MainUiState(isLoading = true)
        )

    init {
        // Create initial identity if needed
        viewModelScope.launch {
            if (identityManager.allIdentities.first().isEmpty()) {
                identityManager.createIdentity("Personal", 0xFF6200EE.toInt())
            }
        }
    }

    fun navigate(input: String) {
        val identity = uiState.value.activeIdentity ?: return
        val activeTab = uiState.value.activeTab
        viewModelScope.launch {
            val prefs = settingsRepository.preferences.first()
            val finalUrl = if (urlHelper.isUrl(input)) {
                urlHelper.normalizeUrl(input)
            } else {
                urlHelper.createSearchUrl(input, prefs.searchEngineUrl)
            }
            if (activeTab != null && (activeTab.url.isEmpty() || activeTab.url == "about:blank")) {
                updateActiveTabUrl(finalUrl, "Loading...")
            } else {
                openTab(finalUrl)
            }
        }
    }

    fun addToHistory(url: String, title: String?) {
        val identity = uiState.value.activeIdentity ?: return
        if (url.isEmpty() || url == "about:blank" || url == "Loading...") return

        viewModelScope.launch {
            val item = HistoryItem(
                id = UUID.randomUUID().toString(),
                identityId = identity.id,
                url = url,
                title = title ?: url,
                visitCount = 1,
                lastVisitedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )
            historyRepository.addHistoryItem(item)
        }
    }
    fun updateActiveTabUrl(url: String, title: String? = null) {
        val activeTab = uiState.value.activeTab ?: return
        viewModelScope.launch {
            val updated = activeTab.copy(
                url = url,
                title = title ?: activeTab.title,
                updatedAt = System.currentTimeMillis()
            )
            tabRepository.updateTab(updated)
        }
    }
    
    fun switchNextIdentity() {
        viewModelScope.launch {
            val all = identityManager.allIdentities.first()
            if (all.size <= 1) return@launch
            val active = uiState.value.activeIdentity ?: return@launch
            val idx = all.indexOfFirst { it.id == active.id }
            if (idx != -1) {
                val next = all[(idx + 1) % all.size]
                identityManager.switchIdentity(next.id)
            }
        }
    }

    fun switchPreviousIdentity() {
        viewModelScope.launch {
            val all = identityManager.allIdentities.first()
            if (all.size <= 1) return@launch
            val active = uiState.value.activeIdentity ?: return@launch
            val idx = all.indexOfFirst { it.id == active.id }
            if (idx != -1) {
                val prev = all[(idx - 1 + all.size) % all.size]
                identityManager.switchIdentity(prev.id)
            }
        }
    }

    fun addToFavorites() {
        val activeTab = uiState.value.activeTab ?: return
        if (activeTab.url.isEmpty() || activeTab.url == "about:blank" || activeTab.url == "Loading...") return
        
        viewModelScope.launch {
            val bookmark = Bookmark(
                id = UUID.randomUUID().toString(),
                identityId = activeTab.identityId,
                url = activeTab.url,
                title = activeTab.title,
                folderId = null,
                faviconUrl = activeTab.faviconUrl,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                sortOrder = 0,
                isFavorite = true
            )
            bookmarkRepository.addBookmark(bookmark)
        }
    }
    
    fun openTab(url: String) {
        val identity = uiState.value.activeIdentity ?: return
        viewModelScope.launch {
            val newTab = Tab(
                id = UUID.randomUUID().toString(),
                identityId = identity.id,
                url = url,
                title = "Loading...",
                faviconUrl = null,
                isActive = true,
                isIncognito = false,
                isPinned = false,
                groupId = null,
                lastVisitedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            tabRepository.addTab(newTab)
            tabRepository.activateTab(newTab.id, identity.id)
        }
    }

    fun closeTab(tab: Tab) {
        viewModelScope.launch {
            tabRepository.removeTab(tab)
            sessionManager.removeSession(tab.id)
            val currentState = uiState.value
            val remain = currentState.tabs.filter { it.id != tab.id }
            if (tab.isActive && remain.isNotEmpty()) {
                tabRepository.activateTab(remain.last().id, tab.identityId)
            }
        }
    }

    fun selectTab(tab: Tab) {
        viewModelScope.launch {
            tabRepository.activateTab(tab.id, tab.identityId)
        }
    }
}
