package com.tarantino.xonarx.domain.usecase

import android.webkit.WebView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReaderModeEngine @Inject constructor() {

    fun enableReaderMode(webView: WebView) {
        val js = """
            (function() {
                var body = document.querySelector('body');
                if (body) {
                    body.style.backgroundColor = '#fdfbfb';
                    body.style.color = '#333333';
                    body.style.fontFamily = 'serif';
                    body.style.fontSize = '18px';
                    body.style.lineHeight = '1.6';
                    body.style.padding = '5%';
                    
                    var elements = body.querySelectorAll('*');
                    for (var i = 0; i < elements.length; i++) {
                        var el = elements[i];
                        if (el.tagName !== 'P' && el.tagName !== 'H1' && el.tagName !== 'H2' && el.tagName !== 'IMG' && el.tagName !== 'A') {
                            el.style.display = 'none';
                        } else {
                            el.style.display = 'block';
                            el.style.maxWidth = '100%';
                            el.style.height = 'auto';
                        }
                    }
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }
}
