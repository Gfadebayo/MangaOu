package com.exzell.mangaplayground.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

object Notifications {

    const val DOWNLOAD_PROGRESS_NOTIFY_ID = 13
    const val DOWNLOAD_PROGRESS_ID = "download progress"

    const val DOWNLOAD_COMPLETE_NOTIFY_ID = 15
    const val DOWNLOAD_COMPLETE_ID = "download complete"

    const val DOWNLOAD_ERROR_NOTIFY_ID = 17
    const val DOWNLOAD_ERROR_ID = "download error"

    const val BOOKMARK_NOTIFY_ID = 19
    const val BOOKMARK_ID = "bookmark update"


    fun createChannels(context: Context){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        NotificationManagerCompat.from(context)
                .createNotificationChannels(listOf(
                        NotificationChannel(DOWNLOAD_PROGRESS_ID, "Downloader", NotificationManager.IMPORTANCE_LOW),
                        NotificationChannel(DOWNLOAD_COMPLETE_ID, "Download Complete", NotificationManager.IMPORTANCE_LOW),
                        NotificationChannel(DOWNLOAD_ERROR_ID, "Download Error", NotificationManager.IMPORTANCE_LOW),
                        NotificationChannel(BOOKMARK_ID, "Bookmark Update", NotificationManager.IMPORTANCE_LOW)))
    }
}