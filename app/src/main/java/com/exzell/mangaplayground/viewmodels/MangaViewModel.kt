package com.exzell.mangaplayground.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.exzell.mangaplayground.download.Download
import com.exzell.mangaplayground.download.DownloadManager
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.models.Chapter
import com.exzell.mangaplayground.models.Manga
import com.exzell.mangaplayground.utils.isConnectedToNetwork
import com.exzell.mangaplayground.utils.toManga
import com.exzell.mangaplayground.utils.transferInfo
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MangaViewModel(application: Application, private val mLink: String) : AndroidViewModel(application) {

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
    fun updateManga(onComplete: (Boolean) -> Unit): Disposable? {
        if (!mContext.isConnectedToNetwork()) {
            onComplete(false)
            return null
        }

        val onNext = { manga: Manga ->
            mManga?.transferInfo(manga)
            mManga = manga
        }

        return mRepo.moveTo(mLink)
                .doOnError { onComplete(false) }
                .toManga(mLink)
                .doOnNext(onNext)
                .doOnComplete {
                    onComplete(true)
                    updateDB(mManga!!)
                    isDoneFetching = true
                }
                .subscribe()
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
        mDownloadManager.startDownloadService()
    }

    fun updateDB(manga: Manga) {
        mRepo.insertManga(manga)
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
