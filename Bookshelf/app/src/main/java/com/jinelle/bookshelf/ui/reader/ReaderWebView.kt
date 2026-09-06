package com.jinelle.bookshelf.ui.reader

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import com.jinelle.bookshelf.data.db.entities.HighlightEntity
import com.jinelle.bookshelf.data.settings.PageMode
import com.jinelle.bookshelf.domain.model.ReaderSettings

/** Left/right margin (px) used for the paginated-mode column layout, mirrors the CSS padding. */
private const val PAGINATION_MARGIN_PX = 16

/**
 * Renders one chapter's HTML. Owns the WebView instance (kept alive across recompositions
 * via `remember`) plus the invisible tap zones for page/chapter turning, since both need
 * direct access to the same WebView to call reader.js's pagination functions.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ReaderWebView(
    chapterHtml: String,
    settings: ReaderSettings,
    pageMode: PageMode,
    highlights: List<HighlightEntity>,
    initialScrollPercent: Float,
    onTextSelected: (start: Int, end: Int, text: String) -> Unit,
    onScrollProgress: (percent: Float) -> Unit,
    onHighlightTapped: (highlightId: String) -> Unit,
    onRequestNextChapter: () -> Unit,
    onRequestPrevChapter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    // WebViewClient/JsBridge are created once (in factory), but chapter/highlights/scroll/
    // pageMode change on every recomposition -- a plain closure over those params would only
    // ever see the FIRST composition's values inside onPageFinished. Route through a mutable
    // holder that `update` refreshes before every load, so onPageFinished always reads current data.
    val pendingApplyData = remember { PendingApplyDataHolder() }
    pendingApplyData.highlights = highlights
    pendingApplyData.initialScrollPercent = initialScrollPercent
    pendingApplyData.pageMode = pageMode

    val bridge = remember {
        JsBridge(
            onTextSelected = onTextSelected,
            onScrollProgress = onScrollProgress,
            onHighlightTapped = onHighlightTapped,
            onRequestNextChapter = onRequestNextChapter,
            onRequestPrevChapter = onRequestPrevChapter
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.allowFileAccess = true
                    addJavascriptInterface(bridge, "AndroidBridge")
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String?) {
                            if (pendingApplyData.pageMode == PageMode.PAGINATED) {
                                view.evaluateJavascript(
                                    "window.__setupPagination($PAGINATION_MARGIN_PX);", null
                                )
                            } else {
                                view.evaluateJavascript("window.__disablePagination();", null)
                            }
                            // Highlights before scroll restore -- <mark> wrapping changes text
                            // node structure, so scroll/page restoration must happen after.
                            for (h in pendingApplyData.highlights) {
                                view.evaluateJavascript(
                                    "window.__applyHighlight(${h.startOffset}, ${h.endOffset}, '${h.color}', '${h.id}');",
                                    null
                                )
                            }
                            view.evaluateJavascript(
                                "window.__restoreScroll(${pendingApplyData.initialScrollPercent});",
                                null
                            )
                        }
                    }
                    webViewRef.value = this
                }
            },
            update = { view ->
                val styleTag = CssInjector.buildFullStyleTag(settings)
                val readerJsTag = "<script src=\"file:///android_asset/reader.js\"></script>"
                val fullHtml = injectIntoHead(chapterHtml, styleTag + readerJsTag)
                view.loadDataWithBaseURL(
                    "file:///android_asset/",
                    fullHtml,
                    "text/html",
                    "utf-8",
                    null
                )
            }
        )

        // Invisible edge tap-zones. In paginated mode these turn pages within the chapter
        // (reader.js hands off to onRequestNextChapter/PrevChapter at the chapter's edges).
        // In scroll mode there's no "page" concept, so a tap here jumps chapters directly.
        // Known tradeoff: these zones intercept touches at the screen edges, so text
        // selection right at the edge of a page won't work -- fine for v1, worth revisiting
        // (e.g. shrink the zones, or gate them behind a swipe instead of a tap).
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight()
                    .pointerInput(pageMode) {
                        detectTapGestures(onTap = {
                            if (pageMode == PageMode.PAGINATED) {
                                webViewRef.value?.evaluateJavascript("window.__prevPage();", null)
                            } else {
                                onRequestPrevChapter()
                            }
                        })
                    }
            )
            Spacer(modifier = Modifier.weight(4f))
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight()
                    .pointerInput(pageMode) {
                        detectTapGestures(onTap = {
                            if (pageMode == PageMode.PAGINATED) {
                                webViewRef.value?.evaluateJavascript("window.__nextPage();", null)
                            } else {
                                onRequestNextChapter()
                            }
                        })
                    }
            )
        }
    }
}

/** Mutable holder so the WebViewClient (created once) always reads the latest chapter data. */
private class PendingApplyDataHolder {
    var highlights: List<HighlightEntity> = emptyList()
    var initialScrollPercent: Float = 0f
    var pageMode: PageMode = PageMode.SCROLL
}

/** Inserts [content] just before </head>, or at the very start if the chapter has no <head>. */
private fun injectIntoHead(html: String, content: String): String {
    val headCloseIndex = html.indexOf("</head>", ignoreCase = true)
    return if (headCloseIndex != -1) {
        html.substring(0, headCloseIndex) + content + html.substring(headCloseIndex)
    } else {
        content + html
    }
}
