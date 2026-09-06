package com.jinelle.bookshelf.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jinelle.bookshelf.data.settings.PageMode
import com.jinelle.bookshelf.domain.model.ReaderFont
import com.jinelle.bookshelf.domain.model.ReaderSettings
import com.jinelle.bookshelf.domain.model.ReaderThemePreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsSheet(
    settings: ReaderSettings,
    pageMode: PageMode,
    onSettingsChange: (ReaderSettings) -> Unit,
    onPageModeChange: (PageMode) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text("Page turning", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = pageMode == PageMode.SCROLL,
                    onClick = { onPageModeChange(PageMode.SCROLL) },
                    label = { Text("Scroll") }
                )
                FilterChip(
                    selected = pageMode == PageMode.PAGINATED,
                    onClick = { onPageModeChange(PageMode.PAGINATED) },
                    label = { Text("Paginated") }
                )
            }

            Spacer(Modifier.height(20.dp))
            Text("Theme", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(ReaderThemePreset.values().toList()) { preset ->
                    ThemeSwatch(
                        preset = preset,
                        onClick = {
                            onSettingsChange(settings.copy(bgColorHex = preset.bg, textColorHex = preset.text))
                        }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Custom colors", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            ColorRow(
                label = "Page color",
                hex = settings.bgColorHex,
                onHexChange = { onSettingsChange(settings.copy(bgColorHex = it)) }
            )
            Spacer(Modifier.height(8.dp))
            ColorRow(
                label = "Text color",
                hex = settings.textColorHex,
                onHexChange = { onSettingsChange(settings.copy(textColorHex = it)) }
            )
            if (isLowContrast(settings.bgColorHex, settings.textColorHex)) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "These colors are close in brightness and may be hard to read.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(20.dp))
            Text("Font", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReaderFont.values().forEach { font ->
                    FilterChip(
                        selected = settings.font == font,
                        onClick = { onSettingsChange(settings.copy(font = font)) },
                        label = { Text(font.displayName) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Font size: ${settings.fontSizePx}px", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = settings.fontSizePx.toFloat(),
                onValueChange = { onSettingsChange(settings.copy(fontSizePx = it.toInt())) },
                valueRange = 12f..32f,
                steps = 19
            )
        }
    }
}

@Composable
private fun ThemeSwatch(preset: ReaderThemePreset, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(preset.bg)))
        )
        Spacer(Modifier.height(4.dp))
        Text(preset.label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ColorRow(label: String, hex: String, onHexChange: (String) -> Unit) {
    var text by androidx.compose.runtime.remember(hex) { androidx.compose.runtime.mutableStateOf(hex) }
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray))
        )
        Spacer(Modifier.width(12.dp))
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                if (runCatching { android.graphics.Color.parseColor(it) }.isSuccess) onHexChange(it)
            },
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
    }
}

/** Simple luminance-difference check to warn about a near-unreadable combo. */
private fun isLowContrast(bgHex: String, textHex: String): Boolean {
    return runCatching {
        val bg = android.graphics.Color.parseColor(bgHex)
        val text = android.graphics.Color.parseColor(textHex)
        val bgLum = luminance(bg)
        val textLum = luminance(text)
        kotlin.math.abs(bgLum - textLum) < 0.15
    }.getOrDefault(false)
}

private fun luminance(color: Int): Double {
    val r = android.graphics.Color.red(color) / 255.0
    val g = android.graphics.Color.green(color) / 255.0
    val b = android.graphics.Color.blue(color) / 255.0
    return 0.299 * r + 0.587 * g + 0.114 * b
}
