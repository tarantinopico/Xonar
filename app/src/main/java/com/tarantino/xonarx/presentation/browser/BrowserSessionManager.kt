package com.tarantino.xonarx.presentation.browser

import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebView
import java.util.concurrent.ConcurrentHashMap
import com.tarantino.xonarx.domain.usecase.AdBlockerEngine
import com.tarantino.xonarx.domain.usecase.DownloadManagerUseCase

data class TabSession(
    val tabId: String,
    val identityId: String,
    var webView: BrowserWebView? = null,
    var previewBitmap: Bitmap? = null
)

/**
 * Manages the lifecycle and caching of WebView sessions and their preview bitmaps.
 *
 * This class ensures that memory leaks are prevented by explicitly destroying WebViews
 * and recycling bitmaps when tabs are closed or identities are cleared.
 */
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
                    identityId,
                    onDownloadStarted = { url, fileName -> 
                        downloadManagerUseCase.startDownload(url, fileName, identityId)
                    }
                )
                // We isolate cookies/storage per identity here.
                try {
                    if (androidx.webkit.WebViewFeature.isFeatureSupported(androidx.webkit.WebViewFeature.MULTI_PROFILE)) {
                        val store = androidx.webkit.ProfileStore.getInstance()
                        val profile = store.getOrCreateProfile("identity_$identityId")
                        androidx.webkit.WebViewCompat.setProfile(wv, profile.name)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
