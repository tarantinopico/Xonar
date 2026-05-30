package com.tarantino.xonarx.presentation.browser

import android.content.Context
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

class BrowserWebView(
    context: Context,
    private val adBlockerEngine: AdBlockerEngine,
    private val onDownloadStarted: (String, String) -> Unit = { _, _ -> }
) : WebView(context) {

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 200
        private val SWIPE_VELOCITY_THRESHOLD = 200

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 != null) {
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                if (Math.abs(diffX) > Math.abs(diffY) && 
                    Math.abs(diffX) > SWIPE_THRESHOLD && 
                    Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        if (canGoBack()) goBack()
                    } else {
                        if (canGoForward()) goForward()
                    }
                    return true
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY)
        }
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
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
        webViewClient = BrowserWebViewClient(adBlockerEngine)
        webChromeClient = BrowserWebChromeClient()
        
        setDownloadListener { url, _, contentDisposition, mimetype, _ ->
            // In a real app we'd parse contentDisposition for the file name
            val fileName = Uri.parse(url).lastPathSegment ?: "downloaded_file"
            onDownloadStarted(url, fileName)
        }
    }
}

class BrowserWebViewClient(
    private val adBlockerEngine: AdBlockerEngine
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
    // Hooks for future reader mode, downloads
}

class BrowserWebChromeClient : WebChromeClient() {
    // Hooks for progress, icons, titles
}
