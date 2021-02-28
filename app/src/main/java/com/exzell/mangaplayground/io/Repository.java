package com.exzell.mangaplayground.io;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.exzell.mangaplayground.AppExecutors;
import com.exzell.mangaplayground.download.Download;
import com.exzell.mangaplayground.io.database.AppDatabase;
import com.exzell.mangaplayground.io.database.ChapterDao;
import com.exzell.mangaplayground.io.database.DBManga;
import com.exzell.mangaplayground.io.database.DownloadDao;
import com.exzell.mangaplayground.io.database.MangaDao;
import com.exzell.mangaplayground.io.internet.InternetManager;
import com.exzell.mangaplayground.io.internet.MangaParkApi;
import com.exzell.mangaplayground.models.Chapter;
import com.exzell.mangaplayground.models.Manga;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Observable;
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
    public Repository(AppExecutors exec, Retrofit service, AppDatabase db) {
        mExecutor = exec;

        mMangaPark = service.create(MangaParkApi.class);

        mMangaDao = db.getMangaDao();
        mChapterDao = db.getChapterDao();
        mDownloadDao = db.getDownloadDao();
    }

    //TODO: Consider changing the return type of both methods to Observables

    public Call<ResponseBody> advancedSearch(Map<String, String> queries) {

        try {
            return mExecutor.getIoExecutor().submit(() -> mMangaPark.advancedSearch(queries)).get();
        } catch (CancellationException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Response<ResponseBody> home() {
        try {
            return mExecutor.getIoExecutor().submit(() -> mMangaPark.home().execute()).get();
        } catch (CancellationException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Moves to a particular page in the same website
     */
    public Observable<ResponseBody> moveTo(String link) { return mMangaPark.next(link); }

    /**
     * Moves to an entirely different website
     */
    public Observable<ResponseBody> goTo(String link) {
        try {
            Request req = new Request.Builder().url(link).build();
            return Observable.just(InternetManager.mClient.newCall(req).execute().body());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    //Database calls

    //Calls to MangaDao

    /**
     * Takes care of inserting the mangas into the DB. After the manga id is gotten from the DB,
     * it is set into its manga instance, so be sure not to replace the instances passed to this method
     */
    public void insertManga(Manga... manga) {
        mExecutor.getDiskExecutor().submit((() -> {
            for (Manga man : manga) {
                long id = mMangaDao.insertMangas(man);
                man.getChapters().forEach(chap -> chap.setMangaId(id));
                man.setId(id);
            }

            mChapterDao.insertChapters(Arrays.stream(manga).flatMap(man -> man.getChapters()
                    .stream()).collect(Collectors.toList()));
        }));
    }

    public void updateManga(boolean andChapters, Manga... manga) {
        mExecutor.getDiskExecutor().submit(() -> {
            mMangaDao.updateMangas(Arrays.asList(manga));
            if (andChapters) mChapterDao.insertChapters(Arrays.stream(manga)
                    .flatMap(man -> man.getChapters().stream()).collect(Collectors.toList()));
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

//    public List<Manga> getMangas(){
//        List<Manga> manga = new ArrayList<>();
//
//        try {
//            return mExecutor.getDiskExecutor().submit(() -> mMangaDao.getMangas()).get();
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//            return Collections.emptyList();
//        }
//    }

    public List<DBManga> getBookmarkedMangaNotLive() {

        try {
            return mExecutor.getDiskExecutor().submit(() -> mMangaDao.notLiveBookmarks()).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public LiveData<List<DBManga>> getBookmarkedManga() {
        try {
            return mExecutor.getDiskExecutor().submit(() -> mMangaDao.bookmarks()).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public LiveData<List<DBManga>> getDownloadedMangas() {
        try {
            return mExecutor.getDiskExecutor().submit(() -> mMangaDao.downloads()).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    public List<DBManga> getMangaWithIds(@NonNull List<Long> ids) {

        try {
            return mExecutor.getDiskExecutor().submit(() -> ids.stream().map(s ->
                    mMangaDao.getMangaFromId(s)).collect(Collectors.toList())).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<DBManga> lastReadMangas(long time) {
        try {
            return mExecutor.getDiskExecutor().submit(() -> mMangaDao.getMangaFromChapterTime(time)).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return Collections.emptyList();
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

    //Calls to ChapterDao

    public void updateChapters(List<Chapter> chapter) {
        mExecutor.getDiskExecutor().submit(() -> mChapterDao.updateChapters(chapter));
    }

    /**
     * Returns the timestamp for every manga with atleast 1 chapter with a read time greater than 0
     */
    public LiveData<List<Long>> allMangaTime() {
        try {
            return mExecutor.getDiskExecutor().submit(() -> mChapterDao.allMangaTime()).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Calls to DownloadDao

    public void insertDownloads(List<Download> downs) {
        new Thread(() -> mDownloadDao.addDownloads(downs)).start();
    }

    public void deleteDownloads(List<Download> downs) {
        new Thread(() -> mDownloadDao.deleteDownloads()).start();
    }

    public LiveData<List<Download>> getLiveDownloads() {

        try {
            return mExecutor.getDiskExecutor().submit(() -> mDownloadDao.getPendingDownloadsLive()).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public LiveData<List<Download>> getDownloads() {
        try {
            return mExecutor.getDiskExecutor().submit(() -> mDownloadDao.getAllDownloads()).get();
        } catch (ExecutionException | InterruptedException e) {
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

    public void updateDownloads(List<Download> d) {
        mExecutor.getDiskExecutor().submit(() -> mDownloadDao.updateDownloads(d));
    }

    public String getDownloadPath(long chapterId) {
        try {
            return mExecutor.getDiskExecutor().submit(() -> mDownloadDao.getPathFromId(chapterId)).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
