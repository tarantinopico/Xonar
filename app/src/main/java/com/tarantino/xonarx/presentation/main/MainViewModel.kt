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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

import com.tarantino.xonarx.presentation.browser.BrowserSessionManager

import com.tarantino.xonarx.domain.model.TabGroup
import com.tarantino.xonarx.domain.repository.TabGroupRepository

data class MainUiState(
    val activeIdentity: Identity? = null,
    val identities: List<Identity> = emptyList(),
    val tabs: List<Tab> = emptyList(),
    val tabGroups: List<TabGroup> = emptyList(),
    val favorites: List<Bookmark> = emptyList(),
    val frequentlyVisited: List<HistoryItem> = emptyList(),
    val activeTab: Tab? = null,
    val isLoading: Boolean = false,
    val isReady: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val identityManager: IdentityManager,
    private val tabRepository: TabRepository,
    private val tabGroupRepository: TabGroupRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val historyRepository: HistoryRepository,
    private val urlHelper: UrlHelper,
    private val settingsRepository: SettingsRepository,
    private val sessionManager: BrowserSessionManager
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MainUiState> = combine(
        identityManager.activeIdentity,
        identityManager.allIdentities
    ) { activeId, allIds -> activeId to allIds }
        .flatMapLatest { (identity, allIdentities) ->
            if (identity == null) {
                flowOf(MainUiState(identities = allIdentities))
            } else {
                combine(
                    tabRepository.observeTabs(identity.id),
                    tabGroupRepository.observeGroups(identity.id),
                    bookmarkRepository.observeBookmarks(identity.id),
                    historyRepository.observeHistory(identity.id)
                ) { tabs, groups, bookmarks, history ->
                    val frequent = history.sortedByDescending { it.visitCount }
                        .take(6)
                        
                    MainUiState(
                        activeIdentity = identity,
                        identities = allIdentities,
                        tabs = tabs,
                        tabGroups = groups,
                        favorites = bookmarks.filter { it.isFavorite },
                        frequentlyVisited = frequent,
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
    
    fun openTab(url: String, groupId: String? = null, overrideIdentityId: String? = null) {
        val identity = uiState.value.activeIdentity ?: return
        val targetIdentityId = overrideIdentityId ?: identity.id
        viewModelScope.launch {
            val newTab = Tab(
                id = UUID.randomUUID().toString(),
                identityId = targetIdentityId,
                url = url,
                title = "Loading...",
                faviconUrl = null,
                isActive = true,
                isIncognito = false,
                isPinned = false,
                groupId = groupId,
                lastVisitedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            tabRepository.addTab(newTab)
            tabRepository.activateTab(newTab.id, targetIdentityId)
            
            // Switch to the target identity if it's different so we can actually see the tab
            if (targetIdentityId != identity.id) {
                identityManager.switchIdentity(targetIdentityId)
            }
        }
    }

    fun closeTab(tab: Tab) {
        viewModelScope.launch {
            tabRepository.removeTab(tab)
            sessionManager.removeSession(tab.id)
            
            // Do NOT auto delete group
            // Empty groups are now persisted.
            
            val currentState = uiState.value
            val remain = currentState.tabs.filter { it.id != tab.id }
            if (tab.isActive && remain.isNotEmpty()) {
                tabRepository.activateTab(remain.last().id, tab.identityId)
            } else if (tab.isActive && remain.isEmpty()) {
                // If it was active and no tabs remain, we should probably open a new blank tab
                // or just leave it empty. Let's open a new tab so the browser doesn't break.
                openTab("about:blank")
            }
        }
    }

    fun toggleTabPinnedState(tab: Tab) {
        viewModelScope.launch {
            tabRepository.updateTab(tab.copy(isPinned = !tab.isPinned))
        }
    }

    fun selectTab(tab: Tab) {
        viewModelScope.launch {
            tabRepository.activateTab(tab.id, tab.identityId)
        }
    }

    // --- Tab Groups ---

    fun createTabGroup(name: String, color: Int, initialTabIds: List<String>) {
        val identity = uiState.value.activeIdentity ?: return
        viewModelScope.launch {
            val groupId = UUID.randomUUID().toString()
            var groupName = name
            
            if (groupName.isBlank() && initialTabIds.isNotEmpty()) {
                val firstTab = uiState.value.tabs.find { it.id == initialTabIds.first() }
                if (firstTab != null) {
                    val domain = urlHelper.getDomainName(firstTab.url)
                    groupName = if (domain.isNotBlank()) domain.capitalize() else "New Group"
                } else {
                    groupName = "New Group"
                }
            } else if (groupName.isBlank()) {
                groupName = "New Group"
            }

            val newGroup = TabGroup(
                id = groupId,
                identityId = identity.id,
                name = groupName,
                color = color,
                isExpanded = true,
                orderIndex = System.currentTimeMillis().toInt(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            tabGroupRepository.addGroup(newGroup)
            
            for (tabId in initialTabIds) {
                val tab = uiState.value.tabs.find { it.id == tabId }
                if (tab != null) {
                    tabRepository.updateTab(tab.copy(groupId = groupId))
                }
            }
        }
    }

    fun renameAndColorTabGroup(groupId: String, name: String, color: Int) {
        val group = uiState.value.tabGroups.find { it.id == groupId } ?: return
        viewModelScope.launch {
            tabGroupRepository.updateGroup(
                group.copy(
                    name = name,
                    color = color,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleTabGroupExpanded(groupId: String) {
        val group = uiState.value.tabGroups.find { it.id == groupId } ?: return
        viewModelScope.launch {
            tabGroupRepository.updateGroup(
                group.copy(
                    isExpanded = !group.isExpanded,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun moveTabToGroup(tabId: String, groupId: String?) {
        val tab = uiState.value.tabs.find { it.id == tabId } ?: return
        if (tab.groupId == groupId) return
        viewModelScope.launch {
            tabRepository.updateTab(tab.copy(groupId = groupId))
            // Empty groups are now persisted. Do NOT auto dissolve.
        }
    }

    fun closeTabGroup(groupId: String) {
        val group = uiState.value.tabGroups.find { it.id == groupId } ?: return
        viewModelScope.launch {
            val tabsInGroup = uiState.value.tabs.filter { it.groupId == groupId }
            for (tab in tabsInGroup) {
                sessionManager.removeSession(tab.id)
            }
            tabGroupRepository.deleteTabsInGroup(groupId)
            tabGroupRepository.removeGroup(group)

            val remainingTabs = uiState.value.tabs.filter { it.groupId != groupId }
            if (remainingTabs.none { it.isActive } && remainingTabs.isNotEmpty()) {
                tabRepository.activateTab(remainingTabs.last().id, group.identityId)
            } else if (remainingTabs.isEmpty()) {
                openTab("about:blank")
            }
        }
    }

    fun ungroupTabGroup(groupId: String) {
        val group = uiState.value.tabGroups.find { it.id == groupId } ?: return
        viewModelScope.launch {
            tabGroupRepository.ungroupTabs(groupId)
            tabGroupRepository.removeGroup(group)
        }
    }
}
