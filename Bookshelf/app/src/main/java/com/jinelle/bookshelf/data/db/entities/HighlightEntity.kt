package com.jinelle.bookshelf.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A highlight is a note-capable thing, not two separate features: note == null
 * means it's a plain highlight, non-null means highlight + note together.
 *
 * startOffset/endOffset are character offsets into the chapter's plain-text content
 * (see JsBridge). selectedText is stored as a fallback so a highlight can still be
 * re-located by text search if offsets ever drift (e.g. after a chapter re-parse).
 */
@Entity(tableName = "highlights")
data class HighlightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val chapterIndex: Int,
    val startOffset: Int,
    val endOffset: Int,
    val selectedText: String,
    val color: String,          // hex string, e.g. "#FFF59D"
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
