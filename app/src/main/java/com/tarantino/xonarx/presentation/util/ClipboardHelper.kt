package com.tarantino.xonarx.presentation.util

import android.content.ClipboardManager
import android.content.Context
import android.webkit.URLUtil

object ClipboardHelper {
    fun getClipboardUrl(context: Context): String? {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString()?.trim()
                if (text != null && (URLUtil.isValidUrl(text) || text.startsWith("www.") || text.contains("."))) {
                    // Check if it's reasonably a URL or search term that looks like a URL
                    if (!text.contains(" ") && text.length < 500) {
                        return text
                    }
                }
            }
        }
        return null
    }
}
