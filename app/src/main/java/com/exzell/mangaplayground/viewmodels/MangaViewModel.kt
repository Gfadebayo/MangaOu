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
import com.exzell.mangaplayground.utils.ChapterUtils
import com.exzell.mangaplayground.utils.MangaUtils
import com.exzell.mangaplayground.utils.isConnectedToNetwork
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

class MangaViewModel(application: Application) : AndroidViewModel(application) {

    private val mContext: Context = application.applicationContext

    @Inject lateinit var mRepo: Repository

    @Inject lateinit var mDownloadManager: DownloadManager

    fun getDbManga(link: String): Manga? {
        return mRepo.getMangaWithLink(link)
    }

    fun fetchMangaInfo(mangaLink: String, onNext: Consumer<Manga>, onComplete: Action): Disposable? {
        if (!mContext.isConnectedToNetwork()) {
            try {
                onComplete.run()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }

            return null
        }

        return mRepo.moveTo(mangaLink).subscribeOn(Schedulers.io())
                .map {
                    val mangaDoc = Jsoup.parse(it.string())
                    it.close()

                    val manga = Manga(mangaLink)
                    MangaUtils.addMangaDetails(mangaDoc, manga)
                    ChapterUtils.createChapterWithObservable(mangaDoc, manga)

                    manga

                }.observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(onComplete)
                .onErrorComplete()
                .subscribe(onNext)
    }

    fun bookmarkManga(manga: Manga) { mRepo.updateManga(false, manga) }

    fun updateChapter(chapter: List<Chapter>) { mRepo.updateChapters(chapter) }

    fun queueDownloads(downs: List<Download>) {
        mRepo.insertDownloads(downs)
        mDownloadManager.startDownloadService()
    }

    fun updateDB(manga: Manga) { mRepo.insertManga(manga) }

    fun getDownloads(o : LifecycleOwner, consumer: java.util.function.Consumer<List<Download>>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                mRepo.getDownloads()
            }.collect {
                consumer.accept(it)
            }
        }
    }
}
