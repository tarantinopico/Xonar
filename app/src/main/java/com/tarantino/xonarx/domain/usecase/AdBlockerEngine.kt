package com.tarantino.xonarx.domain.usecase

import android.net.Uri
import android.webkit.WebResourceResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdBlockerEngine @Inject constructor() {
    private val _isBlockingEnabled = MutableStateFlow(true)
    val isBlockingEnabled: StateFlow<Boolean> = _isBlockingEnabled.asStateFlow()

    // Simplified blocking list for demonstration
    private val blockedDomains = setOf(
        "doubleclick.net",
        "googleadservices.com",
        "ad.doubleclick.net",
        "googlesyndication.com",
        "ads.twitter.com",
        "connect.facebook.net"
    )

    fun setBlockingEnabled(enabled: Boolean) {
        _isBlockingEnabled.value = enabled
    }

    fun shouldBlock(uri: Uri): Boolean {
        if (!_isBlockingEnabled.value) return false
        
        val host = uri.host ?: return false
        
        // Check if the host matches any of our blocked domains
        for (domain in blockedDomains) {
            if (host == domain || host.endsWith(".$domain")) {
                return true
            }
        }
        return false
    }

    fun createEmptyResource(): WebResourceResponse {
        return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
    }
}
