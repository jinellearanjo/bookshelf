package com.jinelle.bookshelf.ui.reader

import android.webkit.JavascriptInterface

/**
 * Registered on the WebView as "AndroidBridge" (see ReaderWebView). Every method here
 * runs on a background JS thread, not the main thread -- callbacks must post back
 * to the main thread before touching any UI state (ReaderViewModel handles this by
 * being called via viewModelScope, which is safe from any thread).
 */
class JsBridge(
    private val onTextSelected: (start: Int, end: Int, text: String) -> Unit,
    private val onScrollProgress: (percent: Float) -> Unit,
    private val onHighlightTapped: (highlightId: String) -> Unit,
    private val onRequestNextChapter: () -> Unit,
    private val onRequestPrevChapter: () -> Unit
) {
    @JavascriptInterface
    fun onTextSelected(start: Int, end: Int, text: String) {
        onTextSelected.invoke(start, end, text)
    }

    @JavascriptInterface
    fun onScrollProgress(percent: Float) {
        onScrollProgress.invoke(percent)
    }

    @JavascriptInterface
    fun onHighlightTapped(highlightId: String) {
        onHighlightTapped.invoke(highlightId)
    }

    /** Called from reader.js's __nextPage() when the user pages past the last page of a chapter. */
    @JavascriptInterface
    fun onRequestNextChapter() {
        onRequestNextChapter.invoke()
    }

    /** Called from reader.js's __prevPage() when the user pages back before the first page of a chapter. */
    @JavascriptInterface
    fun onRequestPrevChapter() {
        onRequestPrevChapter.invoke()
    }
}
