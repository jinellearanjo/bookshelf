package com.jinelle.bookshelf.domain.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.jinelle.bookshelf.data.db.AppDatabase
import com.jinelle.bookshelf.data.db.entities.BookEntity
import com.jinelle.bookshelf.data.epub.EpubParser
import java.io.File
import java.io.FileOutputStream

/**
 * Imports an EPUB the user picked via ACTION_OPEN_DOCUMENT. We never copy the book file
 * itself into app storage -- only a persistable read permission is taken on its URI,
 * and we re-open/re-unzip it on demand each time it's read. The one thing we do cache
 * locally is the cover image (small, and needed instantly for the library grid).
 */
class ImportBookUseCase(private val context: Context) {

    private val parser = EpubParser(context)
    private val db = AppDatabase.get(context)

    suspend fun import(uri: Uri): Long {
        // Persist read access across app restarts/reboots -- required since we don't copy the file.
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        val existing = db.bookDao().getByUri(uri.toString())
        if (existing != null) return existing.id

        val book = parser.open(uri)
        val coverImagePath = book.coverPath?.let { path ->
            parser.readEntryBytes(uri, path)?.let { bytes -> cacheCover(uri, bytes) }
        }

        return db.bookDao().insert(
            BookEntity(
                uri = uri.toString(),
                title = book.title,
                author = book.author,
                coverImagePath = coverImagePath
            )
        )
    }

    private fun cacheCover(uri: Uri, bytes: ByteArray): String {
        val fileName = "cover_${uri.toString().hashCode()}.img"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { it.write(bytes) }
        return file.absolutePath
    }
}
