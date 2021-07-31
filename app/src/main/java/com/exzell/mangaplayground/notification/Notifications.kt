package com.exzell.mangaplayground.notification

import android.app.NotificationChannel
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

    const val BOOKMARK_NEW_CHAPTER_ID = "latest chapters"

    /** A value we multiply to the id of a manga downloading
     * before posting it as the notification id. So it
     * wont be clashing with other notifications that also
     * work with the manga id like Updates
     */
    const val DOWNLOAD_MULTIPLIER = -3000


    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        with(context) {
            NotificationManagerCompat.from(this).also {
                it.createNotificationChannels(listOf(
                        NotificationChannel(DOWNLOAD_PROGRESS_ID, getString(R.string.downloader), NotificationManager.IMPORTANCE_LOW),
                        NotificationChannel(DOWNLOAD_COMPLETE_ID, getString(R.string.download_complete), NotificationManager.IMPORTANCE_LOW),
                        NotificationChannel(DOWNLOAD_ERROR_ID, getString(R.string.download_error), NotificationManager.IMPORTANCE_LOW),
                        NotificationChannel(BOOKMARK_ID, getString(R.string.bookmark_update), NotificationManager.IMPORTANCE_LOW),
                        NotificationChannel(BOOKMARK_NEW_CHAPTER_ID, getString(R.string.latest_chapters), NotificationManager.IMPORTANCE_LOW)))
            }
        }
    }
}