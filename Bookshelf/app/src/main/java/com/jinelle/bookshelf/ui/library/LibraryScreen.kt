package com.jinelle.bookshelf.ui.library

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.jinelle.bookshelf.data.db.LibrarySort
import com.jinelle.bookshelf.data.db.entities.BookEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBookClick: (BookEntity) -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var sortMenuOpen by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importSingleFile(it) }
    }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { viewModel.scanFolder(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookshelf") },
                actions = {
                    Box {
                        IconButton(onClick = { sortMenuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Sort")
                        }
                        DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Alphabetical") },
                                onClick = { viewModel.setSort(LibrarySort.TITLE); sortMenuOpen = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Last opened") },
                                onClick = { viewModel.setSort(LibrarySort.LAST_OPENED); sortMenuOpen = false }
                            )
                            DropdownMenuItem(
                                text = { Text("By author") },
                                onClick = { viewModel.setSort(LibrarySort.AUTHOR); sortMenuOpen = false }
                            )
                        }
                    }
                    IconButton(onClick = { folderPicker.launch(null) }) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Scan folder")
                    }
                    IconButton(onClick = { filePicker.launch(arrayOf("application/epub+zip")) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add book")
                    }
                }
            )
        }
    ) { padding ->
        if (state.books.isEmpty()) {
            EmptyLibrary(
                modifier = Modifier.padding(padding),
                onAddBook = { filePicker.launch(arrayOf("application/epub+zip")) }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 110.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(state.books, key = { it.id }) { book ->
                    BookGridItem(book = book, onClick = { onBookClick(book) })
                }
            }
        }
    }
}

@Composable
private fun BookGridItem(book: BookEntity, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Card(modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)) {
            if (book.coverImagePath != null) {
                AsyncImage(
                    model = book.coverImagePath,
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = book.title.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(book.title, maxLines = 2, style = MaterialTheme.typography.bodyMedium)
        book.author?.let {
            Text(it, maxLines = 1, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun EmptyLibrary(modifier: Modifier = Modifier, onAddBook: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No books yet", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Add an EPUB to get started.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAddBook) { Text("Add book") }
    }
}
