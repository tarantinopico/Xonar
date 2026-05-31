package com.tarantino.xonarx.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UrlHelperTest {

    private val urlHelper = UrlHelper()

    @Test
    fun isUrl_withValidHttpUrl_returnsTrue() {
        assertTrue(urlHelper.isUrl("http://example.com"))
        assertTrue(urlHelper.isUrl("https://example.com"))
    }

    @Test
    fun isUrl_withValidWwwUrl_returnsTrue() {
        assertTrue(urlHelper.isUrl("www.example.com"))
    }

    @Test
    fun isUrl_withInvalidUrl_returnsFalse() {
        assertFalse(urlHelper.isUrl("example domain"))
        assertFalse(urlHelper.isUrl("just text"))
    }

    @Test
    fun normalizeUrl_withHttp_returnsSame() {
        assertEquals("http://example.com", urlHelper.normalizeUrl("http://example.com"))
    }

    @Test
    fun normalizeUrl_withoutHttp_appendsHttps() {
        assertEquals("https://example.com", urlHelper.normalizeUrl("example.com"))
    }

    @Test
    fun createSearchUrl_formatsCorrectly() {
        val result = urlHelper.createSearchUrl("test query", "https://www.google.com/search?q=")
        assertEquals("https://www.google.com/search?q=test+query", result)
    }
}
