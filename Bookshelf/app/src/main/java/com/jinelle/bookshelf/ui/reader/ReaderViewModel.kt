package com.jinelle.bookshelf.ui.reader

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jinelle.bookshelf.data.db.AppDatabase
import com.jinelle.bookshelf.data.db.entities.HighlightEntity
import com.jinelle.bookshelf.data.db.entities.ProgressEntity
import com.jinelle.bookshelf.data.epub.EpubBook
import com.jinelle.bookshelf.data.epub.EpubParser
import com.jinelle.bookshelf.data.settings.PageMode
import com.jinelle.bookshelf.data.settings.ReaderPrefs
import com.jinelle.bookshelf.domain.model.HighlightColor
import com.jinelle.bookshelf.domain.model.ReaderSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReaderUiState(
    val book: EpubBook? = null,
    val bookId: Long = 0,
    val chapterIndex: Int = 0,
    val chapterHtml: String = "",
    val highlights: List<HighlightEntity> = emptyList(),
    val settings: ReaderSettings = ReaderSettings(),
    val pageMode: PageMode = PageMode.SCROLL,
    val initialScrollPercent: Float = 0f,
    val isLoading: Boolean = true,
    // non-null while the highlight/note popup should be shown for a pending selection
    val pendingSelection: PendingSelection? = null
)

data class PendingSelection(val start: Int, val end: Int, val text: String)

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val parser = EpubParser(application)
    private val db = AppDatabase.get(application)
    private val prefs = ReaderPrefs(application)

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var bookUri: Uri? = null
    private var lastScrollPercent: Float = 0f

    init {
        viewModelScope.launch {
            prefs.settingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
            }
        }
        viewModelScope.launch {
            prefs.pageModeFlow.collect { mode ->
                _uiState.value = _uiState.value.copy(pageMode = mode)
            }
        }
    }

    fun openBook(bookId: Long, uri: Uri) {
        bookUri = uri
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, bookId = bookId)
            val book = parser.open(uri)
            val progress = db.progressDao().getForBook(bookId)
            db.bookDao().markOpened(bookId)

            _uiState.value = _uiState.value.copy(
                book = book,
                bookId = bookId,
                chapterIndex = progress?.chapterIndex ?: 0,
                initialScrollPercent = progress?.scrollPercent ?: 0f,
                isLoading = false
            )
            loadChapter(progress?.chapterIndex ?: 0)
        }
    }

    fun loadChapter(index: Int) {
        val book = _uiState.value.book ?: return
        val uri = bookUri ?: return
        if (index < 0 || index >= book.chapters.size) return

        viewModelScope.launch {
            val html = parser.readChapterHtml(uri, book.chapters[index].href)
            val highlights = db.highlightDao().getForChapter(_uiState.value.bookId, index)
            _uiState.value = _uiState.value.copy(
                chapterIndex = index,
                chapterHtml = html,
                highlights = highlights,
                initialScrollPercent = if (index == _uiState.value.chapterIndex) lastScrollPercent else 0f
            )
        }
    }

    fun nextChapter() = loadChapter(_uiState.value.chapterIndex + 1)
    fun previousChapter() = loadChapter(_uiState.value.chapterIndex - 1)

    fun jumpToHref(href: String) {
        val book = _uiState.value.book ?: return
        // Strip any #fragment for chapter matching -- fragment-level scroll is a
        // reasonable v2 addition (would need reader.js to scrollIntoView by id).
        val path = href.substringBefore("#")
        val index = book.chapters.indexOfFirst { it.href == path }
        if (index != -1) loadChapter(index)
    }

    fun onScrollProgress(percent: Float) {
        lastScrollPercent = percent
        viewModelScope.launch {
            db.progressDao().upsert(
                ProgressEntity(
                    bookId = _uiState.value.bookId,
                    chapterIndex = _uiState.value.chapterIndex,
                    scrollPercent = percent
                )
            )
        }
    }

    fun onTextSelected(start: Int, end: Int, text: String) {
        _uiState.value = _uiState.value.copy(pendingSelection = PendingSelection(start, end, text))
    }

    fun dismissPendingSelection() {
        _uiState.value = _uiState.value.copy(pendingSelection = null)
    }

    /** Saves a new highlight for the current pending selection, with an optional note. */
    fun confirmHighlight(color: HighlightColor, note: String?) {
        val pending = _uiState.value.pendingSelection ?: return
        val state = _uiState.value
        viewModelScope.launch {
            val entity = HighlightEntity(
                bookId = state.bookId,
                chapterIndex = state.chapterIndex,
                startOffset = pending.start,
                endOffset = pending.end,
                selectedText = pending.text,
                color = color.hex,
                note = note?.ifBlank { null }
            )
            db.highlightDao().insert(entity)
            _uiState.value = _uiState.value.copy(pendingSelection = null)
            loadChapter(state.chapterIndex) // reload so the new <mark> renders
        }
    }

    fun deleteHighlight(highlight: HighlightEntity) {
        viewModelScope.launch {
            db.highlightDao().delete(highlight)
            loadChapter(_uiState.value.chapterIndex)
        }
    }

    fun updateSettings(settings: ReaderSettings) {
        viewModelScope.launch { prefs.update(settings) }
    }

    fun setPageMode(mode: PageMode) {
        viewModelScope.launch { prefs.setPageMode(mode) }
    }
}
