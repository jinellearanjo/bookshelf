package com.jinelle.bookshelf.data.epub

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

/**
 * Parses the table of contents. EPUB3 books use an XHTML nav document
 * (a <nav epub:type="toc"> containing nested <ol>/<li>/<a> elements).
 * EPUB2 books use a separate NCX XML format with <navPoint> elements.
 * Real-world books are inconsistent about which they include -- EpubParser
 * decides which of these two functions to call based on what OpfParser found.
 */
internal object NavParser {

    /** EPUB3: parses nav.xhtml's <ol><li><a href="...">Title</a><ol>...nested</ol></li></ol> structure. */
    fun parseNavXhtml(input: InputStream): List<TocEntry> {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(input, null)

        // Find the <nav> whose epub:type contains "toc" (fallback: first <nav> found)
        var eventType = parser.eventType
        var depth = 0
        var inTargetNav = false
        var navDepth = -1

        val root = mutableListOf<TocEntry>()
        val stack = ArrayDeque<MutableList<TocEntry>>()
        var pendingHref: String? = null
        var pendingTitle = StringBuilder()
        var capturingLinkText = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    depth++
                    when (parser.name) {
                        "nav" -> {
                            val type = parser.getAttributeValue(null, "type") ?: parser.getAttributeValue(
                                "http://www.idpf.org/2007/ops", "type"
                            )
                            if (!inTargetNav && (type == null || type.contains("toc"))) {
                                inTargetNav = true
                                navDepth = depth
                                stack.addLast(root)
                            }
                        }
                        "ol" -> if (inTargetNav && stack.isNotEmpty()) {
                            // Nested <ol> becomes children of the *last* entry added to current list
                            val current = stack.last()
                            if (current.isNotEmpty() && depth > navDepth + 1) {
                                val last = current.removeAt(current.size - 1)
                                val childList = mutableListOf<TocEntry>()
                                current.add(last.copy(children = childList))
                                stack.addLast(childList)
                            }
                        }
                        "a" -> if (inTargetNav) {
                            pendingHref = parser.getAttributeValue(null, "href")
                            pendingTitle = StringBuilder()
                            capturingLinkText = true
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (capturingLinkText) pendingTitle.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "a" -> if (inTargetNav && capturingLinkText) {
                            capturingLinkText = false
                            val href = pendingHref
                            val title = pendingTitle.toString().trim()
                            if (href != null && title.isNotEmpty() && stack.isNotEmpty()) {
                                stack.last().add(TocEntry(title, href))
                            }
                        }
                        "ol" -> if (inTargetNav && stack.size > 1) {
                            stack.removeLast()
                        }
                        "nav" -> if (inTargetNav && depth == navDepth) {
                            inTargetNav = false
                        }
                    }
                    depth--
                }
            }
            eventType = parser.next()
        }
        return root
    }

    /** EPUB2: parses toc.ncx's <navPoint><navLabel><text>...</text></navLabel><content src="..."/> ... structure. */
    fun parseNcx(input: InputStream): List<TocEntry> {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(input, null)

        val root = mutableListOf<TocEntry>()
        val stack = ArrayDeque<MutableList<TocEntry>>().apply { addLast(root) }
        val pendingChildLists = ArrayDeque<MutableList<TocEntry>>()

        var inLabelText = false
        var currentLabel = StringBuilder()
        var currentSrc: String? = null

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "navPoint" -> {
                            currentLabel = StringBuilder()
                            currentSrc = null
                        }
                        "text" -> inLabelText = true
                        "content" -> currentSrc = parser.getAttributeValue(null, "src")
                    }
                }
                XmlPullParser.TEXT -> if (inLabelText) currentLabel.append(parser.text)
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "text" -> inLabelText = false
                        "navPoint" -> {
                            val title = currentLabel.toString().trim()
                            val href = currentSrc
                            if (title.isNotEmpty() && href != null) {
                                val children = mutableListOf<TocEntry>()
                                stack.last().add(TocEntry(title, href, children))
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return root
    }
}
