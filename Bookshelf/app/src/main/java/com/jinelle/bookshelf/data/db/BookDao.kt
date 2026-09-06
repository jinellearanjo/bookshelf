package com.jinelle.bookshelf.data.db

import androidx.room.*
import com.jinelle.bookshelf.data.db.entities.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    // Three separate queries (rather than one dynamic ORDER BY) so each is a plain,
    // easily-readable Room query -- fine at library-list scale.
    @Query("SELECT * FROM books ORDER BY title COLLATE NOCASE ASC")
    fun observeByTitle(): Flow<List<BookEntity>>

    // "lastOpenedAt IS NULL" sorts false(0) before true(1), so never-opened books
    // naturally fall after ones with a real timestamp, newest first.
    @Query("SELECT * FROM books ORDER BY lastOpenedAt IS NULL ASC, lastOpenedAt DESC, addedAt DESC")
    fun observeByLastOpened(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books ORDER BY author COLLATE NOCASE ASC, title COLLATE NOCASE ASC")
    fun observeByAuthor(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getById(id: Long): BookEntity?

    @Query("SELECT * FROM books WHERE uri = :uri LIMIT 1")
    suspend fun getByUri(uri: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: BookEntity): Long

    @Update
    suspend fun update(book: BookEntity)

    @Delete
    suspend fun delete(book: BookEntity)

    @Query("UPDATE books SET lastOpenedAt = :timestamp WHERE id = :bookId")
    suspend fun markOpened(bookId: Long, timestamp: Long = System.currentTimeMillis())
}

/** Matches the three sort options exposed on the library screen. */
enum class LibrarySort { TITLE, LAST_OPENED, AUTHOR }
