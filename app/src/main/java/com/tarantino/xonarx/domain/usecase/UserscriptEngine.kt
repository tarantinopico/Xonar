package com.tarantino.xonarx.domain.usecase

import com.tarantino.xonarx.domain.model.Userscript
import com.tarantino.xonarx.domain.repository.UserscriptRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserscriptEngine @Inject constructor(
    private val repository: UserscriptRepository
) {
    suspend fun getScriptsForIdentity(identityId: String): List<Userscript> {
        return repository.getScriptsForIdentity(identityId).firstOrNull() ?: emptyList()
    }

    fun injectScripts(view: android.webkit.WebView, url: String, scripts: List<Userscript>) {
        val uri = android.net.Uri.parse(url)
        val host = uri.host ?: return
        
        scripts.filter { it.isEnabled }.forEach { script ->
            val matches = script.domain.isNullOrBlank() || host.contains(script.domain)
            if (matches) {
                if (script.isCss) {
                    val encodedCss = android.util.Base64.encodeToString(script.code.toByteArray(), android.util.Base64.NO_WRAP)
                    val js = "(function() { " +
                            "var parent = document.getElementsByTagName('head').item(0); " +
                            "var style = document.createElement('style'); " +
                            "style.type = 'text/css'; " +
                            "style.innerHTML = window.atob('$encodedCss'); " +
                            "parent.appendChild(style)" +
                            "})()"
                    view.evaluateJavascript(js, null)
                } else {
                    view.evaluateJavascript("(function() { ${script.code} })()", null)
                }
            }
        }
    }
}
