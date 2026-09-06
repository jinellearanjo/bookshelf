package com.jinelle.bookshelf.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jinelle.bookshelf.data.db.entities.BookEntity
import com.jinelle.bookshelf.data.db.entities.HighlightEntity
import com.jinelle.bookshelf.data.db.entities.ProgressEntity

@Database(
    entities = [BookEntity::class, ProgressEntity::class, HighlightEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun progressDao(): ProgressDao
    abstract fun highlightDao(): HighlightDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bookshelf.db"
                ).build().also { instance = it }
            }
    }
}
