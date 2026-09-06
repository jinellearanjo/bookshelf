package com.jinelle.bookshelf.domain.model

/** Persisted (DataStore) reader configuration -- applies across all books. */
data class ReaderSettings(
    val bgColorHex: String = "#FFFFFF",
    val textColorHex: String = "#000000",
    val font: ReaderFont = ReaderFont.TIMES_NEW_ROMAN,
    val fontSizePx: Int = 18,
    val lineHeight: Float = 1.6f
)

/** Quick-tap starter presets. User can still freely override either color afterward. */
enum class ReaderThemePreset(val label: String, val bg: String, val text: String) {
    WHITE("White", "#FFFFFF", "#000000"),
    SEPIA("Sepia", "#F4ECD8", "#5B4636"),
    GREY("Grey", "#E0E0E0", "#2B2B2B"),
    DARK("Dark", "#1E1E1E", "#D6D6D6"),
    BLACK_OLED("Black", "#000000", "#B3B3B3")
}

/** Fixed set of 4 highlight colors, kept small and consistent like the font list. */
enum class HighlightColor(val hex: String, val label: String) {
    YELLOW("#FFF59D", "Yellow"),
    GREEN("#A5D6A7", "Green"),
    BLUE("#90CAF9", "Blue"),
    PINK("#F48FB1", "Pink")
}
