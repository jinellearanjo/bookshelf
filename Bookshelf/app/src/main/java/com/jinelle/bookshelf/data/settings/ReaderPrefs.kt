package com.jinelle.bookshelf.data.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.jinelle.bookshelf.domain.model.ReaderFont
import com.jinelle.bookshelf.domain.model.ReaderSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "reader_settings")

/** Two page modes: continuous vertical scroll, or CSS-column pagination (page-turn feel). */
enum class PageMode { SCROLL, PAGINATED }

class ReaderPrefs(private val context: Context) {

    private object Keys {
        val BG_COLOR = stringPreferencesKey("bg_color")
        val TEXT_COLOR = stringPreferencesKey("text_color")
        val FONT = stringPreferencesKey("font")
        val FONT_SIZE = intPreferencesKey("font_size")
        val LINE_HEIGHT = floatPreferencesKey("line_height")
        val PAGE_MODE = stringPreferencesKey("page_mode")
    }

    val settingsFlow: Flow<ReaderSettings> = context.dataStore.data.map { prefs ->
        ReaderSettings(
            bgColorHex = prefs[Keys.BG_COLOR] ?: "#FFFFFF",
            textColorHex = prefs[Keys.TEXT_COLOR] ?: "#000000",
            font = prefs[Keys.FONT]?.let { runCatching { ReaderFont.valueOf(it) }.getOrNull() }
                ?: ReaderFont.TIMES_NEW_ROMAN,
            fontSizePx = prefs[Keys.FONT_SIZE] ?: 18,
            lineHeight = prefs[Keys.LINE_HEIGHT] ?: 1.6f
        )
    }

    val pageModeFlow: Flow<PageMode> = context.dataStore.data.map { prefs ->
        prefs[Keys.PAGE_MODE]?.let { runCatching { PageMode.valueOf(it) }.getOrNull() } ?: PageMode.SCROLL
    }

    suspend fun update(settings: ReaderSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BG_COLOR] = settings.bgColorHex
            prefs[Keys.TEXT_COLOR] = settings.textColorHex
            prefs[Keys.FONT] = settings.font.name
            prefs[Keys.FONT_SIZE] = settings.fontSizePx
            prefs[Keys.LINE_HEIGHT] = settings.lineHeight
        }
    }

    suspend fun setPageMode(mode: PageMode) {
        context.dataStore.edit { prefs -> prefs[Keys.PAGE_MODE] = mode.name }
    }
}
