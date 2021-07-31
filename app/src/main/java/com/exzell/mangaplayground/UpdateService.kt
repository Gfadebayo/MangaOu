package com.exzell.mangaplayground

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.exzell.mangaplayground.fragment.MangaFragment
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.models.Chapter
import com.exzell.mangaplayground.models.Manga
import com.exzell.mangaplayground.notification.Notifications
import com.exzell.mangaplayground.utils.createNavPendingIntent
import com.exzell.mangaplayground.utils.toManga
import com.exzell.mangaplayground.utils.transferInfo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

/**
 * A Class that is primarily used for updating the mangas the user has bookmarked
 * but it can also be used to add mangas to the DB primarily searched mangas or mangas selected
 * from popular upates and the likes
 */
class UpdateService : Service() {

    companion object {
        const val UPDATE_MANGAS = "specific mangas"

        private const val GROUP = "updates group"

        private const val GROUP_ID = -100
    }

    @Inject
    lateinit var mRepo: Repository

    /**The updates to be performed**/
    var mUpdates: ArrayList<Manga> = ArrayList()

    /**The already completed updates regardless of whether a new chapter was found**/
    private var mCompleted: ArrayList<Manga> = ArrayList()

    /** The amount of manga for every new chapter with a different manga found**/
    private var mNewMangaFound = 0

    private val mDisposer = CompositeDisposable()

    private val mIconWidth: Int by lazy { resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width) }
    private val mIconHeight: Int by lazy { resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height) }

    private val mNotification: NotificationCompat.Builder by lazy {
        NotificationCompat.Builder(this, Notifications.BOOKMARK_ID)
                .setContentTitle(getString(R.string.updating_mangas))
                .setSmallIcon(R.drawable.ic_round_update_24)
    }

    private val mSummaryNotification: NotificationCompat.Builder by lazy {
        NotificationCompat.Builder(this, Notifications.BOOKMARK_NEW_CHAPTER_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setGroup(GROUP)
                .setGroupSummary(true)
    }


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        (application as MangaApplication).mAppComponent.injectRepo(this)
        startForeground(Notifications.BOOKMARK_NOTIFY_ID, NotificationCompat.Builder(this, Notifications.BOOKMARK_ID).build())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mUpdates.apply {
            addAll(
                    if (intent.hasExtra(UPDATE_MANGAS)) mRepo.getMangaWithIds((intent.getLongArrayExtra(UPDATE_MANGAS))!!.toMutableList())
                    else mRepo.getBookmarkedMangaNotLive()
            )

            distinct()
            removeAll(mCompleted)
        }

        mDisposer.add(Observable.fromIterable(mUpdates)
                .filter { it.status.contains("ongoing", true) }
                .flatMap({ createObservable(it) }, 1)
                .observeOn(Schedulers.computation())
                .doOnNext {
                    mUpdates.remove(it.first)
                    mCompleted.add(it.first)


                    mRepo.updateManga(true, it.first)

                    /*Post notification of a new chapter found. A new chapter is found explicitly instead of
                    relying on the DB to inform us that a new chapter row has been inserted due to the fact that
                    some existing chapters can be found and the conflict strategy makes it the new (existing) chapter will
                    replace the one already present giving a false positive*/
                    if (it.second != null) {
                        mNewMangaFound++

                        NotificationManagerCompat.from(this).apply {
                            notify(it.first.id.toInt(), newChapterNotification(it.first, it.second!!).build())

                            if (mCompleted.size > 1) notify(GROUP_ID, mSummaryNotification.setContentTitle("$mNewMangaFound Mangas updated").build())
                        }
                    }
                }
                .doOnComplete { stopService(intent) }.subscribe())

        return super.onStartCommand(intent, flags, startId)
    }

    private fun createObservable(man: Manga): Observable<Pair<Manga, Chapter?>> {

        return mRepo.moveTo(man.link)
                .doOnNext {
                    //The notification is here instead of in a doOnNext before the flatmap is called,
                    //every item is emitted giving the wrong behaviour. so here is simply the next best place
                    mNotification.setContentText(man.title)
                    mNotification.setProgress(mUpdates.size + mCompleted.size, mCompleted.size, false)
                    NotificationManagerCompat.from(this).notify(Notifications.BOOKMARK_NOTIFY_ID, mNotification.build())
                }
                .toManga(man.link)
                .map {
                    val newChap = transferUserInfo(man, it)
                    Pair(it, newChap)
                }.onErrorComplete()
    }

    private fun newChapterNotification(manga: Manga, newChapter: Chapter): NotificationCompat.Builder {

        val cover = Glide.with(this)
                .asBitmap()
                .load(manga.thumbnailLink)
                .onlyRetrieveFromCache(true)
                .dontTransform()
                .centerCrop()
                .circleCrop()
                .override(mIconWidth, mIconHeight)
                .submit()
                .get()

        return NotificationCompat.Builder(this, Notifications.BOOKMARK_NEW_CHAPTER_ID)
                .setContentTitle(manga.title)
                .setContentText(newChapter.numberString)
                .setSmallIcon(R.drawable.ic_round_new_releases_24)
                .setGroup(GROUP)
                .setLargeIcon(cover)
                .setAutoCancel(true)
                .setContentIntent(createNavPendingIntent(R.id.frag_manga, Bundle().apply {
                    putString(MangaFragment.MANGA_LINK, manga.link)
                    putBoolean(MangaFragment.AUTO_UPDATE, false)
                }))
    }

    private fun transferUserInfo(old: Manga, new: Manga): Chapter? {
        old.transferInfo(new)

        return new.chapters.find {
            it.isNewChap && !old.chapters.contains(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        mDisposer.clear()
    }
}