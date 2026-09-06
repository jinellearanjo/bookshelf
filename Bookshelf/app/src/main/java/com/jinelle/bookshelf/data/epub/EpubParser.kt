package com.jinelle.bookshelf.data.epub

import android.content.Context
import android.net.Uri
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

/**
 * Top-level entry point for reading an EPUB. Books are never copied into app storage --
 * we hold a persistable URI permission (see MainActivity) and re-open/re-unzip on demand.
 * A zip is read sequentially per open() call; this is fine for phone-sized EPUBs (a few MB)
 * and avoids keeping the whole archive decompressed in memory between sessions.
 */
class EpubParser(private val context: Context) {

    /** Parses just enough to build the EpubBook model (metadata, spine, TOC). */
    fun open(uri: Uri): EpubBook {
        // Pass 1: find container.xml -> OPF path
        val opfPath = readEntry(uri, "META-INF/container.xml") { stream ->
            parseContainer(stream)
        } ?: error("Invalid EPUB: no container.xml/OPF reference found")

        val opfDir = opfPath.substringBeforeLast('/', "")

        // Pass 2: parse the OPF itself
        val opf = readEntry(uri, opfPath) { stream -> OpfParser.parse(stream) }
            ?: error("Invalid EPUB: OPF file not found at $opfPath")

        val chapters = opf.spineIds.mapIndexedNotNull { index, id ->
            opf.manifest[id]?.let { item ->
                EpubChapter(id = item.id, href = resolvePath(opfDir, item.href), order = index)
            }
        }

        // Pass 3: TOC -- prefer EPUB3 nav.xhtml, fall back to NCX
        val toc: List<TocEntry> = when {
            opf.navHref != null -> {
                val navPath = resolvePath(opfDir, opf.navHref)
                val navDir = navPath.substringBeforeLast('/', "")
                val rawToc = readEntry(uri, navPath) { NavParser.parseNavXhtml(it) } ?: emptyList()
                rewriteHrefs(rawToc, navDir)
            }
            opf.ncxId != null -> {
                val ncxHref = opf.manifest[opf.ncxId]?.href
                if (ncxHref != null) {
                    val ncxPath = resolvePath(opfDir, ncxHref)
                    val ncxDir = ncxPath.substringBeforeLast('/', "")
                    val rawToc = readEntry(uri, ncxPath) { NavParser.parseNcx(it) } ?: emptyList()
                    rewriteHrefs(rawToc, ncxDir)
                } else emptyList()
            }
            else -> emptyList()
        }

        val coverPath = opf.coverManifestId?.let { opf.manifest[it]?.href }?.let { resolvePath(opfDir, it) }

        return EpubBook(
            title = opf.title,
            author = opf.author,
            coverPath = coverPath,
            chapters = chapters,
            toc = toc
        )
    }

    /** Reads a single chapter's raw XHTML as a UTF-8 string, for feeding into the WebView. */
    fun readChapterHtml(uri: Uri, href: String): String {
        return readEntry(uri, href) { it.readBytes().toString(Charsets.UTF_8) }
            ?: error("Chapter not found: $href")
    }

    /** Reads any raw entry (e.g. a cover image) as bytes. */
    fun readEntryBytes(uri: Uri, path: String): ByteArray? {
        return readEntry(uri, path) { it.readBytes() }
    }

    // -- internals --

    private fun parseContainer(input: java.io.InputStream): String {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(input, null)
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "rootfile") {
                return parser.getAttributeValue(null, "full-path") ?: ""
            }
            eventType = parser.next()
        }
        error("No <rootfile> found in container.xml")
    }

    private fun resolvePath(baseDir: String, relative: String): String {
        if (relative.startsWith("/")) return relative.trimStart('/')
        if (baseDir.isEmpty()) return relative
        // basic relative path resolution, handles "../" segments
        val baseParts = baseDir.split("/").toMutableList()
        val relParts = relative.substringBefore("#").split("/")
        for (part in relParts) {
            when (part) {
                "..", "." -> if (part == "..") baseParts.removeLastOrNull()
                else -> baseParts.add(part)
            }
        }
        return baseParts.joinToString("/")
    }

    private fun rewriteHrefs(entries: List<TocEntry>, dir: String): List<TocEntry> =
        entries.map { entry ->
            entry.copy(
                href = resolvePath(dir, entry.href),
                children = rewriteHrefs(entry.children, dir)
            )
        }

    /** Streams through the zip once, invoking [block] on the entry matching [path]. */
    private fun <T> readEntry(uri: Uri, path: String, block: (java.io.InputStream) -> T): T? {
        context.contentResolver.openInputStream(uri)?.use { fileStream ->
            ZipInputStream(fileStream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name.trimStart('/') == path.trimStart('/')) {
                        // ZipInputStream doesn't support mark/reset well across all impls;
                        // buffer into memory since EPUB chapters/OPF/nav files are small (KB, not MB).
                        val buffer = ByteArrayOutputStream()
                        zip.copyTo(buffer)
                        return block(buffer.toByteArray().inputStream())
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }
        return null
    }
}
