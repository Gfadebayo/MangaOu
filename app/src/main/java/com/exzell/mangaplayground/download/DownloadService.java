package com.exzell.mangaplayground.download;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.exzell.mangaplayground.MangaApplication;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.notification.Notifications;

import javax.inject.Inject;

public class DownloadService extends Service {

    private final String WAKE_LOCK_TAG = "wakelock:DownloadService";

    @Inject
    public DownloadManager mManager;
    private PowerManager.WakeLock mWakeLock;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        ((MangaApplication) getApplication()).mAppComponent
                .injectDownloadManager(this);

        acquireWakeLock();

        startForeground(Notifications.DOWNLOAD_PROGRESS_NOTIFY_ID, buildTempNotification());
    }

    private Notification buildTempNotification(){
        return new NotificationCompat.Builder(this, Notifications.DOWNLOAD_PROGRESS_ID)
                .setContentTitle(getString(R.string.app_name)).build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mManager.startDownloading();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mManager.stopDownloading();
        if(mWakeLock.isHeld()) mWakeLock.release();
        mWakeLock = null;
    }

    private void acquireWakeLock() {
        PowerManager powerManager = getSystemService(PowerManager.class);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
        mWakeLock.acquire();
    }
}
