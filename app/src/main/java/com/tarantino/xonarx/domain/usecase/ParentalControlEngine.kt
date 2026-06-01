package com.tarantino.xonarx.domain.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParentalControlEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("ParentalControls", Context.MODE_PRIVATE)

    fun isIdentityRestricted(identityId: String): Boolean {
        return prefs.getBoolean("restricted_$identityId", false)
    }

    fun setIdentityRestricted(identityId: String, isRestricted: Boolean) {
        prefs.edit().putBoolean("restricted_$identityId", isRestricted).apply()
    }

    fun getApprovedDomains(identityId: String): List<String> {
        val jsonStr = prefs.getString("domains_$identityId", "[]") ?: "[]"
        val arr = JSONArray(jsonStr)
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            list.add(arr.getString(i))
        }
        return list
    }

    fun setApprovedDomains(identityId: String, domains: List<String>) {
        val arr = JSONArray()
        domains.forEach { arr.put(it) }
        prefs.edit().putString("domains_$identityId", arr.toString()).apply()
    }

    fun isUrlAllowed(identityId: String, url: String): Boolean {
        if (!isIdentityRestricted(identityId)) return true
        val domains = getApprovedDomains(identityId)
        if (domains.isEmpty()) return false // Restricted but no domains? Allow nothing.
        
        return domains.any { url.contains(it, ignoreCase = true) }
    }
}
