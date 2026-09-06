package com.jinelle.bookshelf.domain.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

/**
 * Optional "library folder" auto-detection, picked via ACTION_OPEN_DOCUMENT_TREE.
 * Scans (non-recursively, one level -- most people keep books in a flat folder)
 * for .epub files and imports each one that isn't already in the library.
 * Kept separate from ImportBookUseCase so a single-file pick and a folder scan
 * can be triggered independently from the library screen.
 */
class ScanLibraryUseCase(
    private val context: Context,
    private val importBook: ImportBookUseCase
) {
    suspend fun scanFolder(treeUri: Uri): Int {
        context.contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        val dir = DocumentFile.fromTreeUri(context, treeUri) ?: return 0
        var imported = 0
        for (file in dir.listFiles()) {
            if (file.isFile && file.name?.endsWith(".epub", ignoreCase = true) == true) {
                importBook.import(file.uri)
                imported++
            }
        }
        return imported
    }
}
