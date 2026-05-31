package com.tarantino.xonarx.presentation.browser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.GestureDetector
import android.view.MotionEvent
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.tarantino.xonarx.domain.usecase.AdBlockerEngine

/**
 * A custom WebView tailored for Xonar identity isolation and browser functions.
 * 
 * Note: Horizontal swipe gestures (fling) have been removed from the WebView level 
 * to prevent conflicts with standard web page horizontal scrolling. Users should rely 
 * on system back gestures for navigation.
 */
class BrowserWebView(
    context: Context,
    private val adBlockerEngine: AdBlockerEngine,
    val identityId: String,
    private val onDownloadStarted: (String, String) -> Unit = { _, _ -> }
) : WebView(context) {

    var onPageUpdate: ((String, String?) -> Unit)? = null

    fun capturePreview(): Bitmap? {
        if (width <= 0 || height <= 0) return null
        return try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            draw(canvas)
            val scaledWidth = width / 3
            val scaledHeight = height / 3
            if (scaledWidth > 0 && scaledHeight > 0) {
                val scaled = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
                bitmap.recycle()
                scaled
            } else {
                bitmap
            }
        } catch (e: Exception) {
            null
        }
    }

    init {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }
        webViewClient = BrowserWebViewClient(adBlockerEngine) {
            onPageUpdate?.invoke(url ?: "", title)
        }
        webChromeClient = BrowserWebChromeClient()
        
        setDownloadListener { url, _, contentDisposition, mimetype, _ ->
            // In a real app we'd parse contentDisposition for the file name
            val fileName = Uri.parse(url).lastPathSegment ?: "downloaded_file"
            onDownloadStarted(url, fileName)
        }
    }
}

class BrowserWebViewClient(
    private val adBlockerEngine: AdBlockerEngine,
    private val onPageUpdateCallback: () -> Unit
) : WebViewClient() {
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val uri = request?.url
        if (uri != null && adBlockerEngine.shouldBlock(uri)) {
            return adBlockerEngine.createEmptyResource()
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageUpdateCallback()
    }
}

class BrowserWebChromeClient : WebChromeClient() {
    // Hooks for progress, icons, titles
}
