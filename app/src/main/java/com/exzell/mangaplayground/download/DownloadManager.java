package com.exzell.mangaplayground.download;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import com.exzell.mangaplayground.di.ActivityScope;
import com.exzell.mangaplayground.io.Repository;
import com.exzell.mangaplayground.notification.Notifications;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.exzell.mangaplayground.download.DownloadChangeListener.*;

@Singleton
public class DownloadManager {

    private Context context;
    private Map<Long, Download> downloads;
    private Repository repo;
    private List<DownloadChangeListener> listeners;
    private DownloadNotifier notification;
    private boolean isServiceRunning;
    private Observer<List<Download>> observer = this::dataChanged;
    private Intent mServiceIntent;

    /**
     * Adds and notifies listeners when a new set of downloads is received from the database
     */
    private void dataChanged(List<Download> newDownloads) {
        if(this.downloads.values().containsAll(newDownloads)) return;

        ArrayList<Download> copy = new ArrayList<>(newDownloads);

        copy.removeAll(downloads.values());
        copy.forEach(c -> {
            downloads.put(c.getId(), c);
            updateDownload(c.getId(), DownloadChangeListener.FLAG_NEW);
        });
    }

    @Inject
    public DownloadManager(Context context, Repository repo){
        this.context = context;
        this.repo = repo;
        downloads = new HashMap<>();

        listeners = new ArrayList<>();

        repo.getLiveDownloads().observeForever(observer);

        notification = new DownloadNotifier(context);
        addListener(notification);
    }

    /**
     * Starts the download service. This method is not called in the constructor due to
     * the fact that it is possible for there to not be an active download or a pause download
     * by the user
     */
    public void startDownloadService(){
        if(isServiceRunning) return;

        mServiceIntent = new Intent(context, DownloadService.class);
        ContextCompat.startForegroundService(context, mServiceIntent);
        isServiceRunning = true;
    }

    void stopService(){
        if(!isServiceRunning) return;

        notification.dismissNotification();
        context.stopService(mServiceIntent);
        NotificationManagerCompat.from(context).cancel(Notifications.DOWNLOAD_PROGRESS_NOTIFY_ID);
        isServiceRunning = false;
    }

    /**
     * Adds an observer which will be notified when a download changes
     */
    public void addListener(DownloadChangeListener listener){
        this.listeners.add(listener);
    }

    /**
     * Called by INSTANCE holders to inform the INSTANCE that either a new download has been queued
     * or an ongoing download's state or progress has changed
     * @param flag The listener flag giving the exact action that happened
     */
    public void updateDownload(long updateId, String flag){
        Download changed = downloads.get(updateId);
        listeners.forEach(l -> l.onDownloadChange(changed, flag));

        if(flag.equals(DownloadChangeListener.FLAG_STATE)) {

            if(changed.getState().equals(Download.State.CANCELLED)) removeStoppedDownloads();
            else updateDb(Collections.singletonList(changed));

            if(changed.getState().equals(Download.State.DOWNLOADED)) {
                downloads.remove(updateId);
                if(downloads.isEmpty()) stopService();
            }
        }
    }

    /**
     * Returns an immutable list of the current downloads to be made
     */
    public List<Download> getDownloads(){
        List<Download> vals = new ArrayList<>(downloads.values());
        return Collections.unmodifiableList(vals);
    }

    private void removeStoppedDownloads(){
        List<Download> toBeRemoved = downloads.values().stream()
                .filter(p -> p.getState().equals(Download.State.CANCELLED)).collect(Collectors.toList());

        toBeRemoved.forEach(down -> downloads.remove(down.getId()));
        if(!toBeRemoved.isEmpty()) repo.deleteDownloads(toBeRemoved);
    }

    private void updateDb(List<Download> downloads){
        repo.updateDownloads(downloads);
    }
}
