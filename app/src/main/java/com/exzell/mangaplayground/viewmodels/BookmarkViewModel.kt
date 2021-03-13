package com.exzell.mangaplayground.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exzell.mangaplayground.R
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.io.database.*
import com.exzell.mangaplayground.models.Chapter
import com.exzell.mangaplayground.models.Manga
import com.exzell.mangaplayground.utils.reset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.Consumer
import javax.inject.Inject

class BookmarkViewModel(application: Application) : AndroidViewModel(application) {
    @JvmField
    @Inject
    var mRepo: Repository? = null
    private val mContext = application.applicationContext
    private val mMangas = mutableListOf<DBManga>()


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
            }
        }
    }

    fun getTimes(consumer: Consumer<Map<Long, List<DBManga>>>) {
        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                mRepo!!.allMangaTime().map { timestamps ->
                    val times = timestamps.map { Calendar.getInstance().reset(it).timeInMillis }.distinct()

                    //resetting the mangas time with the calendar extension should also work
                    mRepo!!.lastReadMangas(times.minOrNull()!!).map { it.createManga() }.groupBy {
//                            times.find { time -> it.lastReadTime >= time }!!
                        Calendar.getInstance().reset(it.lastReadTime).timeInMillis
                    }
                }
            }.collect {
                consumer.accept(it)
            }
        }
    }

    fun getDayTitle(day: Int): String = when {
        day == 0 -> mContext.getString(R.string.date_today)
        day == 1 -> mContext.getString(R.string.date_yesterday)
        day < 30 -> {
            day.toString() + " " + mContext.getString(R.string.date_days_ago)
        }
        else -> {
            val todayExact = Calendar.getInstance().reset(null)
            todayExact.add(Calendar.DAY_OF_MONTH, day * -1)
            SimpleDateFormat.getDateInstance().format(todayExact.time)
        }
    }

    fun deleteBookmarks(mangas: List<Manga>) {
        val bookmarkInfos = mangas.map { it.toBookmarkInfo() }
        bookmarkInfos.forEach { info: BookmarkInfo -> info.bookmark = false }

        mRepo!!.updateManga(*bookmarkInfos.toTypedArray())
    }

    fun removeFromHistory(lastChapter: Chapter) {
//        lastChapter.lastReadTime = 0
        mRepo!!.updateChapterTime(listOf(ChapterTimeUpdate(lastChapter.id, 0)))
    }

}