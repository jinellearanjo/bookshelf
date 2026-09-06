package com.jinelle.bookshelf.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per imported book. We store the content:// URI (with a persisted permission
 * grant taken in MainActivity) rather than copying the file into app storage --
 * keeps storage footprint to metadata only, per Jinelle's storage question earlier.
 */
@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String,
    val title: String,
    val author: String?,
    val coverImagePath: String?,   // cached cover extracted to app cache dir, or null
    val addedAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long? = null
)
