package com.jinelle.bookshelf.ui.library

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jinelle.bookshelf.data.db.AppDatabase
import com.jinelle.bookshelf.data.db.LibrarySort
import com.jinelle.bookshelf.data.db.entities.BookEntity
import com.jinelle.bookshelf.domain.usecase.ImportBookUseCase
import com.jinelle.bookshelf.domain.usecase.ScanLibraryUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryUiState(
    val books: List<BookEntity> = emptyList(),
    val sort: LibrarySort = LibrarySort.LAST_OPENED,
    val isImporting: Boolean = false
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.get(application)
    private val importBook = ImportBookUseCase(application)
    private val scanLibrary = ScanLibraryUseCase(application, importBook)

    private val sortFlow = MutableStateFlow(LibrarySort.LAST_OPENED)

    private val booksFlow = sortFlow.flatMapLatest { sort ->
        when (sort) {
            LibrarySort.TITLE -> db.bookDao().observeByTitle()
            LibrarySort.LAST_OPENED -> db.bookDao().observeByLastOpened()
            LibrarySort.AUTHOR -> db.bookDao().observeByAuthor()
        }
    }

    val uiState: StateFlow<LibraryUiState> = combine(booksFlow, sortFlow) { books, sort ->
        LibraryUiState(books = books, sort = sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryUiState())

    fun setSort(sort: LibrarySort) {
        sortFlow.value = sort
    }

    fun importSingleFile(uri: Uri) {
        viewModelScope.launch { importBook.import(uri) }
    }

    fun scanFolder(treeUri: Uri) {
        viewModelScope.launch { scanLibrary.scanFolder(treeUri) }
    }

    fun deleteBook(book: BookEntity) {
        viewModelScope.launch { db.bookDao().delete(book) }
    }
}
