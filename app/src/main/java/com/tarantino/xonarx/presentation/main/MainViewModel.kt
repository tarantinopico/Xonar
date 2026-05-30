package com.tarantino.xonarx.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarantino.xonarx.domain.model.Identity
import com.tarantino.xonarx.domain.model.Tab
import com.tarantino.xonarx.domain.repository.TabRepository
import com.tarantino.xonarx.domain.usecase.IdentityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class MainUiState(
    val activeIdentity: Identity? = null,
    val tabs: List<Tab> = emptyList(),
    val activeTab: Tab? = null,
    val isLoading: Boolean = false,
    val isReady: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val identityManager: IdentityManager,
    private val tabRepository: TabRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MainUiState> = identityManager.activeIdentity
        .flatMapLatest { identity ->
            if (identity == null) {
                flowOf(MainUiState())
            } else {
                tabRepository.observeTabs(identity.id).map { tabs ->
                    MainUiState(
                        activeIdentity = identity,
                        tabs = tabs,
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
