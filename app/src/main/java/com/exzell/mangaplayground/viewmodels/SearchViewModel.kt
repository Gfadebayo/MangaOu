package com.exzell.mangaplayground.viewmodels

import android.app.Application
import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle

import com.exzell.mangaplayground.adapters.MangaListAdapter
import com.exzell.mangaplayground.advancedsearch.MangaSearch
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.models.Manga
import com.exzell.mangaplayground.utils.MangaUtils

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.stream.Stream

import javax.inject.Inject

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchViewModel(application: Application, private val mHandle: SavedStateHandle) : AndroidViewModel(application) {

    private val TAG = "SearchViewModel"
    private val mContext = application.applicationContext

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

    private val mCurrentSearchResults = ArrayList<Manga>()

    val currentSearchResults: List<Manga>
        get() = mCurrentSearchResults

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

                        val searchManga = MangaUtils.createSearchManga(html)
                        mCurrentSearchResults.addAll(searchManga)
                        onMangaRetrieved.accept(searchManga)
                        Log.w(TAG, next)

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
        if (which)
            arr!!.add(genre)
        else {
            arr!!.removeAt(arr.indexOf(genre))
        }
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
        if (!mHandle.contains(KEY_GENRE)) mHandle.set(KEY_GENRE, ArrayList<String>())
        if (!mHandle.contains(KEY_TITLE)) mHandle.set(KEY_TITLE, "")
        if (!mHandle.contains(KEY_AUTHOR)) mHandle.set(KEY_AUTHOR, "")
        if (!mHandle.contains(KEY_TITLE_CONTAIN)) mHandle.set(KEY_TITLE_CONTAIN, "")
        if (!mHandle.contains(KEY_AUTHOR_CONTAIN)) mHandle.set(KEY_AUTHOR_CONTAIN, "")
        if (!mHandle.contains(KEY_RATING)) mHandle.set(KEY_RATING, -1)
        if (!mHandle.contains(KEY_STATUS)) mHandle.set(KEY_STATUS, "")
        if (!mHandle.contains(KEY_TYPE)) mHandle.set(KEY_TYPE, "")
        if (!mHandle.contains(KEY_CHAPTERS)) mHandle.set(KEY_CHAPTERS, -1)
        if (!mHandle.contains(KEY_RELEASE)) mHandle.set(KEY_RELEASE, -1)
        if (!mHandle.contains(KEY_GENRE_INCL)) mHandle.set(KEY_GENRE_INCL, "")
        if (!mHandle.contains(KEY_ORDER)) mHandle.set(KEY_ORDER, "")
    }

    fun search(): Map<String, String> {
        val search = MangaSearch.Builder().setAuthor(getName(true)!!)
                .setAuthorContain(containValue(true)!!)
                .setTitle(getName(false)!!)
                .setTitleContain(containValue(false)!!)
                .setChapters(chapters)
                .setRating(rating)
                .setRelease(release)
                .setStatus(status!!)
                .setType(type)
                .addGenres(selectedGenres!!)
                .setGenreInclusion(genreInclusion!!)
                .setOrder(order)
                .build()

        //        if(search.searchQuery().isEmpty()) return;
        return search.searchQuery()
    }

    override fun onCleared() {
        super.onCleared()
        mCurrentSearchResults.clear()
    }

    companion object {

        @JvmField val statusData = Stream.of(MangaSearch.STATUS_COMPLETED, MangaSearch.STATUS_ONGOING).map{ it.toUpperCase() }.collect(Collectors.toList())
        @JvmField val chapterData = Stream.of(1, 5, 10, 20, 30, 40, 50, 100, 200).collect(Collectors.toList())
        @JvmField val releaseData = IntStream.rangeClosed(1946, 2017).boxed().map { it.toString() }.collect(Collectors.toList())
    }
}
