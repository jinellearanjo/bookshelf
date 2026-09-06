package com.jinelle.bookshelf.ui.toc

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jinelle.bookshelf.data.epub.TocEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TocBottomSheet(
    entries: List<TocEntry>,
    onEntryClick: (TocEntry) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(modifier = Modifier.padding(bottom = 24.dp)) {
            items(flatten(entries)) { (entry, depth) ->
                ListItem(
                    headlineContent = { Text(entry.title) },
                    modifier = Modifier.padding(start = (depth * 16).dp)
                        .clickableItem { onEntryClick(entry) }
                )
                Divider()
            }
        }
    }
}

// Note: Divider() is deprecated in newer Material3 versions in favor of
// HorizontalDivider() -- if Android Studio flags it, swap it in; kept as
// Divider() here for compatibility with the compose-bom version pinned above.

/** Flattens the nested TOC tree into a depth-annotated list for a single LazyColumn. */
private fun flatten(entries: List<TocEntry>, depth: Int = 0): List<Pair<TocEntry, Int>> =
    entries.flatMap { entry -> listOf(entry to depth) + flatten(entry.children, depth + 1) }

// Small helper so ListItem (which has no built-in onClick) stays readable above.
private fun Modifier.clickableItem(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
