package com.tarantino.xonarx.presentation.browser

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.tarantino.xonarx.domain.usecase.ReaderModeEngine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.tarantino.xonarx.domain.usecase.SuggestionsEngine

@HiltViewModel
class BrowserViewModel @Inject constructor(
    val sessionManager: BrowserSessionManager,
    val readerModeEngine: ReaderModeEngine,
    private val suggestionsEngine: SuggestionsEngine
) : ViewModel() {
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    private var searchJob: Job? = null

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
}
