package com.exzell.mangaplayground.viewmodels;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.SavedStateHandle;

import com.exzell.mangaplayground.io.Repository;
import com.exzell.mangaplayground.models.Manga;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class HomeViewModel extends AndroidViewModel {
    private final String TAG = "ViewModel";
    private Repository mRepo;
    private Context mContext;
    private ExecutorService mExecutor;
    private SavedStateHandle mHandle;

    private final String KEY_LINK = "next link";
    private final String KEY_MANGAS = "cached mangas";

    public HomeViewModel(@NonNull Application application, SavedStateHandle handle) {
        super(application);
        mRepo = Repository.getInstance(application);
        mContext = application.getApplicationContext();
        mExecutor = Executors.newFixedThreadPool(4);
        mHandle = handle;
    }

    public void parseHome(BiConsumer<List<? extends Manga>, Integer> consumer, int popularIndex, int latestIndex){

        mExecutor.submit(() -> {
            Response<ResponseBody> res = mRepo.home();

            if(res.isSuccessful()) {
                try {
                    Document docu = Jsoup.parse(res.body().string());
                    res.body().close();

                new Thread(() -> popularUpdates(docu, consumer, popularIndex)).start();

                new Thread(() -> latestRelease(docu, consumer, latestIndex)).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(mContext, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void queryDb(BiConsumer<List<? extends Manga>, Integer> consumer, int bookmarkIndex, int downloadIndex, LifecycleOwner o){

        mRepo.getBookmarkedManga().observe(o, dbMangas -> consumer.accept(dbMangas, bookmarkIndex));

        mRepo.getDownloadedMangas().observe(o, dbMangas -> consumer.accept(dbMangas, downloadIndex));
    }

    /**
     * On Analyzing the home html, popular updates
     * are all under a <b>li</b> tag with class as used in the code
     * @param docu The Document of the home html
     */
    private void popularUpdates(Document docu, BiConsumer<List<? extends Manga>, Integer> consumer, int acceptIndex){

        //Gives every <a> tag which is part of popular updates
        List<Manga> popularMangas = docu.body().getElementsByAttributeValue("class", "thm-effect radius")
                .stream().map(c -> {
                    String link = c.attr("href");
                    String title = c.attr("title");
                    String thumbnail = c.getElementsByTag("img").first().attr("src");
                    if (!thumbnail.startsWith("https:")) thumbnail = "https:" + thumbnail;

                    Manga manga = new Manga(link);
                    manga.setThumbnailLink(thumbnail);
                    manga.setTitle(title);

                    Log.i(TAG, link);

                    return manga;
//                    mangaSubscriber(mRepo.moveTo(link), link, adapter);
                }).collect(Collectors.toList());

        consumer.accept(popularMangas, acceptIndex);
    }

    /**
     * Just like @see popularUpdates#, The mangas under this can be found in a <div> tag with
     * class as used below
     * @param docu The Document of the home html
     * @return A list of manga found under Latest Releases
     */
    private void latestRelease(Document docu, BiConsumer<List<? extends Manga>, Integer> consumer, int acceptIndex){

        Elements attr = docu.body().getElementsByAttributeValue("class", "bd ls1");
        List<Manga> latestMangas = attr.first().getElementsByClass("cover").stream().map(c -> {
            String link = c.attr("href");
            String title = c.attr("title");
            String thumbnail = c.getElementsByTag("img").attr("src");
            if (!thumbnail.startsWith("https:")) thumbnail = "https:" + thumbnail;


            Manga manga = new Manga(link);
            manga.setTitle(title);
            manga.setThumbnailLink(thumbnail);

            return manga;
        }).collect(Collectors.toList());

        consumer.accept(latestMangas, acceptIndex);
    }

    public Disposable goToLink(String link, Consumer<List<Manga>> next) {
        return mRepo.moveTo(link).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(res -> {
                    String response = res.string();
                    res.close();

                    Log.i(TAG, link);
                    List<Manga> mangas = new ArrayList<>();
                    if (!response.isEmpty()) {

                        Document doc = Jsoup.parse(response);
                        if(doc.hasClass("col-12 no-match")) return mangas;

                        Elements mangaHtml = doc.select("a[class=cover]");

                        mangas.addAll(mangaHtml.stream().map(m -> {
                            String mlink = m.attr("href");
                            String title = m.attr("title");

                            String thumbLink = m.select("img[src*=//file-thumb.mangapark.net/W300/]").attr("src");
                            if(!thumbLink.startsWith("https:")) thumbLink = "https://" + thumbLink;

                            Manga manga = new Manga(mlink);
                            manga.setTitle(title);
                            manga.setThumbnailLink(thumbLink);

                            return (manga);
                        }).collect(Collectors.toList()));
                    }
                    return mangas;
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(mangas -> {
                    setNextLink(nextLink(link));
                    next.accept(mangas);
                    cacheMangas(mangas);
                }, thr -> thr.printStackTrace());
    }

    private String nextLink(String link){
        StringBuilder newLink = new StringBuilder(link);
        int index = newLink.lastIndexOf("/");
        String num = newLink.substring(index+1);
        int newNum = num.isEmpty() || !Character.isDigit(num.charAt(0)) ? 2 : Integer.parseInt(num)+1;
        newLink.replace(index+1, newLink.length(), String.valueOf(newNum));

        return newLink.toString();
    }

    private void setNextLink(String nextLink){
        mHandle.set(KEY_LINK, nextLink);
    }

    private void cacheMangas(List<Manga> additions){

        ((ArrayList<Manga>) mHandle.get(KEY_MANGAS)).addAll(additions);
    }

    public String getNextLink(){return mHandle.get(KEY_LINK);}

    public List<Manga> getCachedMangas(){
        return mHandle.get(KEY_MANGAS);
    }

    public void initHandler(String startLink){
        if(mHandle.contains(KEY_LINK)) setNextLink(startLink);
        if(!mHandle.contains(KEY_MANGAS)) mHandle.set(KEY_MANGAS, new ArrayList<Manga>());
    }
}
