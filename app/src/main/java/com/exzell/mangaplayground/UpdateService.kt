package com.exzell.mangaplayground

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.io.database.DBManga
import com.exzell.mangaplayground.models.Manga
import com.exzell.mangaplayground.notification.Notifications
import com.exzell.mangaplayground.utils.ChapterUtils
import com.exzell.mangaplayground.utils.MangaUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.jsoup.Jsoup

class UpdateService: Service() {

    private val mRepo: Repository by lazy { Repository.getInstance(application) }

    var mUpdates: ArrayList<Manga> = ArrayList()

    private var mCompleted: ArrayList<Manga> = ArrayList()

    private val mDisposer = CompositeDisposable()

    private val mNotification: NotificationCompat.Builder by lazy { NotificationCompat.Builder(this, Notifications.BOOKMARK_ID)
            .setContentTitle(getString(R.string.updating_manga))
            .setSmallIcon(R.drawable.ic_update_black_24dp)}


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(Notifications.BOOKMARK_NOTIFY_ID, NotificationCompat.Builder(this, Notifications.BOOKMARK_ID).build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mUpdates.addAll((mRepo.bookmarkedMangaNotLive))
        mUpdates.removeAll(mCompleted)

        mDisposer.add(Observable.fromIterable(mUpdates).subscribeOn(Schedulers.io())
                .flatMap { createObservable(it) }
                .doOnNext {
                    mNotification.setContentText(it.title)
                    mNotification.setProgress(mUpdates.size + mCompleted.size, mCompleted.size, false)
                    NotificationManagerCompat.from(this).notify(Notifications.BOOKMARK_NOTIFY_ID, mNotification.build())

                    mRepo.updateManga(it)
                    mUpdates.remove(it)
                    mCompleted.add(it)
                }
                .doOnComplete { stopService(intent) }.subscribe())

        return super.onStartCommand(intent, flags, startId)
    }

    private fun createObservable(man: Manga): Observable<Manga>{
        return mRepo.moveTo(man.link).map {
            val mangaDoc = Jsoup.parse(it.string())
            it.close()

            val manga = Manga(man.link)
            MangaUtils.addMangaDetails(mangaDoc, manga)
            ChapterUtils.createChapterWithObservable(mangaDoc, manga)

            transferUserInfo(man, manga)
            manga
        }
    }

    private fun transferUserInfo(old: Manga, new: Manga){
        new.isBookmark = old.isBookmark

        new.chapters = ChapterUtils.transferChapterInfo(new.chapters, old.chapters)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        mDisposer.clear()
    }
}