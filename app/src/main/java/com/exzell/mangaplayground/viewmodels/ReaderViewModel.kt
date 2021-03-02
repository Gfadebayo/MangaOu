package com.exzell.mangaplayground.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.io.database.DBManga
import com.exzell.mangaplayground.models.Manga
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.ResponseBody
import timber.log.Timber
import javax.inject.Inject

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    val mContext: Context = application.applicationContext

    @Inject lateinit var mRepo: Repository

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

        val ret: Observable<ResponseBody>? = mRepo.goTo(link)
        if(ret == null) {
            onFailure.invoke()
            return Disposable.empty()
        }

                return ret.subscribeOn(Schedulers.io())
                .map {
                    val mime = it.contentType()!!.type()
                    val sub = it.contentType()!!.subtype()
                    val bytes = it.bytes()
                    it.close()
                    Timber.d("Content Type of the image is $mime / $sub")

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
        return mRepo.getMangaForChapter(id)!!
    }

    fun updateManga(manga: Manga){
        mRepo.updateManga(true, manga)
    }
}