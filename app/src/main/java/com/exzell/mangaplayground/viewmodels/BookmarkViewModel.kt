package com.exzell.mangaplayground.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exzell.mangaplayground.R
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.io.database.DBManga
import com.exzell.mangaplayground.models.Chapter
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

    /**
     * Bookmarks gives either the bookmarked(true) mangas or downloaded(false) mangas.
     */
    fun getBookmarks(forBookmarks: Boolean, consumer: Consumer<List<DBManga>>) {
        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                if (forBookmarks) mRepo!!.getBookmarkedManga()
                else mRepo!!.getDownloadedMangas()
            }.collect {
                consumer.accept(it)
            }
        }
    }

    fun getTimes(consumer: Consumer<Map<Long, List<DBManga>>>) {
        viewModelScope.launch {

            launch(Dispatchers.IO) {
                mRepo!!.allMangaTime().map { timestamps ->
                    val times = timestamps.map { Calendar.getInstance().reset(it).timeInMillis }.distinct()

                    //resetting the mangas time with the calendar extension should also work
                    getHistoryManga(times.minOrNull()!!).groupBy {
//                            times.find { time -> it.lastReadTime >= time }!!
                        Calendar.getInstance().reset(it.lastReadTime).timeInMillis
                    }
                }.collect {
                    consumer.accept(it)
                }
            }
        }
    }

    fun getDayTitle(day: Int): String =
            when {
                day == 0 -> mContext.getString(R.string.today)
                day == 1 -> mContext.getString(R.string.yesterday)
                day < 30 -> {
                    day.toString() + " " + mContext.getString(R.string.days_ago)
                }
                else -> {
                    val todayExact = Calendar.getInstance().reset(null)
                    todayExact.add(Calendar.DAY_OF_MONTH, day * -1)
                    SimpleDateFormat.getDateInstance().format(todayExact.time)
                }
            }

    fun deleteBookmarks(mangas: List<DBManga>) {
        mangas.forEach { m: DBManga -> m.isBookmark = false }
        mRepo!!.updateManga(false, *mangas.toTypedArray())
    }

    fun getHistoryManga(time: Long): List<DBManga> {
        return mRepo!!.lastReadMangas(time)
    }

    fun removeFromHistory(lastChapter: Chapter) {
        lastChapter.lastReadTime = 0
        mRepo!!.updateChapters(listOf(lastChapter))
    }

}