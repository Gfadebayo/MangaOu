package com.exzell.mangaplayground.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.io.database.BookmarkInfo
import com.exzell.mangaplayground.io.database.createManga
import com.exzell.mangaplayground.io.database.toBookmarkInfo
import com.exzell.mangaplayground.models.Manga
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.function.Consumer
import javax.inject.Inject

class BookmarkViewModel(application: Application) : AndroidViewModel(application) {
    @JvmField
    @Inject
    var mRepo: Repository? = null

    val mMangas = mutableListOf<Manga>()


    /**
     * Bookmarks gives either the bookmarked(true) mangas or downloaded(false) mangas.
     */
    fun getBookmarks(forBookmarks: Boolean, consumer: Consumer<List<Manga>>) {
        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                if (forBookmarks) mRepo!!.getBookmarkedManga().map {
                    it.map { info -> info.createManga() }
                }
                else mRepo!!.getDownloadedMangas().map {
                    it.map { info -> info.createManga() }
                }
            }.collect {
                consumer.accept(it)

                mMangas.clear()
                mMangas.addAll(it)
            }
        }
    }

    fun deleteBookmarks(mangas: List<Manga>) {
        val bookmarkInfos = mangas.map { it.toBookmarkInfo() }
        bookmarkInfos.forEach { info: BookmarkInfo -> info.bookmark = false }

        mRepo!!.updateManga(*bookmarkInfos.toTypedArray())
    }
}