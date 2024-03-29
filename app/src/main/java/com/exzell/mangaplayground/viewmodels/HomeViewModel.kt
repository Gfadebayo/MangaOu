package com.exzell.mangaplayground.viewmodels

import android.app.Application
import android.content.Context
import android.util.Pair
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.exzell.mangaplayground.R
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.io.database.createManga
import com.exzell.mangaplayground.models.Manga
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import timber.log.Timber
import java.io.IOException
import java.util.*
import java.util.function.BiConsumer
import java.util.stream.Collectors
import javax.inject.Inject
import kotlin.collections.ArrayList

class HomeViewModel(application: Application, private val mHandle: SavedStateHandle) : DisposableViewModel(application) {

    @JvmField
    @Inject
    var mRepo: Repository? = null
    private val mContext: Context = application.applicationContext
    private val KEY_LINK = "next link"
    private val KEY_MANGAS = "cached mangas"


    fun initHandler(startLink: String) {
        if (!mHandle.contains(KEY_LINK)) setNextLink(startLink)
        if (!mHandle.contains(KEY_MANGAS)) mHandle.set(KEY_MANGAS, ArrayList<Manga>())
    }

    fun parseHome(consumer: BiConsumer<List<Manga>, Pair<Int, Boolean>>, popularIndex: Int, latestIndex: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var errorMessage = ""
                val resp = mRepo!!.home()
                if (resp != null)
                    with(resp) {
                        if (isSuccessful) {
                            try {
                                val docu = Jsoup.parse(body()!!.string())
                                body()!!.close()

                                popularUpdates(this@launch, docu, consumer, popularIndex)
                                latestRelease(this@launch, docu, consumer, latestIndex)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                            return@withContext
                        } else errorMessage = mContext.getString(R.string.failed, ": ${resp.code()}")

                    } else errorMessage = mContext.getString(R.string.error_network)

                //should only be reached when a problem occurs
                launch(Dispatchers.Main) {
                    if (errorMessage.isNotEmpty()) Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show()
                    consumer.accept(emptyList(), Pair(popularIndex, false))
                    consumer.accept(emptyList(), Pair(latestIndex, false))
                }
            }
        }
    }

    /**
     * On Analyzing the home html, popular updates
     * are all under a **li** tag with class as used in the code
     * @param docu The Document of the home html
     */
    private suspend fun popularUpdates(scope: CoroutineScope, docu: Document, consumer: BiConsumer<List<Manga>, Pair<Int, Boolean>>, acceptIndex: Int) {
        scope.launch(Dispatchers.Main) {
            consumer.accept(
                    withContext(Dispatchers.Default) {
                        //Gives every <a> tag which is part of popular updates
                        docu.body().getElementsByAttributeValue("class", "thm-effect radius")
                                .map {
                                    val link = it.attr("href")
                                    val title = it.attr("title")
                                    var thumbnail = it.getElementsByTag("img").first().attr("src")
                                    if (!thumbnail.startsWith("https:")) thumbnail = "https:$thumbnail"
                                    val manga = Manga(link)
                                    manga.thumbnailLink = thumbnail
                                    manga.title = title
                                    Timber.i(link)
                                    manga
                                }
                    }, Pair(acceptIndex, true))
        }
    }

    /**
     * Just like @see popularUpdates#, The mangas under this can be found in a <div> tag with
     * class as used below
     * @param docu The Document of the home html
     * @return A list of manga found under Latest Releases
    </div> */
    private suspend fun latestRelease(scope: CoroutineScope, docu: Document, consumer: BiConsumer<List<Manga>, Pair<Int, Boolean>>, acceptIndex: Int) {
        scope.launch(Dispatchers.Main) {
            consumer.accept(
                    withContext(Dispatchers.Default) {
                        val attr = docu.body().getElementsByAttributeValue("class", "bd ls1")
                        attr.first().getElementsByClass("cover")
                                .map {
                                    val link = it.attr("href")
                                    val title = it.attr("title")
                                    var thumbnail = it.getElementsByTag("img").attr("src")
                                    if (!thumbnail.startsWith("https:")) thumbnail = "https:$thumbnail"
                                    val manga = Manga(link)
                                    manga.title = title
                                    manga.thumbnailLink = thumbnail
                                    manga
                                }
                    }, Pair(acceptIndex, true))
        }
    }

    fun queryDb(consumer: BiConsumer<List<Manga>, Pair<Int, Boolean>>, bookmarkIndex: Int, downloadIndex: Int) {
        viewModelScope.launch {

            launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) {
                    mRepo!!.getDownloadedMangas().map {
                        it.map { info -> info.createManga() }
                    }
                }.collect {
                    consumer.accept(it.shuffled().take(5), Pair(downloadIndex, it.isNotEmpty()))
                }
            }

            withContext(Dispatchers.IO) {
                mRepo!!.getBookmarkedManga().map {
                    it.map { info -> info.createManga() }
                }
            }.collect {
                consumer.accept(it.shuffled().take(5), Pair(bookmarkIndex, it.isNotEmpty()))
            }
        }
    }

    //Used in empty fragment instead of creating another viewmodel just for it
    fun goToLink(link: String, next: Consumer<List<Manga>?>) {
        addDisposable(mRepo!!.moveTo(link)
                .observeOn(Schedulers.computation())
                .map {
                    val response = it.string()
                    it.close()
                    Timber.i(link)
                    val mangas: MutableList<Manga> = ArrayList()
                    if (response.isNotEmpty()) {
                        val doc = Jsoup.parse(response)
                        if (doc.hasClass("col-12 no-match")) return@map mangas
                        val mangaHtml = doc.select("a[class=cover]")
                        mangas.addAll(mangaHtml.stream().map { m: Element ->
                            val mlink = m.attr("href")
                            val title = m.attr("title")
                            var thumbLink = m.select("img[src*=//file-thumb.mangapark.net/W300/]").attr("src")
                            if (!thumbLink.startsWith("https:")) thumbLink = "https://$thumbLink"
                            val manga = Manga(mlink)
                            manga.title = title
                            manga.thumbnailLink = thumbLink
                            manga
                        }.collect(Collectors.toList()))
                    }
                    mangas
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribe({ mangas: List<Manga>? ->
                    setNextLink(nextLink(link))
                    next.accept(mangas)
                    cacheMangas(mangas ?: emptyList())
                }) { thr: Throwable -> thr.printStackTrace() })
    }

    private fun nextLink(link: String): String {
        val newLink = StringBuilder(link)
        val index = newLink.lastIndexOf("/")
        val num = newLink.substring(index + 1)
        val newNum = if (num.isEmpty() || !Character.isDigit(num[0])) 2 else num.toInt() + 1
        newLink.replace(index + 1, newLink.length, newNum.toString())
        return newLink.toString()
    }

    private fun setNextLink(nextLink: String) {
        mHandle.set(KEY_LINK, nextLink)
    }

    private fun cacheMangas(additions: List<Manga>) {
        mHandle.get<ArrayList<Manga>>(KEY_MANGAS)!!.addAll(additions)
    }

    fun getNextLink(): String? = mHandle.get(KEY_LINK)

    fun getCachedMangas(): List<Manga> = mHandle.get(KEY_MANGAS)!!
}