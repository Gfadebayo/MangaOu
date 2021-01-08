package com.exzell.mangaplayground.io;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Transaction;

import com.exzell.mangaplayground.AppExecutors;
import com.exzell.mangaplayground.io.database.AppDatabase;
import com.exzell.mangaplayground.io.database.ChapterDao;
import com.exzell.mangaplayground.io.database.DBManga;
import com.exzell.mangaplayground.io.database.DownloadDao;
import com.exzell.mangaplayground.io.database.MangaDao;
import com.exzell.mangaplayground.download.Download;
import com.exzell.mangaplayground.io.internet.InternetManager;
import com.exzell.mangaplayground.io.internet.MangaParkApi;
import com.exzell.mangaplayground.models.Chapter;
import com.exzell.mangaplayground.models.Manga;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

@Singleton
public class Repository {

    private AppExecutors mExecutor;
    private MangaParkApi mMangaPark;

    private ChapterDao mChapterDao;
    private MangaDao mMangaDao;
    private DownloadDao mDownloadDao;

    @Inject
    public Repository(AppExecutors exec, Retrofit service, AppDatabase db){
        mExecutor = exec;

        mMangaPark = service.create(MangaParkApi.class);

        mMangaDao = db.getMangaDao();
        mChapterDao = db.getChapterDao();
        mDownloadDao = db.getDownloadDao();
    }

    public Call<ResponseBody> advancedSearch(Map<String, String> queries){
        Call<ResponseBody> doc = null;

        try {
            doc = mExecutor.getIoExecutor().submit(() -> mMangaPark.advancedSearch(queries)).get();
        }catch(CancellationException | ExecutionException | InterruptedException e){e.printStackTrace();}

        return doc;
    }

    public Response<ResponseBody> home(){
        Response<ResponseBody> doc = null;

        try {
            doc = mExecutor.getIoExecutor().submit(() -> mMangaPark.home().execute()).get();
        }catch(CancellationException | ExecutionException | InterruptedException e){e.printStackTrace();}

        return doc;
    }

    /** Moves to a particular page in the same website */
    public Observable<ResponseBody> moveTo(String link){
        return mMangaPark.next(link);
    }

    /** Moves to an entirely different website */
    public Observable<ResponseBody> goTo(String link){
        Request req = new Request.Builder().url(link).build();
        try {
            return Observable.just(InternetManager.mClient.newCall(req).execute().body());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }




    //Database calls
    @Transaction
    public void insertManga(Manga... manga){
        mExecutor.getDiskExecutor().submit((() -> {
            mMangaDao.insertMangas(Arrays.asList(manga));

            mChapterDao.insertChapters(Arrays.stream(manga).flatMap(man -> man.getChapters()
                    .stream()).collect(Collectors.toList()));
        }));
    }

    @Transaction
    public void updateManga(Manga... manga){
        mExecutor.getDiskExecutor().submit(() -> {
            mMangaDao.updateMangas(Arrays.asList(manga));
            mChapterDao.insertChapters(Arrays.stream(manga).flatMap(man -> man.getChapters()
                    .stream()).collect(Collectors.toList()));
        });
    }

    public Manga getMangaWithLink(String link) {
        try {
            return mExecutor.getDiskExecutor().submit((Callable<Manga>) () -> mMangaDao.getMangaFromLink(link)).get();

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Manga> getMangas(){
        List<Manga> manga = new ArrayList<>();

        try {
            manga.addAll(mExecutor.getDiskExecutor().submit(() -> mMangaDao.getMangas()).get());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return manga;
    }

    public List<DBManga> getBookmarkedMangaNotLive(){

        try {
            return mExecutor.getDiskExecutor().submit(() -> mMangaDao.notLiveBookmarks()).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }

    public LiveData<List<DBManga>> getBookmarkedManga(){
        try {
            return mExecutor.getDiskExecutor().submit(() -> mMangaDao.bookmarks()).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public LiveData<List<DBManga>> getDownloadedMangas(){
        try{
            return mExecutor.getDiskExecutor().submit(() -> mMangaDao.downloads()).get();
        }catch(ExecutionException | InterruptedException e){
            e.printStackTrace();
            return null;
        }
    }

    public List<Long> allDate(){
        List<Long> dates = new ArrayList<>();
        try {
            dates = mExecutor.getDiskExecutor().submit(() -> mChapterDao.allTime()).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return dates;
    }

    public List<DBManga> lastReadMangas(long time){
        List<DBManga> mangas = new ArrayList<>();

        try {
            mangas = mExecutor.getDiskExecutor().submit(() -> mMangaDao.getMangaLastChapter(time)).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return mangas;
    }

    public void updateChapters(List<Chapter> chapter) {
        mExecutor.getDiskExecutor().submit(() -> mChapterDao.updateChapters(chapter));
    }

    public void insertDownloads(List<Download> downs) {
        new Thread(() -> mDownloadDao.addDownloads(downs)).start();
    }

    public void deleteDownloads(List<Download> downs){
        new Thread(() -> mDownloadDao.deleteDownloads()).start();
    }

    public LiveData<List<Download>> getLiveDownloads(){

        try { return mExecutor.getDiskExecutor().submit(() -> mDownloadDao.getPendingDownloadsLive()).get(); }
        catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public LiveData<List<Download>> getDownloads(){
        try{
            return mExecutor.getDiskExecutor().submit(() -> mDownloadDao.getAllDownloads()).get();
        }catch(ExecutionException | InterruptedException e){
            e.printStackTrace();
        return null;
        }
    }

    public List<Download> getCurrentDownloads() {

        try {
            return mExecutor.getDiskExecutor().submit(() -> mDownloadDao.getPendingDownloads()).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateDownloads(List<Download> d){
        mExecutor.getDiskExecutor().submit(() -> mDownloadDao.updateDownloads(d));
    }

    public String getDownloadPath(long chapterId){
        try {
            return mExecutor.getDiskExecutor().submit(() -> mDownloadDao.getPathFromId(chapterId)).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public DBManga getMangaForChapter(long id) {

        try {
            return mExecutor.getDiskExecutor().submit(() -> mMangaDao.getMangaFromChapter(id)).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void resetTime(Chapter chapter){
        mExecutor.getDiskExecutor().submit(() -> {
           mChapterDao.resetTime(chapter.getId());
        });
    }

    @NotNull
    public List<DBManga> getMangaWithLinks(@NonNull List<Long> ids) {

            try {

                return mExecutor.getDiskExecutor().submit(() -> ids.stream().map(s ->
                        mMangaDao.getMangaFromId(s)).collect(Collectors.toList())).get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                return Collections.EMPTY_LIST;
            }
    }
}
