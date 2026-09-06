package com.jinelle.bookshelf

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jinelle.bookshelf.ui.library.LibraryScreen
import com.jinelle.bookshelf.ui.reader.ReaderScreen

private object Routes {
    const val LIBRARY = "library"
    const val READER = "reader/{bookId}/{uri}"
    fun reader(bookId: Long, uri: String) = "reader/$bookId/${Uri.encode(uri)}"
}

@Composable
fun BookshelfNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LIBRARY) {
        composable(Routes.LIBRARY) {
            LibraryScreen(
                onBookClick = { book ->
                    navController.navigate(Routes.reader(book.id, book.uri))
                }
            )
        }
        composable(Routes.READER) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")?.toLongOrNull() ?: return@composable
            val uriString = backStackEntry.arguments?.getString("uri")?.let { Uri.decode(it) } ?: return@composable
            ReaderScreen(
                bookId = bookId,
                bookUri = Uri.parse(uriString),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
