package com.jinelle.bookshelf.data.epub

/**
 * In-memory representation of a parsed EPUB, built fresh each time a book is opened.
 * Nothing here is persisted directly -- BookEntity in the Room DB stores only
 * the file URI + metadata needed for the library list. This is re-parsed on open.
 */
data class EpubBook(
    val title: String,
    val author: String?,
    val coverPath: String?,       // path inside the zip, resolved via EpubParser.readEntryBytes
    val chapters: List<EpubChapter>,
    val toc: List<TocEntry>
)

data class EpubChapter(
    val id: String,               // manifest item id
    val href: String,             // path inside the zip, relative to OPF directory
    val order: Int
)

data class TocEntry(
    val title: String,
    val href: String,             // may include a #fragment
    val children: List<TocEntry> = emptyList()
)
