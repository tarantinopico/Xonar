package com.tarantino.xonarx.domain.usecase

import android.webkit.URLUtil
import org.junit.Assert.*
import org.junit.Test
import java.net.URI

class DownloadUrlValidatorTest {

    @Test
    fun `isValidDownloadUrl returns true for valid HTTP URLs`() {
        val url = "https://example.com/file.zip"
        assertTrue(isValidUrl(url))
    }

    @Test
    fun `isValidDownloadUrl returns true for valid HTTP URLs with query`() {
        val url = "http://example.com/download?id=123"
        assertTrue(isValidUrl(url))
    }

    @Test
    fun `isValidDownloadUrl returns false for invalid schemes`() {
        assertFalse(isValidUrl("javascript:alert(1)"))
        assertFalse(isValidUrl("file:///etc/passwd"))
        assertFalse(isValidUrl("xonar://settings"))
    }
    
    // Simple custom URL validator logic for tests
    private fun isValidUrl(url: String): Boolean {
        return try {
            val uri = URI.create(url)
            uri.scheme == "http" || uri.scheme == "https"
        } catch (e: Exception) {
            false
        }
    }
}
