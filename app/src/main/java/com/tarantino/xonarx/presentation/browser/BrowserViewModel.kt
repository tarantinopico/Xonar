package com.tarantino.xonarx.presentation.browser

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.tarantino.xonarx.domain.usecase.SuggestionsEngine
import com.tarantino.xonarx.domain.usecase.QrScannerUseCase
import com.tarantino.xonarx.domain.usecase.UserscriptEngine
import com.tarantino.xonarx.domain.model.Userscript
import com.tarantino.xonarx.domain.repository.SettingsRepository
import com.tarantino.xonarx.domain.repository.AppPreferences
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class BrowserViewModel @Inject constructor(
    val sessionManager: BrowserSessionManager,
    private val suggestionsEngine: SuggestionsEngine,
    private val qrScannerUseCase: QrScannerUseCase,
    private val userscriptEngine: UserscriptEngine,
    settingsRepository: SettingsRepository
) : ViewModel() {
    val preferences: StateFlow<AppPreferences> = settingsRepository.preferences.stateIn(
        viewModelScope, SharingStarted.Lazily, AppPreferences()
    )
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    private var currentIdentityId: String? = null
    private var activeScripts: List<Userscript> = emptyList()

    private var searchJob: Job? = null

    fun updateIdentity(identityId: String) {
        if (currentIdentityId != identityId) {
            currentIdentityId = identityId
            loadScriptsForIdentity(identityId)
        }
    }

    private fun loadScriptsForIdentity(identityId: String) {
        viewModelScope.launch {
            activeScripts = userscriptEngine.getScriptsForIdentity(identityId)
            sessionManager.getAllSessionsForIdentity(identityId).forEach { session ->
                session.webView?.scripts = activeScripts
            }
        }
    }

    fun capturePreviewForTab(tabId: String, identityId: String) {
        val session = sessionManager.getOrCreateSession(tabId, identityId)
        session.webView?.scripts = activeScripts
        val bmp = session.webView?.capturePreview()
        if (bmp != null) {
            sessionManager.updatePreview(tabId, bmp)
        }
    }

    fun updateSearchQuery(query: String, identityId: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _suggestions.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            val result = suggestionsEngine.getSuggestions(query, identityId)
            _suggestions.value = result
        }
    }

    fun startQrScan(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = qrScannerUseCase.scanQrCode()
            if (!result.isNullOrBlank()) {
                onResult(result)
            }
        }
    }
}
