package com.exzell.mangaplayground.download

import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.bumptech.glide.Glide
import com.exzell.mangaplayground.MainActivity
import com.exzell.mangaplayground.R
import com.exzell.mangaplayground.fragment.BookmarkFragment
import com.exzell.mangaplayground.models.Download
import com.exzell.mangaplayground.models.Manga
import com.exzell.mangaplayground.notification.Notifications
import kotlin.math.roundToInt

class DownloadNotifier(private val context: Context,
                       private val downloadManager: DownloadManager) : DownloadChangeListener {

    var title: String? = null

    val manager: NotificationManagerCompat = NotificationManagerCompat.from(context)

    private val mIconWidth: Int by lazy { context.resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width) }
    private val mIconHeight: Int by lazy { context.resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height) }

    private var progressNotification: NotificationCompat.Builder = NotificationCompat.Builder(context, Notifications.DOWNLOAD_PROGRESS_ID)
            .setContentTitle(context.getString(R.string.downloading_chapters))
            .setSmallIcon(R.drawable.ic_round_download_24)
            .setProgress(MAX_PROGRESS, 0, false)
            .setGroup(GROUP)
            .setGroupSummary(true)
            .setContentIntent(createIntent(R.id.nav_downloads))


    private var completeNotification: NotificationCompat.Builder = NotificationCompat.Builder(context, Notifications.DOWNLOAD_COMPLETE_ID)
            .setContentTitle(context.getString(R.string.download_complete))
            .setSmallIcon(R.drawable.ic_round_done_all_24)
            .setContentIntent(createIntent(R.id.nav_bookmark, Bundle(1)
                    .apply { putInt(BookmarkFragment.KEY_PAGE, BookmarkFragment.PAGE_BOOKMARK) }))

    private val errorNotification: NotificationCompat.Builder = NotificationCompat.Builder(context, Notifications.DOWNLOAD_ERROR_ID)
            .setContentTitle(context.getString(R.string.download_error))
            .setSmallIcon(R.drawable.ic_round_error_24)
            .setContentIntent(createIntent(R.id.nav_downloads))

    private fun createIntent(dest: Int, args: Bundle? = null): PendingIntent {
        return NavDeepLinkBuilder(context).let {
            it.setComponentName(MainActivity::class.java)
            it.setGraph(R.navigation.nav_graph)
            it.setDestination(dest)
            args?.let { arg -> it.setArguments(arg) }

            it.createPendingIntent()
        }
    }

    override fun onDownloadChange(downs: MutableList<Download>, flag: String) {
        if (flag == DownloadChangeListener.FLAG_NEW) return

        downloadManager.submitToExecutor {
            when (downs[0].state) {
                Download.State.DOWNLOADED -> showCompleteNotification(downs)
                Download.State.ERROR -> showErrorNotification(downs)
                else -> showNotification(downs)
            }
        }
    }

    private fun showCompleteNotification(downs: MutableList<Download>) {
        if (downloadManager.getDownloads().isNotEmpty()) return

        with(completeNotification) {
            val text = context.getString(R.string.chapter_download_complete)
            setContentText(text)

            manager.notify(Notifications.DOWNLOAD_COMPLETE_NOTIFY_ID, build())
        }
    }

    private fun showErrorNotification(downs: MutableList<Download>) {
        with(errorNotification) {
            val chap = downs[0].title + downs[0].chapNumber
            val error = context.getString(R.string.error_download, chap)//"Error downloading $chap"
            setContentText(error)

            manager.notify(Notifications.DOWNLOAD_ERROR_NOTIFY_ID, build())
        }
    }

    private fun showNotification(downs: MutableList<Download>) {
        val down = downs[0]

        val title: String = down.title + down.chapNumber
        val length: String = down.state.toString() + "(" + down.progress + "/" + down.length + ")"
        val prog: Int = ((down.progress / down.length.div(1.0)) * MAX_PROGRESS).roundToInt()

        with(progressNotification) {
            setContentTitle(title)
            setContentText(length)

            if (prog == MAX_PROGRESS) {
                setProgress(0, 0, false)
                setAutoCancel(false)
            } else setProgress(MAX_PROGRESS, prog, false)


            manager.notify(Notifications.DOWNLOAD_PROGRESS_NOTIFY_ID, build())
        }

        downs.forEach {
            val progress = ((down.progress / down.length.div(1.0)) * MAX_PROGRESS).roundToInt()

            showMangaSpecificNotification(it, progress)
        }
    }


    private fun showMangaSpecificNotification(down: Download, progress: Int) {
        val manga: Manga? = downloadManager.getManga(down.mangaId)

        val notifyId = downloadManager.getDownloads(down.mangaId)[0].mangaId.times(Notifications.DOWNLOAD_MULTIPLIER)

        manga?.let {
            val cover = Glide.with(context)
                    .asBitmap()
                    .load(manga.thumbnailLink)
                    .onlyRetrieveFromCache(true)
                    .dontTransform()
                    .centerCrop()
                    .circleCrop()
                    .override(mIconWidth, mIconHeight)
                    .submit()
                    .get()

            val notification = NotificationCompat.Builder(context, Notifications.DOWNLOAD_PROGRESS_ID)
                    .setContentTitle(manga.title)
                    .setContentText(down.title)
                    .setSmallIcon(R.drawable.ic_round_download_24)
                    .setProgress(MAX_PROGRESS, progress, false)
                    .setGroup(GROUP)
                    .setLargeIcon(cover)
                    .setAutoCancel(true)
                    /*.setContentIntent(createNavPendingIntent(R.id.frag_manga, Bundle().apply {
                        putString(MangaFragment.MANGA_LINK, manga.link)
                        putBoolean(MangaFragment.AUTO_UPDATE, false)
                    }))*/.build()

            manager.notify(notifyId.toInt(), notification)
        }
    }

    fun dismissNotification() {
        manager.cancel(Notifications.DOWNLOAD_PROGRESS_NOTIFY_ID)
    }

    companion object {
        const val GROUP: String = "download group"
        const val MAX_PROGRESS = 200
    }

}