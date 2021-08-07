package com.exzell.mangaplayground.viewmodels

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.exzell.mangaplayground.advancedsearch.MangaSearch
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.models.Manga
import com.exzell.mangaplayground.utils.createSearchManga
import com.exzell.mangaplayground.utils.toManga
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import javax.inject.Inject

class SearchViewModel(application: Application, private val mHandle: SavedStateHandle) : AndroidViewModel(application) {

    private val mContext = application.applicationContext

    @JvmField
    var mSearch: MangaSearch = MangaSearch.Builder().build()

    @Inject
    lateinit var mRepo: Repository

    //KEYS
    private val KEY_TITLE = "title"
    private val KEY_AUTHOR = "auth/art"
    private val KEY_TITLE_CONTAIN = "title_extra"
    private val KEY_AUTHOR_CONTAIN = "auth/art_extra"
    private val KEY_GENRE = "genres"
    private val KEY_RATING = "getRating"
    private val KEY_STATUS = "status"
    private val KEY_TYPE = "type"
    private val KEY_CHAPTERS = "chapters"
    private val KEY_RELEASE = "releases"
    private val KEY_GENRE_INCL = "genre inclusion"
    private val KEY_ORDER = "order"

    private val KEY_LINK = "last search link"

    @JvmField
    val mCurrentSearchResults = ArrayList<Manga>()

    val nextLink: Map<String, String>?
        get() = mHandle.get<Map<String, String>>(KEY_LINK)

    val selectedGenres: MutableList<String>?
        get() = mHandle.get(KEY_GENRE)

    var rating: Int
        get() = mHandle.get<Int>(KEY_RATING)!!
        set(rating) = mHandle.set(KEY_RATING, rating)

    var status: String?
        get() = mHandle.get<String>(KEY_STATUS)
        set(status) = mHandle.set(KEY_STATUS, status)

    var type: String?
        get() = mHandle.get<String>(KEY_TYPE)
        set(type) = mHandle.set(KEY_TYPE, type)

    var chapters: Int
        get() = mHandle.get<Int>(KEY_CHAPTERS)!!
        set(chap) = mHandle.set(KEY_CHAPTERS, chap)

    var release: Int
        get() = mHandle.get<Int>(KEY_RELEASE)!!
        set(release) = mHandle.set(KEY_RELEASE, release)

    var genreInclusion: String?
        get() = mHandle.get<String>(KEY_GENRE_INCL)
        set(incl) = mHandle.set(KEY_GENRE_INCL, incl)

    var order: String?
        get() = mHandle.get<String>(KEY_ORDER)
        set(order) = mHandle.set(KEY_ORDER, order)

    /**
     * Collects a map of search parameters to their values which can be created through [.search]
     * and makes the request as well as creating the mangas
     * The consumer is called on the same thread the request was made
     * @param onMangaRetrieved A consumer called when the mangas have been created
     */
    fun resolveSearch(search: Map<String, String>, onMangaRetrieved: Consumer<List<Manga>>) {

        mRepo.advancedSearch(search)!!.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val html: Element
                    try {
                        html = Jsoup.parse(response.body()!!.string()).body()
                        response.body()!!.close()

                        val next = html.getElementsContainingText("next▶").attr("href")

                        if (!next.isEmpty()) {

                            val nextLink = HashMap(search)
                            nextLink["page"] = getDigits(next)
                            setNextSearchLink(nextLink)
                        } else
                            setNextSearchLink(null)

                        val searchManga = html.createSearchManga(mCurrentSearchResults.size)
                        mCurrentSearchResults.addAll(searchManga)
                        onMangaRetrieved.accept(searchManga)
                        Timber.w(next)

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                } else {
                    Toast.makeText(mContext, "Error Code: " + response.code(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(mContext, "Failed: " + t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun clearSearchResults() {
        mCurrentSearchResults.clear()
    }

    fun setNextSearchLink(nextLink: Map<String, String>?) {
        mHandle.set(KEY_LINK, nextLink)
    }

    private fun getDigits(string: String): String {
        val num = StringBuilder()
        for (c in string.toCharArray()) {
            if (Character.isDigit(c)) num.append(c)
        }

        return num.toString()
    }

    fun getName(which: Boolean): String? {
        return if (which) mHandle.get<String>(KEY_AUTHOR) else mHandle.get<String>(KEY_TITLE)
    }

    fun containValue(which: Boolean): String? {
        return if (which) mHandle.get<String>(KEY_AUTHOR_CONTAIN) else mHandle.get<String>(KEY_TITLE_CONTAIN)
    }

    fun setGenres(genre: String, which: Boolean) {
        val arr = mHandle.get<ArrayList<String>>(KEY_GENRE)

        if (which) arr!!.add(genre)
        else arr!!.removeAt(arr.indexOf(genre))
    }

    fun setName(which: Boolean, value: String) {
        val key = if (which) KEY_AUTHOR else KEY_TITLE
        mHandle.set(key, value)
    }

    fun setContainValue(which: Boolean, value: String) {
        val key = if (which) KEY_AUTHOR_CONTAIN else KEY_TITLE_CONTAIN
        mHandle.set(key, value)
    }

    fun resetValues() {
        setName(false, "")
        setName(true, "")
        setContainValue(false, "")
        setContainValue(true, "")
        chapters = -1
        rating = -1
        release = -1
        status = ""
        type = ""
        genreInclusion = ""
        selectedGenres!!.clear()
    }

    fun handlerDefaults() {
        with(mHandle) {
            if (!contains(KEY_GENRE)) set(KEY_GENRE, ArrayList<String>())
            if (!contains(KEY_TITLE)) set(KEY_TITLE, "")
            if (!contains(KEY_AUTHOR)) set(KEY_AUTHOR, "")
            if (!contains(KEY_TITLE_CONTAIN)) set(KEY_TITLE_CONTAIN, "")
            if (!contains(KEY_AUTHOR_CONTAIN)) set(KEY_AUTHOR_CONTAIN, "")
            if (!contains(KEY_RATING)) set(KEY_RATING, -1)
            if (!contains(KEY_STATUS)) set(KEY_STATUS, "")
            if (!contains(KEY_TYPE)) set(KEY_TYPE, "")
            if (!contains(KEY_CHAPTERS)) set(KEY_CHAPTERS, -1)
            if (!contains(KEY_RELEASE)) set(KEY_RELEASE, -1)
            if (!contains(KEY_GENRE_INCL)) set(KEY_GENRE_INCL, "")
            if (!contains(KEY_ORDER)) set(KEY_ORDER, "")
        }
    }

    fun search(): Map<String, String> {
        val search = MangaSearch.Builder().setAuthor(getName(true)!!)
                .setAuthorContain(containValue(true)!!)
                .setTitle(getName(false)!!)
                .setTitleContain(containValue(false)!!)
                .setChapterAmount(chapters)
                .setRating(rating)
                .setRelease(release)
                .setStatus(status!!)
                .setType(type)
                .addGenres(selectedGenres!!)
                .setGenreInclusion(genreInclusion!!)
                .setOrder(order)
                .build().apply { mSearch = this }

        //        if(search.searchQuery().isEmpty()) return;
        return search.searchQuery()
    }

    override fun onCleared() {
        super.onCleared()
        mCurrentSearchResults.clear()
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
}
