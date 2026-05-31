package com.tarantino.xonarx.presentation.browser

import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebView
import android.util.LruCache
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.tarantino.xonarx.domain.usecase.AdBlockerEngine
import com.tarantino.xonarx.domain.usecase.DownloadManagerUseCase
import com.tarantino.xonarx.domain.usecase.ParentalControlEngine
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

import kotlinx.coroutines.flow.MutableStateFlow

data class TabSession(
    val tabId: String,
    val identityId: String,
    var webView: BrowserWebView? = null,
    var previewBitmap: Bitmap? = null,
    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
)

/**
 * Manages the lifecycle and caching of WebView sessions and their preview bitmaps.
 */
class BrowserSessionManager(
    private val applicationContext: Context,
    private val adBlockerEngine: AdBlockerEngine,
    private val downloadManagerUseCase: DownloadManagerUseCase,
    private val parentalControlEngine: ParentalControlEngine
) {
    private val sessions = ConcurrentHashMap<String, TabSession>()
    private val scope = CoroutineScope(Dispatchers.Main)
    
    // Memory safe LRU cache for previews, limited to 15 items.
    private val previewCache = LruCache<String, Bitmap>(15)
    
    // Bounded LRU cache for favicons
    private val faviconCache = LruCache<String, Bitmap>(50)

    // WebView Pool per identity
    private val webViewPools = ConcurrentHashMap<String, ConcurrentLinkedQueue<BrowserWebView>>()
    
    private fun getPoolForIdentity(identityId: String): ConcurrentLinkedQueue<BrowserWebView> {
        return webViewPools.getOrPut(identityId) { ConcurrentLinkedQueue() }
    }

    fun downloadUrl(url: String, fileName: String, identityId: String) {
        downloadManagerUseCase.startDownload(url, fileName, identityId)
    }

    private fun createWebView(identityId: String): BrowserWebView {
        val wv = BrowserWebView(
            applicationContext,
            adBlockerEngine,
            parentalControlEngine,
            identityId,
            onDownloadStarted = { url, fileName -> 
                downloadManagerUseCase.startDownload(url, fileName, identityId)
            }
        )
        try {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) {
                val store = androidx.webkit.ProfileStore.getInstance()
                val profile = store.getOrCreateProfile("identity_$identityId")
                WebViewCompat.setProfile(wv, profile.name)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return wv
    }

    fun getOrCreateSession(tabId: String, identityId: String): TabSession {
        return sessions.getOrPut(tabId) {
            TabSession(tabId = tabId, identityId = identityId).apply {
                // Try to acquire from pool first
                val pool = getPoolForIdentity(identityId)
                val wv = pool.poll() ?: createWebView(identityId)
                
                wv.onLoadingStateChanged = { loading ->
                    this.isLoading.value = loading
                }
                
                // Clear state if recycled
                wv.onResume()
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
    
    fun putFavicon(url: String, bitmap: Bitmap) {
        val domain = android.net.Uri.parse(url).host ?: url
        faviconCache.put(domain, bitmap)
    }

    fun getFavicon(url: String): Bitmap? {
        val domain = android.net.Uri.parse(url).host ?: url
        return faviconCache.get(domain)
    }

    fun removeSession(tabId: String) {
        val session = sessions.remove(tabId)
        session?.webView?.let { wv ->
            // Isolate and clean
            wv.stopLoading()
            wv.clearHistory()
            wv.loadUrl("about:blank")
            wv.onPause()
            
            // Delay 5 seconds before returning to pool
            scope.launch {
                delay(5000)
                val pool = getPoolForIdentity(session.identityId)
                // Limit pool size to 3 per identity
                if (pool.size < 3) {
                    pool.offer(wv)
                } else {
                    wv.removeAllViews()
                    wv.destroy()
                }
            }
        }
        previewCache.remove(tabId)
    }
    
    fun getAllSessionsForIdentity(identityId: String): List<TabSession> {
        return sessions.values.filter { it.identityId == identityId }
    }
    
    fun clearIdentitySessions(identityId: String) {
        val toRemove = sessions.values.filter { it.identityId == identityId }
        toRemove.forEach { removeSession(it.tabId) }
        
        // Also clear the pool for this identity
        val pool = webViewPools.remove(identityId)
        pool?.forEach { wv ->
            wv.removeAllViews()
            wv.destroy()
        }
    }
    
    fun prewarmWebView(identityId: String) {
        val pool = getPoolForIdentity(identityId)
        if (pool.size < 2) {
            scope.launch {
                pool.offer(createWebView(identityId))
            }
        }
    }
}
