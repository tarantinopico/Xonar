package com.tarantino.xonarx.presentation.browser

import android.content.Context
import android.webkit.WebView
import java.util.concurrent.ConcurrentHashMap
import com.tarantino.xonarx.domain.usecase.AdBlockerEngine
import com.tarantino.xonarx.domain.usecase.DownloadManagerUseCase

data class TabSession(
    val tabId: String,
    val identityId: String,
    var webView: WebView? = null
)

class BrowserSessionManager(
    private val applicationContext: Context,
    private val adBlockerEngine: AdBlockerEngine,
    private val downloadManagerUseCase: DownloadManagerUseCase
) {
    private val sessions = ConcurrentHashMap<String, TabSession>()

    fun getOrCreateSession(tabId: String, identityId: String): TabSession {
        return sessions.getOrPut(tabId) {
            TabSession(tabId = tabId, identityId = identityId).apply {
                val wv = BrowserWebView(
                    applicationContext,
                    adBlockerEngine,
                    onDownloadStarted = { url, fileName -> 
                        downloadManagerUseCase.startDownload(url, fileName, identityId)
                    }
                )
                // In a real implementation, we isolate cookies/storage per identity here.
                webView = wv
            }
        }
    }

    fun removeSession(tabId: String) {
        val session = sessions.remove(tabId)
        session?.webView?.let { wv ->
            wv.stopLoading()
            wv.clearHistory()
            wv.removeAllViews()
            wv.destroy()
        }
    }
    
    fun clearIdentitySessions(identityId: String) {
        val toRemove = sessions.values.filter { it.identityId == identityId }
        toRemove.forEach { removeSession(it.tabId) }
    }
}
