package com.tarantino.xonarx.domain.usecase

import com.tarantino.xonarx.domain.repository.BookmarkRepository
import com.tarantino.xonarx.domain.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

@Singleton
class SuggestionsEngine @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val historyRepository: HistoryRepository
) {
    suspend fun getSuggestions(query: String, identityId: String): List<String> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val results = mutableSetOf<String>()

        // 0. Universal Commands
        val lq = query.lowercase()
        val commands = mapOf(
            "xonar://settings" to listOf("settings", "theme", "tools", "options"),
            "xonar://history" to listOf("history", "recent"),
            "xonar://bookmarks" to listOf("bookmarks", "favorites"),
            "xonar://downloads" to listOf("downloads", "files"),
            "xonar://notes" to listOf("notes", "memo"),
            "xonar://userscripts" to listOf("userscripts", "css", "extensions"),
            "xonar://feeds" to listOf("feeds", "rss", "news")
        )
        for ((route, keywords) in commands) {
            if (keywords.any { it.startsWith(lq) }) {
                results.add(route)
            }
        }

        // 1. History (local)
        try {
            historyRepository.observeHistory(identityId)
                .firstOrNull()
                ?.filter { it.title.contains(query, ignoreCase = true) || it.url.contains(query, ignoreCase = true) }
                ?.take(2)
                ?.forEach { results.add(it.url) }
        } catch (_: Exception) {}

        // 2. Bookmarks/Favorites (local)
        try {
            bookmarkRepository.observeBookmarks(identityId)
                .firstOrNull()
                ?.filter { it.title.contains(query, ignoreCase = true) || it.url.contains(query, ignoreCase = true) }
                ?.take(2)
                ?.forEach { results.add(it.url) }
        } catch (_: Exception) {}

        // 3. Online Search Suggestions
        try {
            val url = URL("https://suggestqueries.google.com/complete/search?client=firefox&q=${java.net.URLEncoder.encode(query, "UTF-8")}")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 1500
            connection.readTimeout = 1500

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(response)
                if (jsonArray.length() >= 2) {
                    val suggestions = jsonArray.getJSONArray(1)
                    val count = minOf(suggestions.length(), 5)
                    for (i in 0 until count) {
                        results.add(suggestions.getString(i))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        results.toList()
    }
}
