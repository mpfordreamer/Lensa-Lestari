package com.example.lensalestari.utils

import android.content.Context
import android.content.SharedPreferences

object UrlManager {
    private const val PREFS_NAME = "api_prefs"
    private const val KEY_BASE_URL = "base_url"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveBaseUrl(context: Context, url: String) {
        // Pastikan URL diakhiri dengan /
        val finalUrl = if (url.endsWith("/")) url else "$url/"
        getPrefs(context).edit().putString(KEY_BASE_URL, finalUrl).apply()
    }

    fun getBaseUrl(context: Context): String? {
        return getPrefs(context).getString(KEY_BASE_URL, null)
    }
}