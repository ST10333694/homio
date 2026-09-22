package com.homio.app

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

object LocaleHelper {

    private const val PREFS_NAME = "homio_prefs"
    private const val KEY_LANGUAGE = "app_language"

    fun getSavedLanguage(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun saveLanguage(context: Context, languageCode: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    fun applyLocale(context: Context): Context {
        val languageCode = getSavedLanguage(context)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}