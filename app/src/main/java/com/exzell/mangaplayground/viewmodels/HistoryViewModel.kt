package com.exzell.mangaplayground.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exzell.mangaplayground.R
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.io.database.ChapterTimeUpdate
import com.exzell.mangaplayground.io.database.DBManga
import com.exzell.mangaplayground.io.database.createManga
import com.exzell.mangaplayground.models.Manga
import com.exzell.mangaplayground.utils.reset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val mContext = application.applicationContext

    @Inject
    lateinit var mRepo: Repository

    var alreadyWatching = false

    private val mMangas: MutableList<DBManga> = mutableListOf()

    var onHistoryChanged: ((Map<Long, List<DBManga>>) -> Unit)? = null

    fun getMangas(): List<DBManga> = mMangas.toList()

    fun getHistory(): Map<Long, List<DBManga>> =
            Collections.unmodifiableMap(mMangas.groupBy { Calendar.getInstance().reset(it.lastReadTime).timeInMillis })

    private fun getTimes() {
        viewModelScope.launch {
            alreadyWatching = true

            withContext(Dispatchers.IO) {
                mRepo!!.allMangaTime().map { timestamps ->
                    val times = timestamps.map { Calendar.getInstance().reset(it).timeInMillis }.distinct()

                    if (times.isEmpty()) emptyMap()
                    else {
                        //resetting the mangas time with the calendar extension should also work
                        mRepo!!.lastReadMangas(times.minOrNull()!!)
                                .map { it.createManga() }
                                .also {
                                    mMangas.clear()
                                    mMangas.addAll(it)
                                }.groupBy {
//            times.find { time -> it.lastReadTime >= time }!!
                                    Calendar.getInstance().reset(it.lastReadTime).timeInMillis
                                }
                    }
                }
            }.collect {
                onHistoryChanged?.invoke(it)
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
            val todayExact = Calendar.getInstance().reset()
            todayExact.add(Calendar.DAY_OF_MONTH, day.times(-1))
            SimpleDateFormat.getDateInstance().format(todayExact.time)
        }
    }

    fun removeFromHistory(manga: Manga) {
//        lastChapter.lastReadTime = 0
        mRepo!!.updateChapterTime(manga.chapters.map { ChapterTimeUpdate(it.id, 0) })
    }

    fun startWatching() {
        if (!alreadyWatching) getTimes()
    }
}