package com.exzell.mangaplayground

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.models.Manga
import com.exzell.mangaplayground.notification.Notifications
import com.exzell.mangaplayground.utils.ChapterUtils
import com.exzell.mangaplayground.utils.MangaUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.jsoup.Jsoup
import javax.inject.Inject

/**
 * A Class that is primarily used for updating the mangas the user has bookmarked
 * but it can also be used to add mangas to the DB primarily searched mangas or mangas selected
 * from popular upates and the likes
 */
class UpdateService: Service() {

    companion object{
        const val UPDATE_MANGAS = "specific mangas"
        const val CREATE_MANGAS = "new mangas"
    }

    @Inject lateinit var mRepo: Repository

    var mUpdates: ArrayList<Manga> = ArrayList()

    private var mCompleted: ArrayList<Manga> = ArrayList()

    private val mDisposer = CompositeDisposable()

    private val mNotification: NotificationCompat.Builder by lazy { NotificationCompat.Builder(this, Notifications.BOOKMARK_ID)
            .setContentTitle(getString(R.string.updating_manga))
            .setSmallIcon(R.drawable.ic_update_black_24dp)}


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        (application as MangaApplication).mAppComponent.injectRepo(this)
        startForeground(Notifications.BOOKMARK_NOTIFY_ID, NotificationCompat.Builder(this, Notifications.BOOKMARK_ID).build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when {
            intent?.hasExtra(CREATE_MANGAS) == true -> {
                mUpdates.addAll(intent.getStringArrayListExtra(CREATE_MANGAS)!!.map { Manga(it) })
            }
            intent?.hasExtra(UPDATE_MANGAS) == true -> mUpdates.addAll(mRepo.getMangaWithLinks((intent.getLongArrayExtra(UPDATE_MANGAS))!!.toMutableList()))
            else -> mUpdates.addAll(mRepo.bookmarkedMangaNotLive)
        }
        mUpdates.distinct()
        mUpdates.removeAll(mCompleted)

        /*TODO: Try to fix this using a boolean to check if the service is already running so instead
            we just add to it the same way we add more downloads to existing ones*/

        mDisposer.add(Observable.fromIterable(mUpdates).subscribeOn(Schedulers.io())
                .flatMap { createObservable(it) }
                .doOnNext {
                    mUpdates.remove(it)
                    mCompleted.add(it)

                    if(intent?.hasExtra(CREATE_MANGAS) == true){
                        it.isBookmark = true
                        mRepo.insertManga(it)
                    }
                    else mRepo.updateManga(true, it)

                    if(intent?.hasExtra(CREATE_MANGAS) == false) {
                        mNotification.setContentText(it.title)
                        mNotification.setProgress(mUpdates.size + mCompleted.size, mCompleted.size, false)
                        NotificationManagerCompat.from(this).notify(Notifications.BOOKMARK_NOTIFY_ID, mNotification.build())
                    }
                }
                .doOnComplete { stopService(intent) }.onErrorComplete().subscribe())

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
        new.id = old.id
        new.chapters = ChapterUtils.transferChapterInfo(new.chapters, old.chapters)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        mDisposer.clear()
    }
}