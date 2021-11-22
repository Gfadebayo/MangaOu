package com.exzell.mangaplayground.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exzell.mangaplayground.R
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.io.database.ChapterTimeUpdate
import com.exzell.mangaplayground.io.database.DBManga
import com.exzell.mangaplayground.io.database.createManga
import com.exzell.mangaplayground.io.database.toChapter
import com.exzell.mangaplayground.models.Chapter
import com.exzell.mangaplayground.models.Manga
import com.exzell.mangaplayground.utils.reset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
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

    private val todayInDays = Math.floorDiv(Calendar.getInstance().reset().timeInMillis, (1000 * 60 * 60 * 24).toLong())

    fun getMangas(): List<DBManga> = mMangas.toList()

    fun getHistory(): Map<Long, List<DBManga>> =
            Collections.unmodifiableMap(mMangas.groupBy { Calendar.getInstance().reset(it.lastReadTime).timeInMillis })

    private fun getTimes() {
        viewModelScope.launch {
            alreadyWatching = true

            withContext(Dispatchers.IO) {
                mRepo.allMangaTime().flatMapConcat {
                    val timeReset = Calendar.getInstance().reset(it).timeInMillis

                    //resetting the mangas time with the calendar extension should also work
                    mRepo.lastReadMangas(timeReset).map {
                        it.map { it.createManga() }
                    }
                }.onEach {
                    mMangas.clear()
                    mMangas.addAll(it)
                }.map {
                    it.groupBy { Calendar.getInstance().reset(it.lastReadTime).timeInMillis }
                }
            }.collect {
                onHistoryChanged?.invoke(it)
            }
        }
    }

    fun getDayTitle(timestamp: Long): String {
        val time = Calendar.getInstance().reset(timestamp).timeInMillis
        val dayInDays = time.floorDiv((1000 * 60 * 60 * 24).toLong())
        val day = (todayInDays - dayInDays).toInt()

        return when {

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
    }

    fun getChaptersWithMangaId(mangaId: Long, onComplete: (List<Chapter>) -> Unit) {
        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                mRepo.getReadChaptersForManga(mangaId).map {
                    it.map { it.toChapter() }
                }
            }.collect {
                it.forEach { chap -> Timber.d(chap.title) }
                onComplete.invoke(it)
            }
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