package com.jinelle.bookshelf.ui.reader

import com.jinelle.bookshelf.domain.model.ReaderFont
import com.jinelle.bookshelf.domain.model.ReaderSettings
import com.jinelle.bookshelf.domain.model.fontAssetMap

/**
 * Builds the CSS injected into every chapter's <head>. Two responsibilities kept separate:
 * font-face declarations (register the 3 fonts' weight files once) and body styling
 * (colors/size/line-height, applied via !important since books often ship inline styles).
 */
object CssInjector {

    fun buildFontFaceCss(): String = buildString {
        for ((font, files) in fontAssetMap) {
            appendLine(fontFaceBlock(font.cssFamily, "normal", "normal", files.regular))
            appendLine(fontFaceBlock(font.cssFamily, "bold", "normal", files.bold))
            appendLine(fontFaceBlock(font.cssFamily, "normal", "italic", files.italic))
            appendLine(fontFaceBlock(font.cssFamily, "bold", "italic", files.boldItalic))
        }
    }

    private fun fontFaceBlock(family: String, weight: String, style: String, assetPath: String) = """
        @font-face {
            font-family: '$family';
            src: url('file:///android_asset/$assetPath');
            font-weight: $weight;
            font-style: $style;
        }
    """.trimIndent()

    /**
     * Body + universal-override styling. The wildcard rule matters more than it looks --
     * many EPUBs hardcode colors on spans/divs, and without forcing inheritance those
     * elements silently ignore the reader's background/text color choice.
     */
    fun buildBodyCss(settings: ReaderSettings): String = """
        html, body {
            background-color: ${settings.bgColorHex} !important;
            color: ${settings.textColorHex} !important;
            font-family: '${settings.font.cssFamily}', serif !important;
            font-size: ${settings.fontSizePx}px !important;
            line-height: ${settings.lineHeight} !important;
            margin: 0;
            padding: 16px;
            word-wrap: break-word;
        }
        * {
            color: inherit !important;
            background-color: transparent !important;
            max-width: 100% !important;
        }
        img { height: auto; }
        mark {
            /* background-color set inline per-highlight by JsBridge.applyHighlights() */
            border-radius: 2px;
        }
    """.trimIndent()

    /** Full <style> block combining both, injected once per chapter load. */
    fun buildFullStyleTag(settings: ReaderSettings): String =
        "<style>\n${buildFontFaceCss()}\n${buildBodyCss(settings)}\n</style>"
}
