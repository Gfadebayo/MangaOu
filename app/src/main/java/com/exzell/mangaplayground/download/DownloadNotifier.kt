package com.exzell.mangaplayground.download

import android.app.*
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.exzell.mangaplayground.MainActivity
import com.exzell.mangaplayground.R
import com.exzell.mangaplayground.notification.Notifications
import kotlin.math.roundToInt

class DownloadNotifier(private val context: Context) : DownloadChangeListener {

    var title: String? = null

    val manager: NotificationManagerCompat = NotificationManagerCompat.from(context)

    private var progressNotification: NotificationCompat.Builder = NotificationCompat.Builder(context, Notifications.DOWNLOAD_PROGRESS_ID)
            .setContentTitle("Downloading")
            .setSmallIcon(R.drawable.ic_file_download_black_24dp)
            .setProgress(200, 0, false)
            .setContentIntent(createIntent())


    private var completeNotification: NotificationCompat.Builder = NotificationCompat.Builder(context, Notifications.DOWNLOAD_COMPLETE_ID)
            .setContentTitle("Download Complete")
            .setSmallIcon(R.drawable.ic_done_all_black_24dp)
            .setContentIntent(createIntent())

    private val errorNotification: NotificationCompat.Builder = NotificationCompat.Builder(context, Notifications.DOWNLOAD_ERROR_ID)
            .setContentTitle("Download Error")
            .setSmallIcon(R.drawable.ic_error_outline_black_24dp)
            .setContentIntent(createIntent())

    private fun createIntent(): PendingIntent? {
        return NavDeepLinkBuilder(context)
                .setComponentName(MainActivity::class.java)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.nav_downloads).createPendingIntent()
    }

    override fun onDownloadChange(down: Download, flag: String) {
        if(flag.equals(DownloadChangeListener.FLAG_NEW)) return

        when(down.state) {
            Download.State.DOWNLOADED -> showCompleteNotification(down)
            Download.State.ERROR -> showErrorNotification(down)
            else -> showNotification(down)
        }
    }

    private fun showCompleteNotification(down: Download) {
        with(completeNotification){
            val text = "All Chapters Downloaded"
            setContentText(text)

            manager.notify(Notifications.DOWNLOAD_COMPLETE_NOTIFY_ID, build())
        }
    }

    private fun showErrorNotification(down: Download) {
        with(errorNotification){
            val chap = down.title + down.chapNumber
            val error = "Error downloading $chap"
            setContentText(error)

            manager.notify(Notifications.DOWNLOAD_ERROR_NOTIFY_ID, build())
        }
    }

    private fun showNotification(down: Download){
        val title: String = down.title + down.chapNumber
        val length: String = down.state.toString() + "(" + down.progress + "/" + down.length + ")"
        val prog :Int = ((down.progress / down.length.div(1.0)) * 200).roundToInt()

        with(progressNotification) {
            setContentTitle(title)
            setContentText(length)

            if(prog == 200) {
                setProgress(0, 0, false)
                setAutoCancel(false)
            } else setProgress(200, prog, false)

            manager.notify(Notifications.DOWNLOAD_PROGRESS_NOTIFY_ID, build())
        }
    }

//    fun endNotification(left: Int){
//        val downLeft: String = left.toString() + " Downloads left"
//
//        with(progressNotification){
//            setContentTitle(downLeft)
//            setOngoing(false)
//            setAutoCancel(false)
//            manager.notify(Notifications.DOWNLOAD_COMPLETE_NOTIFY_ID, build())
//        }
//    }

    fun dismissNotification(){
        manager.cancel(Notifications.DOWNLOAD_PROGRESS_NOTIFY_ID)
    }

}