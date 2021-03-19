package com.exzell.mangaplayground.utils

import com.exzell.mangaplayground.models.Manga
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import okhttp3.ResponseBody
import org.jsoup.Jsoup

fun Observable<ResponseBody>.toManga(link: String): Observable<Manga> {
    return map {
        val mangaDoc = Jsoup.parse(it.string())
        it.close()

        val manga = Manga(link)
        mangaDoc.body().createManga(manga)
        mangaDoc.body().createChapterWithObservable(manga)

        manga

    }.observeOn(AndroidSchedulers.mainThread())
}