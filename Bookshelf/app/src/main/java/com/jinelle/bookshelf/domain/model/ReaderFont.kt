package com.jinelle.bookshelf.domain.model

/**
 * 4 fonts: PT Serif, Domine, Lato, and a default styled as "Times New Roman."
 *
 * Honest note on that last one: actual Times New Roman is a Monotype font, licensed
 * for Windows/Office only -- it can't legally be bundled in an APK. What's bundled
 * instead is Tinos, Google's metric-compatible open-source clone (Apache 2.0 license,
 * designed specifically to substitute for Times New Roman with matching character
 * widths). Visually it's very close; it's labelled "Times New Roman" in the UI since
 * that's the reading feel being asked for, but it's worth knowing it's a substitute.
 *
 * Each font ships regular/bold/italic/bold-italic files EXCEPT Domine, which as of
 * this writing doesn't have an italic release upstream (confirmed via Google Fonts/
 * Typewolf) -- its italic/bold-italic slots fall back to the upright files, so
 * <i>/<em> text in Domine will render upright rather than slanted. Everything else
 * gets real weight files, not faux-bold/oblique browser synthesis.
 */
enum class ReaderFont(val displayName: String, val cssFamily: String) {
    TIMES_NEW_ROMAN("Times New Roman", "TimesNewRomanSub"),
    PT_SERIF("PT Serif", "PTSerif"),
    DOMINE("Domine", "Domine"),
    LATO("Lato", "Lato")
}

internal data class FontWeightFiles(
    val regular: String,
    val bold: String,
    val italic: String,
    val boldItalic: String
)

internal val fontAssetMap: Map<ReaderFont, FontWeightFiles> = mapOf(
    ReaderFont.TIMES_NEW_ROMAN to FontWeightFiles(
        regular = "fonts/Tinos-Regular.ttf",
        bold = "fonts/Tinos-Bold.ttf",
        italic = "fonts/Tinos-Italic.ttf",
        boldItalic = "fonts/Tinos-BoldItalic.ttf"
    ),
    ReaderFont.PT_SERIF to FontWeightFiles(
        regular = "fonts/PTSerif-Regular.ttf",
        bold = "fonts/PTSerif-Bold.ttf",
        italic = "fonts/PTSerif-Italic.ttf",
        boldItalic = "fonts/PTSerif-BoldItalic.ttf"
    ),
    ReaderFont.DOMINE to FontWeightFiles(
        regular = "fonts/Domine-Regular.ttf",
        bold = "fonts/Domine-Bold.ttf",
        // No italic upstream -- point both at the upright files rather than omitting
        // the @font-face rule entirely, so <i>/<em> still renders (just not slanted)
        // instead of silently falling back to a totally different system font.
        italic = "fonts/Domine-Regular.ttf",
        boldItalic = "fonts/Domine-Bold.ttf"
    ),
    ReaderFont.LATO to FontWeightFiles(
        regular = "fonts/Lato-Regular.ttf",
        bold = "fonts/Lato-Bold.ttf",
        italic = "fonts/Lato-Italic.ttf",
        boldItalic = "fonts/Lato-BoldItalic.ttf"
    )
)
