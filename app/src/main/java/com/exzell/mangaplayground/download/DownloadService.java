package com.exzell.mangaplayground.download;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.exzell.mangaplayground.notification.Notifications;

public class DownloadService extends Service {

    private final String TAG = "wakelock:DownloadService";
    private DownloadManager mManager;
    private PowerManager.WakeLock mWakeLock;
    private Downloader mDownloader;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mManager = DownloadManager.getInstance(getApplication());
        mDownloader = new Downloader(mManager, this);
        acquireWakeLock();
        //TODO: Find a better way to resume failed downloads

        startForeground(Notifications.DOWNLOAD_PROGRESS_NOTIFY_ID, buildTempNotification());
    }

    private Notification buildTempNotification(){
        return new NotificationCompat.Builder(this, Notifications.DOWNLOAD_PROGRESS_ID)
                .setContentTitle("Manga Ou").build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mDownloader.startDownloading();
        return super.onStartCommand(intent, flags, startId);
    }
//
//    private void startDownloading(){
//        if(isRunning) return;
//
//        mDisposable.clear();
//        PublishSubject.create()
//        mDisposable.add(Observable.fromIterable(mManager.getPendingDownloads())
//                .filter(p -> !p.getState().equals(Download.State.CANCELLED))
//                .subscribeOn(Schedulers.io())
//                .flatMap(down -> DownloadService.this.createDownloadPages(down), 5)
//                .doOnNext(page -> {
//                    if(page.getState().equals(DownloadPage.State.DOWNLOADED)) incrementProgress(page.getParent());
//                }).doOnComplete(() -> {
//                    isRunning = false;
//                    mDisposable.clear();
//                }).subscribe());
//
//        isRunning = true;
//    }
//
//    private void stopDownloading(){
//        if(!isRunning) return;
//
//        mDisposable.clear();
//        isRunning = false;
//    }
//
//    private Observable<DownloadPage> createDownloadPages(Download d){
//        return Observable.range(1, d.getLength())
//                .map(i -> {
//                    //checks to see if the page has already been downloaded
//                    String pageNumber = createPageNumber(i);
//                    String pagePath = createPagePath(d.getPath(), pageNumber);
//                    String pageUrl = createPageUrl(d.getLink(), i);
//
//                    return new DownloadPage(pageNumber, pagePath, pageUrl, d, DownloadPage.State.PENDING);
//                }).doOnNext(downloadPage -> {
//                    if(!checkDownloadState(downloadPage.getParent())) return;
//
//                    if(checkFile(downloadPage)) downloadPage.setState(DownloadPage.State.DOWNLOADED);
//                    else downloadPage(downloadPage);
//                });
//
//    }
//
//
//    private boolean checkDownloadState(Download d){
//        if(d.getState().equals(Download.State.QUEUED) || d.getState().equals(Download.State.DOWNLOADING)) {
//            d.setState(Download.State.DOWNLOADING);
//            return true;
//        }
//        return false;
//    }
//    private String createPageNumber(int number){
//        return number < 10 ? "0" + number : String.valueOf(number);
//    }
//
//    private String createPagePath(String path, String number){
//        File pageFile = new File(path, number);
//        return pageFile.getPath();
//    }
//
//    private String createPageUrl(String link, int number){
//        StringBuilder build = new StringBuilder(link);
//        int lastIndex = build.lastIndexOf("/");
//
//        return build.replace(lastIndex + 1, build.length(), String.valueOf(number)).toString();
//    }
//
//    private void downloadPage(DownloadPage page) throws IOException {
//        if(!NetworkMonitorKt.isConnectedToNetwork(this)) return;
//
//        HttpUrl url = HttpUrl.get(mBaseUrl + page.getUrl());
//
//        Log.i("Download", "Fetching Links for page number: " + page.getNumber());
//
//        Request req = new Request.Builder().get().url(url).build();
//
//        Response response = mClient.newCall(req).execute();
//
//        if (response.isSuccessful()) {
//            String html = response.body().string();
//            String extension = '.' + decipherImageExtension(response.body().contentType());
//            response.close();
//
//            String downloadLink = ChapterUtils.fetchDownloadLink(Jsoup.parse(html));
//
//            byte[] bytes = downloadImages(downloadLink);
//
////            if(bytes == null) {
////                page.setState(DownloadPage.State.ERROR);
////                return;
////            }
//
//            File file = new File(page.getPath() + extension);
//            if(!file.exists()) file.createNewFile();
//
//            FileOutputStream output = new FileOutputStream(file);
//            output.write(bytes);
//            output.close();
//
//            page.setState(DownloadPage.State.DOWNLOADED);
//        }else{
//            if(BuildConfig.DEBUG) Log.w(TAG, "Response Code: " + response.code());
//            page.setState(DownloadPage.State.ERROR);
//        }
//    }
//
//    private String decipherImageExtension(@Nullable MediaType type){
//        if(type == null) return "png";
//
//        String mimeType = String.format("%s/%s", type.type(), type.subtype());
//
//        String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
//        return ext != null ? ext : "png";
//    }
//
//    private byte[] downloadImages(String link) throws IOException {
//
//        Request downloadRequest = new Request.Builder().url(link).build();
//
//        Response response = mClient.newCall(downloadRequest).execute();
//
//        if(!response.isSuccessful()) throw new IOException("Failed: " + response.code());
//
//            byte[] bytes = response.body().bytes();
//            response.close();
//
//            return bytes;
//    }
//
//    private void incrementProgress(Download d){
//        int prog  = d.getProgress()+1;
//        Log.w("DownloadService", "Current Progress is: " + prog);
//        d.setProgress(prog);
//        mManager.updateDownload(d.getId(), DownloadChangeListener.FLAG_PROGRESS);
//
//        if(d.getProgress() == d.getLength()){
//            d.setState(Download.State.DOWNLOADED);
//            mManager.updateDownload(d.getId(), DownloadChangeListener.FLAG_STATE);
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDownloader.stopDownloading();
        if(mWakeLock.isHeld()) mWakeLock.release();
        mWakeLock = null;
    }

    private void acquireWakeLock() {
        PowerManager powerManager = getSystemService(PowerManager.class);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();
    }
}
