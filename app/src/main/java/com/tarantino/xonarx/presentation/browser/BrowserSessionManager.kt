package com.tarantino.xonarx.presentation.browser

import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebView
import android.util.LruCache
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
 */
class BrowserSessionManager(
    private val applicationContext: Context,
    private val adBlockerEngine: AdBlockerEngine,
    private val downloadManagerUseCase: DownloadManagerUseCase
) {
    private val sessions = ConcurrentHashMap<String, TabSession>()
    
    // Memory safe LRU cache for previews
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    
    private val previewCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
        
        override fun entryRemoved(evicted: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
            if (evicted && oldValue != newValue) {
                // Ensure we don't recycle if it's still being used, though recycle is risky in Compose.
                // Leaving it to GC is generally safer in modern Android.
            }
        }
    }

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

    fun getWebView(tabId: String): BrowserWebView? = sessions[tabId]?.webView

    fun updatePreview(tabId: String, bitmap: Bitmap) {
        val session = sessions[tabId]
        if (session != null) {
            session.previewBitmap = bitmap
            previewCache.put(tabId, bitmap)
        }
    }

    fun getPreview(tabId: String): Bitmap? {
        return previewCache.get(tabId) ?: sessions[tabId]?.previewBitmap
    }

    fun removeSession(tabId: String) {
        val session = sessions.remove(tabId)
        session?.webView?.let { wv ->
            wv.stopLoading()
            wv.clearHistory()
            wv.removeAllViews()
            wv.destroy()
        }
        previewCache.remove(tabId)
    }
    
    fun clearIdentitySessions(identityId: String) {
        val toRemove = sessions.values.filter { it.identityId == identityId }
        toRemove.forEach { removeSession(it.tabId) }
    }
}
