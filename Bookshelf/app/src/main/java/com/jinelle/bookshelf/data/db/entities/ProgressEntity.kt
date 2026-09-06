package com.jinelle.bookshelf.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per book, tracking exactly where the reader left off.
 * scrollPercent is within the current chapter (0f-1f); combined with chapterIndex
 * this survives font-size/theme changes since it's re-applied after CSS reflow.
 */
@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey val bookId: Long,
    val chapterIndex: Int = 0,
    val scrollPercent: Float = 0f,
    val updatedAt: Long = System.currentTimeMillis()
)
