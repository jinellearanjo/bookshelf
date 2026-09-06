package com.jinelle.bookshelf.data.epub

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

/**
 * Parses the OPF (package) file: metadata, manifest (every file in the book),
 * and the spine (reading order, by manifest id reference).
 */
internal data class OpfResult(
    val title: String,
    val author: String?,
    val manifest: Map<String, ManifestItem>,   // id -> item
    val spineIds: List<String>,                // ordered list of manifest ids
    val coverManifestId: String?,
    val navHref: String?,                      // EPUB3 nav document href (manifest properties="nav")
    val ncxId: String?                         // EPUB2 toc.ncx manifest id (spine toc attribute)
)

internal data class ManifestItem(
    val id: String,
    val href: String,
    val mediaType: String,
    val properties: String?
)

internal object OpfParser {

    fun parse(input: InputStream): OpfResult {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(input, null)

        var title = "Untitled"
        var author: String? = null
        val manifest = mutableMapOf<String, ManifestItem>()
        val spineIds = mutableListOf<String>()
        var coverManifestId: String? = null
        var navHref: String? = null
        var ncxId: String? = null

        var inMetadata = false
        var currentTextTag: String? = null
        val textBuffer = StringBuilder()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "metadata" -> inMetadata = true
                        "title" -> if (inMetadata) { currentTextTag = "title"; textBuffer.clear() }
                        "creator" -> if (inMetadata) { currentTextTag = "creator"; textBuffer.clear() }
                        "meta" -> {
                            // EPUB2 cover: <meta name="cover" content="manifest-id"/>
                            val name = parser.getAttributeValue(null, "name")
                            if (name == "cover") {
                                coverManifestId = parser.getAttributeValue(null, "content")
                            }
                        }
                        "item" -> {
                            val id = parser.getAttributeValue(null, "id") ?: ""
                            val href = parser.getAttributeValue(null, "href") ?: ""
                            val mediaType = parser.getAttributeValue(null, "media-type") ?: ""
                            val properties = parser.getAttributeValue(null, "properties")
                            manifest[id] = ManifestItem(id, href, mediaType, properties)
                            if (properties?.contains("nav") == true) navHref = href
                            if (properties?.contains("cover-image") == true) coverManifestId = id
                        }
                        "itemref" -> {
                            parser.getAttributeValue(null, "idref")?.let { spineIds.add(it) }
                        }
                        "spine" -> {
                            ncxId = parser.getAttributeValue(null, "toc")
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (currentTextTag != null) textBuffer.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "metadata" -> inMetadata = false
                        "title" -> if (currentTextTag == "title") {
                            title = textBuffer.toString().trim().ifEmpty { title }
                            currentTextTag = null
                        }
                        "creator" -> if (currentTextTag == "creator") {
                            author = textBuffer.toString().trim().ifEmpty { null }
                            currentTextTag = null
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return OpfResult(title, author, manifest, spineIds, coverManifestId, navHref, ncxId)
    }
}
