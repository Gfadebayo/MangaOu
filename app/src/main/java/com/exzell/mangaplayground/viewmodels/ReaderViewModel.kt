package com.exzell.mangaplayground.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.io.database.DBManga
import com.exzell.mangaplayground.models.Chapter
import com.exzell.mangaplayground.models.Manga
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    val mContext: Context = application.applicationContext
    private val mRepo: Repository = Repository.getInstance(application)

    fun getImageLink(link: String, onNext: (String) -> Unit, onError: () -> Unit): Disposable{
        return (mRepo.moveTo(link).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    val consume: String = it.string()
                    it.close()

                    consume
                }.subscribe(onNext, {
                    it.printStackTrace()
                    onError.invoke()
                }))
    }

    fun getImageBytes(link: String, onSuccess: (ByteArray) -> Unit, onFailure: () -> Unit): Disposable {
        return mRepo.goTo(link).subscribeOn(Schedulers.io())
                .map {
                    val mime = it.contentType()!!.type()
                    val sub = it.contentType()!!.subtype()
                    val bytes = it.bytes()
                    it.close()
                    Log.d("ReaderViewModel", "Content Type of the image is $mime / $sub")

                    bytes
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess, {
                    it.printStackTrace()
                    onFailure.invoke()
                })
    }

    fun getDownloadedPath(chapterId: Long): String{
        return mRepo.getDownloadPath(chapterId) ?: ""
    }

    fun getMangaWithChapterId(id: Long): DBManga{
        return mRepo.getMangaForChapter(id)
    }

    fun updateChapter(lastChapter: Chapter) {
        mRepo.updateChapters(listOf(lastChapter))
    }

    fun today(): Long{
        val calen = Calendar.getInstance().apply {
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return calen.timeInMillis
    }

    fun updateManga(manga: Manga){
        mRepo.updateManga(manga)
    }
}