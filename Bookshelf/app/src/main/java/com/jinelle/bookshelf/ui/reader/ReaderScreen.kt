package com.jinelle.bookshelf.ui.reader

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jinelle.bookshelf.ui.highlights.HighlightPopup
import com.jinelle.bookshelf.ui.settings.ReaderSettingsSheet
import com.jinelle.bookshelf.ui.toc.TocBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: Long,
    bookUri: Uri,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showToc by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(bookId, bookUri) {
        viewModel.openBook(bookId, bookUri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.book?.title ?: "", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showToc = true }) {
                        Icon(Icons.Default.List, contentDescription = "Table of contents")
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Reader settings")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                // Tap-zone page/chapter navigation lives inside ReaderWebView itself now --
                // it needs direct access to the WebView instance to call reader.js's
                // pagination functions, so it can't live as a sibling overlay here anymore.
                ReaderWebView(
                    chapterHtml = state.chapterHtml,
                    settings = state.settings,
                    pageMode = state.pageMode,
                    highlights = state.highlights,
                    initialScrollPercent = state.initialScrollPercent,
                    targetFragmentId = state.pendingFragmentId,
                    onTextSelected = viewModel::onTextSelected,
                    onScrollProgress = viewModel::onScrollProgress,
                    onHighlightTapped = { id ->
                        state.highlights.find { it.id.toString() == id }?.let { viewModel.deleteHighlight(it) }
                    },
                    onRequestNextChapter = viewModel::nextChapter,
                    onRequestPrevChapter = viewModel::previousChapter,
                    onFragmentConsumed = viewModel::consumeFragmentJump
                )
            }
        }
    }

    if (showToc && state.book != null) {
        TocBottomSheet(
            entries = state.book!!.toc,
            onEntryClick = { entry ->
                viewModel.jumpToHref(entry.href)
                showToc = false
            },
            onDismiss = { showToc = false }
        )
    }

    if (showSettings) {
        ReaderSettingsSheet(
            settings = state.settings,
            pageMode = state.pageMode,
            onSettingsChange = viewModel::updateSettings,
            onPageModeChange = viewModel::setPageMode,
            onDismiss = { showSettings = false }
        )
    }

    state.pendingSelection?.let { pending ->
        HighlightPopup(
            selectedText = pending.text,
            onConfirm = { color, note -> viewModel.confirmHighlight(color, note) },
            onDismiss = viewModel::dismissPendingSelection
        )
    }
}
