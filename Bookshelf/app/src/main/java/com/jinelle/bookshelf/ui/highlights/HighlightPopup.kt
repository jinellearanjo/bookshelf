package com.jinelle.bookshelf.ui.highlights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.jinelle.bookshelf.domain.model.HighlightColor

/**
 * Shown when the user has an active text selection. A highlight IS a note-capable
 * thing -- one popup, one save action, not two separate highlight/note features.
 */
@Composable
fun HighlightPopup(
    selectedText: String,
    onConfirm: (HighlightColor, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedColor by remember { mutableStateOf(HighlightColor.YELLOW) }
    var noteText by remember { mutableStateOf("") }
    var noteExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Highlight") },
        text = {
            Column {
                Text(
                    text = if (selectedText.length > 120) selectedText.take(120) + "…" else selectedText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HighlightColor.values().forEach { color ->
                        ColorSwatch(
                            color = Color(android.graphics.Color.parseColor(color.hex)),
                            selected = color == selectedColor,
                            onClick = { selectedColor = color }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (!noteExpanded) {
                    TextButton(onClick = { noteExpanded = true }) {
                        Text("Add a note")
                    }
                } else {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Note") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedColor, noteText.ifBlank { null }) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(if (selected) 34.dp else 28.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
    )
}
