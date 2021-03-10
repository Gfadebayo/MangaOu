package com.exzell.mangaplayground.notification

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.exzell.mangaplayground.R

object Notifications {

    const val DOWNLOAD_PROGRESS_NOTIFY_ID = -13
    const val DOWNLOAD_PROGRESS_ID = "download progress"

    const val DOWNLOAD_COMPLETE_NOTIFY_ID = -15
    const val DOWNLOAD_COMPLETE_ID = "download complete"

    const val DOWNLOAD_ERROR_NOTIFY_ID = -17
    const val DOWNLOAD_ERROR_ID = "download error"

    const val BOOKMARK_NOTIFY_ID = -19
    const val BOOKMARK_ID = "bookmark update"

    const val BOOKMARK_NEW_CHAPTER_GROUP_ID = "latest chapters"


    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        NotificationManagerCompat.from(context).also {
            it.createNotificationChannels(listOf(
                    NotificationChannel(DOWNLOAD_PROGRESS_ID, context.getString(R.string.downloader), NotificationManager.IMPORTANCE_LOW),
                    NotificationChannel(DOWNLOAD_COMPLETE_ID, context.getString(R.string.download_complete), NotificationManager.IMPORTANCE_LOW),
                    NotificationChannel(DOWNLOAD_ERROR_ID, context.getString(R.string.download_error), NotificationManager.IMPORTANCE_LOW),
                    NotificationChannel(BOOKMARK_ID, context.getString(R.string.bookmark_update), NotificationManager.IMPORTANCE_LOW)))

            it.createNotificationChannelGroups(listOf(
                    NotificationChannelGroup(BOOKMARK_NEW_CHAPTER_GROUP_ID, context.getString(R.string.latest_chapters))
            ))
        }
    }
}