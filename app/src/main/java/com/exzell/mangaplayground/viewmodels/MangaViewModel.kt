package com.exzell.mangaplayground.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.exzell.mangaplayground.download.DownloadManager
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.models.Chapter
import com.exzell.mangaplayground.models.Download
import com.exzell.mangaplayground.models.Manga
import com.exzell.mangaplayground.utils.isConnectedToNetwork
import com.exzell.mangaplayground.utils.toManga
import com.exzell.mangaplayground.utils.transferInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class MangaViewModel(application: Application, private val mLink: String) : DisposableViewModel(application) {

    private val mContext: Context = application.applicationContext

    @Inject
    lateinit var mRepo: Repository

    @Inject
    lateinit var mDownloadManager: DownloadManager

    var isDoneFetching = false
        private set

    @get:JvmName("getManga")
    var mManga: Manga? = null
        private set

    fun fetchMangaInfo() {
        mManga = mRepo.getMangaWithLink(mLink)
    }

    /**
     * Updates the manga remotely and calls onComplete passing true when successful
     * or false when there is an error
     */
    fun updateManga(onComplete: (Boolean) -> Unit) {
        if (!mContext.isConnectedToNetwork()) {
            onComplete(false)
            return
        }

        val initiallyNull = mManga == null

        val onNext = { manga: Manga ->
            mManga?.transferInfo(manga)
            mManga = manga
        }

        val onError = { thro: Throwable ->
            onComplete(false)
            Timber.d(thro)
        }

        addDisposable(mRepo.moveTo(mLink)
                .toManga(mLink)
                .doOnNext(onNext)
                .doOnComplete {
                    onComplete(true)
                    updateDB(mManga!!, initiallyNull)
                    isDoneFetching = true
                }
                .subscribe(onNext, onError))
    }

    /** Switches the manga bookmark to the opposite value whenever called **/
    fun alterBookmark(): Boolean = mManga?.let {
        it.isBookmark = !it.isBookmark
        mRepo.updateManga(false, it)

        it.isBookmark
    }!!

    fun updateChapter(chapter: List<Chapter>) {
        mRepo.updateChapters(chapter)
    }

    fun queueDownloads(downs: List<Download>) {
        mRepo.insertDownloads(downs)
        mDownloadManager.prepareToDownload()
    }

    /**
     * Updates the DB, but it first needs to know what exactly to do
     *
     * @param justInsert passed to tell the method whether to just update
     * what was initally there or if it is a new value and it should simply insert it.
     * True means to just insert and False to update
     */
    fun updateDB(manga: Manga, justInsert: Boolean) {
        if (justInsert) mRepo.insertManga(manga)
        else mRepo.updateManga(true, manga)
    }

    fun getDownloads(o: LifecycleOwner, consumer: java.util.function.Consumer<List<Download>>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                mRepo.getDownloads()
            }.collect {
                consumer.accept(it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mManga = null
    }
}
