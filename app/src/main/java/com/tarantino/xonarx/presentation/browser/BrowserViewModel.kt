package com.tarantino.xonarx.presentation.browser

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.tarantino.xonarx.domain.usecase.ReaderModeEngine

@HiltViewModel
class BrowserViewModel @Inject constructor(
    val sessionManager: BrowserSessionManager,
    val readerModeEngine: ReaderModeEngine
) : ViewModel() {

}
