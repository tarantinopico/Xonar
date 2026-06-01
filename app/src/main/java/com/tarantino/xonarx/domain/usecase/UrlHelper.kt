package com.tarantino.xonarx.domain.usecase

import android.net.Uri
import android.util.Patterns
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UrlHelper @Inject constructor() {
    
    fun isUrl(input: String): Boolean {
        val lower = input.lowercase().trim()
        if (lower.contains(" ") && !lower.contains("://")) return false
        
        // Match standard web protocols or common TLDs
        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file://") || lower.startsWith("content://") || lower.startsWith("chrome://")) {
            return true
        }

        if (lower.startsWith("localhost:") || lower == "localhost") return true

        // IP address match
        if (lower.matches(Regex("^\\d{1,3}(\\.\\d{1,3}){3}(:\\d{1,5})?(/.*)?$"))) return true
        
        // Common TLD check roughly
        val matcher = Patterns.WEB_URL.matcher(input)
        return matcher.matches()
    }

    fun normalizeUrl(input: String): String {
        val trimmed = input.trim()
        if (isUrl(trimmed)) {
            if (!trimmed.contains("://") && !trimmed.startsWith("localhost")) {
                return "https://$trimmed"
            }
            return trimmed
        }
        return createSearchUrl(trimmed)
    }

    fun createSearchUrl(query: String, searchEngineUrl: String = "https://www.google.com/search?q="): String {
        return "$searchEngineUrl${URLEncoder.encode(query, "UTF-8")}"
    }

    fun getDomainName(url: String): String {
        return try {
            val uri = Uri.parse(url)
            val host = uri.host ?: return ""
            if (host.startsWith("www.")) host.substring(4) else host
        } catch (e: Exception) {
            ""
        }
    }
}
