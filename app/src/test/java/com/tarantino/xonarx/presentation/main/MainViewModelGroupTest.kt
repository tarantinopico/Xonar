package com.tarantino.xonarx.presentation.main

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tarantino.xonarx.domain.model.*
import com.tarantino.xonarx.domain.repository.*
import com.tarantino.xonarx.domain.usecase.AdBlockerEngine
import com.tarantino.xonarx.domain.usecase.DownloadManagerUseCase
import com.tarantino.xonarx.domain.usecase.IdentityManager
import com.tarantino.xonarx.domain.usecase.UrlHelper
import com.tarantino.xonarx.presentation.browser.BrowserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

class FakeTabGroupRepository : TabGroupRepository {
    private val groups = MutableStateFlow<List<TabGroup>>(emptyList())
    override fun observeGroups(identityId: String) = groups
    override suspend fun getGroupById(groupId: String, identityId: String) = groups.value.find { it.id == groupId }
    override suspend fun addGroup(group: TabGroup) { groups.update { it + group } }
    override suspend fun updateGroup(group: TabGroup) { groups.update { list -> list.map { if (it.id == group.id) group else it } } }
    override suspend fun removeGroup(group: TabGroup) { groups.update { list -> list.filter { it.id != group.id } } }
    override suspend fun ungroupTabs(groupId: String) {}
    override suspend fun deleteTabsInGroup(groupId: String) {}
}

class FakeTabRepository : TabRepository {
    private val tabs = MutableStateFlow<List<Tab>>(emptyList())
    override fun observeTabs(identityId: String) = tabs
    override suspend fun getTab(tabId: String, identityId: String) = tabs.value.find { it.id == tabId }
    override suspend fun getActiveTab(identityId: String) = tabs.value.find { it.isActive }
    override suspend fun addTab(tab: Tab) { tabs.update { it + tab } }
    override suspend fun updateTab(tab: Tab) { tabs.update { list -> list.map { if (it.id == tab.id) tab else it } } }
    override suspend fun removeTab(tab: Tab) { tabs.update { list -> list.filter { it.id != tab.id } } }
    override suspend fun activateTab(tabId: String, identityId: String) {
        tabs.update { list -> list.map { it.copy(isActive = it.id == tabId) } }
    }
}

class FakeSettingsRepository : SettingsRepository {
    override val preferences = flowOf(AppPreferences(lastActiveIdentityId = "1"))
    override suspend fun updateLastActiveIdentityId(id: String?) {}
    override suspend fun updateThemeMode(mode: ThemeMode) {}
    override suspend fun updateMaterialYou(enabled: Boolean) {}
    override suspend fun updateGesturesEnabled(enabled: Boolean) {}
    override suspend fun updateSearchEngine(url: String) {}
    override suspend fun updateBiometricsEnabled(enabled: Boolean) {}
    override suspend fun updateAutoClearOnExit(enabled: Boolean) {}
}

class FakeIdentityRepository : IdentityRepository {
    val activeIdentity = MutableStateFlow<Identity?>(Identity("1", "Test", 0, null, "", 0, 0, 0, 0, 0, false))
    override fun getAllIdentities(): Flow<List<Identity>> = flowOf(listOf(activeIdentity.value!!))
    override fun observeIdentityById(id: String): Flow<Identity?> = activeIdentity
    override suspend fun getIdentityById(id: String): Identity? = activeIdentity.value
    override suspend fun createIdentity(identity: Identity) {}
    override suspend fun updateIdentity(identity: Identity) {}
    override suspend fun deleteIdentity(identity: Identity) {}
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class MainViewModelGroupTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var tabGroupRepo: FakeTabGroupRepository
    private lateinit var tabRepo: FakeTabRepository
    private lateinit var viewModel: MainViewModel
    private lateinit var collectJob: kotlinx.coroutines.Job

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        tabGroupRepo = FakeTabGroupRepository()
        tabRepo = FakeTabRepository()
        val settingsRepo = FakeSettingsRepository()
        val identityRepo = FakeIdentityRepository()
        val identityManager = IdentityManager(identityRepo, settingsRepo)
        
