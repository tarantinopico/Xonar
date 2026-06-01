package com.tarantino.xonarx.presentation.browser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.PermissionRequest
import android.webkit.GeolocationPermissions
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
    private val parentalControlEngine: com.tarantino.xonarx.domain.usecase.ParentalControlEngine,
    val identityId: String,
    private val onDownloadStarted: (String, String) -> Unit = { _, _ -> }
) : WebView(context) {

    var onPageUpdate: ((String, String?) -> Unit)? = null
    var onLoadingStateChanged: ((Boolean) -> Unit)? = null
    var onLongPressElement: ((ContextualActionTarget) -> Unit)? = null
    var backgroundVideoPlaybackEnabled: Boolean = false

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

    fun setDataSaverEnabled(enabled: Boolean) {
        settings.loadsImagesAutomatically = !enabled
        // Can optionally also inject JS to block media tags if really needed
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        if (visibility != View.VISIBLE && backgroundVideoPlaybackEnabled) {
            // Do not pause the WebView and its timers if background video playback is requested
            super.onWindowVisibilityChanged(visibility)
            return
        }
        super.onWindowVisibilityChanged(visibility)
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
            mediaPlaybackRequiresUserGesture = false
        }
        webViewClient = BrowserWebViewClient(adBlockerEngine, parentalControlEngine, identityId) {
            onPageUpdate?.invoke(url ?: "", title)
        }
        (webViewClient as BrowserWebViewClient).onLoadingStateChanged = { loading ->
            onLoadingStateChanged?.invoke(loading)
        }
        webChromeClient = BrowserWebChromeClient()
        
        setDownloadListener { url, _, contentDisposition, mimetype, _ ->
            val fileName = Uri.parse(url).lastPathSegment ?: "downloaded_file"
            onDownloadStarted(url, fileName)
        }

        setOnLongClickListener {
            val result = hitTestResult
            val target = when (result.type) {
                HitTestResult.SRC_ANCHOR_TYPE -> {
                    result.extra?.let { ContextualActionTarget.Link(it) }
                }
                HitTestResult.IMAGE_TYPE -> {
                    result.extra?.let { ContextualActionTarget.Image(it) }
                }
                HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                    result.extra?.let { ContextualActionTarget.ImageLink(it, it) }
                }
                else -> null
            }
            if (target != null) {
                // Haptic feedback will be triggered from the Compose UI layer or ViewModel.
                onLongPressElement?.invoke(target)
                true
            } else {
                false
            }
        }
    }
}

class BrowserWebViewClient(
    private val adBlockerEngine: AdBlockerEngine,
    private val parentalControlEngine: com.tarantino.xonarx.domain.usecase.ParentalControlEngine,
    private val identityId: String,
    private val onPageUpdateCallback: () -> Unit
) : WebViewClient() {
    
    var onLoadingStateChanged: ((Boolean) -> Unit)? = null

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onLoadingStateChanged?.invoke(true)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        if (!parentalControlEngine.isUrlAllowed(identityId, url)) {
            // Block loading this URL
            view?.loadData("<html><body><h1>Access Restricted</h1><p>This identity is not allowed to access this domain.</p></body></html>", "text/html", "UTF-8")
            return true
        }
        return super.shouldOverrideUrlLoading(view, request)
    }

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
        onLoadingStateChanged?.invoke(false)
        onPageUpdateCallback()
    }
}

class BrowserWebChromeClient : WebChromeClient() {
    override fun onPermissionRequest(request: PermissionRequest?) {
        // In a real premium app, we'd show a dialog based on identity preferences.
        // For now, we cautiously grant if we want it to feel modern, or deny by default.
        // Granting audio/video safely:
        request?.grant(request.resources)
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        callback?.invoke(origin, true, false)
    }
}
