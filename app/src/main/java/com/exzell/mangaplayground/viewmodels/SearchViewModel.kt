package com.exzell.mangaplayground.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.exzell.mangaplayground.advancedsearch.Genre
import com.exzell.mangaplayground.advancedsearch.MangaSearch
import com.exzell.mangaplayground.advancedsearch.Order
import com.exzell.mangaplayground.advancedsearch.Type
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.models.Manga
import com.exzell.mangaplayground.utils.createSearchManga
import com.exzell.mangaplayground.utils.isConnectedToNetwork
import com.exzell.mangaplayground.utils.toManga
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.jsoup.Jsoup
import java.util.*
import java.util.function.Consumer
import javax.inject.Inject
import kotlin.collections.HashMap

@SuppressLint("staticFieldLeak")
class SearchViewModel(application: Application, private val mHandle: SavedStateHandle) : DisposableViewModel(application) {

    companion object {
        const val ERROR_NO_RESULT = "error no result"
        const val ERROR_OTHERS = "error others"
    }

    private val mContext = application.applicationContext

    lateinit var mSearch: MangaSearch

    @Inject
    lateinit var mRepo: Repository

    private val KEY_LINK = "last search link"

    @JvmField
    val mCurrentSearchResults = ArrayList<Manga>()

    val nextLink: Map<String, String>?
        get() = mHandle.get<Map<String, String>>(KEY_LINK)

    var rating: Int
        get() = mSearch.rating
        set(rating) {
            mSearch.rating = rating
        }

    var status: String?
        get() = mSearch.status
        set(status) {
            mSearch.status = status
        }

    var type: String?
        get() = mSearch.type.dispName
        set(type) {
            mSearch.type = Type.values().find { it.dispName == type }
        }

    var chapters: Int
        get() = mSearch.chapterAmount
        set(chap) {
            mSearch.chapterAmount = chap
        }

    var release: Int
        get() = mSearch.release
        set(release) {
            mSearch.release = release
        }

    var genreInclusion: String?
        get() = mSearch.genreInclusion
        set(incl) {
            mSearch.genreInclusion = incl
        }

    var order: String?
        get() = mSearch.order.dispName
        set(order) {
            mSearch.order = Order.values().find { it.dispName == order }
        }

    var authorContain: String
        get() = mSearch.authorContain
        set(contain) {
            mSearch.authorContain = contain
        }

    var titleContain: String
        get() = mSearch.titleContain
        set(contain) {
            mSearch.titleContain = contain
        }

    var title: String
        get() = mSearch.title
        set(title) {
            mSearch.title = title
        }

    var author: String
        get() = mSearch.author
        set(author) {
            mSearch.author = author
        }

    var onSuccess: Consumer<List<Manga>>? = null

    var onError: Consumer<String>? = null

    /**
     * Collects a map of search parameters to their values which can be created through [.search]
     * and makes the request as well as creating the mangas
     */
    fun resolveSearch(search: Map<String, String>) {
        if (!mContext.isConnectedToNetwork()) {
            onError?.accept(ERROR_OTHERS)
            return
        }

        addDisposable(mRepo.advancedSearch(search)?.map {
            val html = Jsoup.parse(it.string()).body()
            it.close()

            val hasNoMatch = html.select("div[class=manga-list").hasClass("no-match")
            if (hasNoMatch) throw RuntimeException(ERROR_NO_RESULT)

            val next = html.getElementsContainingText("next▶").attr("href")

            if (next.isNotEmpty()) setNextSearchLink(next, search)
            else setNextSearchLink(null, search)

            html.createSearchManga(mCurrentSearchResults.size)
        }?.observeOn(AndroidSchedulers.mainThread())?.subscribe({
            mCurrentSearchResults.addAll(it)
            onSuccess?.accept(it)

        }, {
            onError?.accept(if (it.message.equals(ERROR_NO_RESULT)) ERROR_NO_RESULT else ERROR_OTHERS)

        })) ?: onError?.accept(ERROR_OTHERS)
    }

    fun clearSearchResults() {
        mCurrentSearchResults.clear()
    }

    private fun setNextSearchLink(nextLink: String?, currentQuery: Map<String, String>) {
        if (nextLink == null) mHandle.set(KEY_LINK, null)
        else {
            val nextQuery = HashMap(currentQuery);
            nextQuery["page"] = getDigits(nextLink)

            mHandle.set(KEY_LINK, nextQuery)
        }
    }

    private fun getDigits(string: String): String {
        val num = StringBuilder()
        for (c in string.toCharArray()) {
            if (Character.isDigit(c)) num.append(c)
        }

        return num.toString()
    }

    fun getGenres(): List<String> {
        return mSearch.genre.map { it.dispName }
    }

    fun setGenres(genre: String, isAdded: Boolean) {
        val gen = Genre.values().find { it.dispName == genre }
        if (isAdded) mSearch.addGenre(gen)
        else mSearch.genre.remove(gen)
    }

    fun resetValues() {
        mSearch = MangaSearch()
        mHandle.keys().forEach { mHandle[it] = null }
    }

    fun search(): Map<String, String> {
        return mSearch.searchQuery()
    }

    fun createAndBookmarkManga(mangaLinks: ArrayList<String>, onError: () -> Unit) {
        Observable.fromIterable(mangaLinks).flatMap {
            mRepo.moveTo(it)
                    .subscribeOn(Schedulers.io())
                    .toManga(it)
                    .onErrorComplete {
                        onError.invoke()
                        true
                    }.doOnNext { it.isBookmark = true }
        }
                .toList()
                .subscribe { mangas -> mRepo.insertManga(*mangas.toTypedArray()) }
    }

    fun saveSearchParams() {
        val query = mSearch.searchQuery()
        query.keys.forEach {
            mHandle.set(it, query[it])
        }
    }

    fun createSearchFromHandle() {
        val lastQuery = hashMapOf<String, String>()
        mHandle.keys().forEach { if (!it.equals(KEY_LINK)) lastQuery[it] = mHandle[it]!! }

        mSearch = MangaSearch.from(lastQuery)
    }

    override fun onCleared() {
        super.onCleared()
        mCurrentSearchResults.clear()
    }
}