        viewModel = MainViewModel(
            identityManager = identityManager,
            tabRepository = tabRepo,
            tabGroupRepository = tabGroupRepo,
            bookmarkRepository = object : BookmarkRepository {
                override fun observeBookmarks(id: String) = flowOf(emptyList<Bookmark>())
                override suspend fun addBookmark(b: Bookmark) {}
                override suspend fun removeBookmark(b: Bookmark) {}
                override suspend fun updateBookmark(b: Bookmark) {}
            },
            historyRepository = object : HistoryRepository {
                override fun observeHistory(id: String) = flowOf(emptyList<HistoryItem>())
                override suspend fun addHistoryItem(i: HistoryItem) {}
                override suspend fun removeHistoryItem(i: HistoryItem) {}
                override suspend fun clearHistory(id: String) {}
            },
            urlHelper = UrlHelper(),
            settingsRepository = settingsRepo,
            sessionManager = BrowserSessionManager(
                ApplicationProvider.getApplicationContext(),
                AdBlockerEngine(),
                DownloadManagerUseCase(ApplicationProvider.getApplicationContext(), object : DownloadRepository {
                    override fun observeDownloads(identityId: String) = flowOf(emptyList<DownloadItem>())
                    override suspend fun addDownload(item: DownloadItem) {}
                    override suspend fun removeDownload(item: DownloadItem) {}
                })
            )
        )
        
        collectJob = kotlinx.coroutines.CoroutineScope(testDispatcher).launch {
            viewModel.uiState.collect {}
        }
    }

    @After
    fun tearDown() {
        collectJob.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun createTabGroup_addsGroupAndMovesTabs() = runTest {
        val tab1 = Tab("t1", "1", "url", "tt", null, true, false, false, null, 0, 0, 0)
        tabRepo.addTab(tab1)
        
        kotlinx.coroutines.delay(10)
        viewModel.createTabGroup("My Group", 0xFF00FF, listOf("t1"))
        kotlinx.coroutines.delay(10)
        
        val state = viewModel.uiState.value
        assertEquals(1, state.tabGroups.size)
        assertEquals("My Group", state.tabGroups[0].name)
        assertEquals(state.tabGroups[0].id, state.tabs[0].groupId)
    }
    
    @Test
    fun renameAndColorTabGroup_updatesGroup() = runTest {
        kotlinx.coroutines.delay(10)
        viewModel.createTabGroup("My Group", 0xFF00FF, emptyList())
        kotlinx.coroutines.delay(10)
        
        val groupId = viewModel.uiState.value.tabGroups[0].id
        viewModel.renameAndColorTabGroup(groupId, "New Name", 0x00FF00)
        kotlinx.coroutines.delay(10)
        
        val state = viewModel.uiState.value
        assertEquals("New Name", state.tabGroups[0].name)
        assertEquals(0x00FF00, state.tabGroups[0].color)
    }
    
    @Test
    fun toggleTabGroupExpanded_switchesState() = runTest {
        kotlinx.coroutines.delay(10)
        viewModel.createTabGroup("My Group", 0xFF00FF, emptyList())
        kotlinx.coroutines.delay(10)
        
        val groupId = viewModel.uiState.value.tabGroups[0].id
        val prevExpanded = viewModel.uiState.value.tabGroups[0].isExpanded
        
        viewModel.toggleTabGroupExpanded(groupId)
        kotlinx.coroutines.delay(10)
        
        val state = viewModel.uiState.value
        assertEquals(!prevExpanded, state.tabGroups[0].isExpanded)
    }
    
    @Test
    fun moveTabToGroup_changesGroupId() = runTest {
        val tab1 = Tab("t1", "1", "url", "tt", null, true, false, false, null, 0, 0, 0)
        tabRepo.addTab(tab1)
        kotlinx.coroutines.delay(10)
        
        viewModel.createTabGroup("G1", 0xFF00FF, emptyList())
        kotlinx.coroutines.delay(10)
        
        val groupId = viewModel.uiState.value.tabGroups[0].id
        
        viewModel.moveTabToGroup("t1", groupId)
        kotlinx.coroutines.delay(10)
        
        assertEquals(groupId, viewModel.uiState.value.tabs[0].groupId)
    }
}
