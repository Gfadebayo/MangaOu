package com.exzell.mangaplayground.utils;

import android.os.Build;
import android.util.Log;
import android.widget.RadioGroup;

import com.exzell.mangaplayground.BuildConfig;
import com.exzell.mangaplayground.models.Chapter;
import com.exzell.mangaplayground.models.Manga;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.exzell.mangaplayground.BuildConfig.*;

public class ChapterUtils {

    private static final String TAG = "ChapterUtils";

    /**
     * Uses RxJava to concurrently create the manga chapters and inserts them into the manga
     * This method returns a Disposable for the caller to properly dispose
     */
    public static void createChapterWithObservable(Document doc, Manga manga){

        Elements select = doc.body().select("div[id*=stream]");

        Observable.range(0, select.size()).flatMap(integer -> Observable.just(select.get(integer))
                .flatMap((Function<Element, ObservableSource<Chapter>>) ele -> {
                    String version = ele.select("span[class*=ml-1 stream-text]").text();

                    Chapter.Version vers = Stream.of(Chapter.Version.values())
                            .filter(p -> version.equalsIgnoreCase(p.getDispName()))
                            .findFirst().get();

                    return createChapters(ele, manga, vers);
                }, 30)).map(chapter -> manga).blockingSubscribe();
    }

    private static Observable<Chapter> createChapters(Element parent, Manga manga, Chapter.Version version){

        Elements allLinks = parent.select("a[class=ml-1 visited ch]");

        String[] allTime = parent.select("span[class=time]").text().split("ago");
        Elements newChapter = parent.select("li[class*=d-flex py-1 item new]");

        List<String> newChapNum = Stream.of(newChapter.text().split(" 1 3 6 10 all of ")).filter(p -> p.startsWith("ch.")).collect(Collectors.toList());

        String[] chapLengths = parent.select("em").text().split("1 3 6 10 all of ");
        Elements li = parent.select("li[class*=d-flex py-1 item]");


        return Observable.range(0, allLinks.size()).map(i -> {
            Chapter chap = new Chapter();
            chap.setVersion(version);


            String chapterNumber = allLinks.get(i).text();
            chap.setNumber(chapterNumber);
            chap.setReleaseDate(DateUtils.translateTime(allTime[i]));
            chap.setLink(allLinks.get(i).attr("href"));
            chap.setNewChap(newChapNum.contains(chapterNumber));

            String title = fixTitle(li.get(i).select("div[class=d-none d-md-flex align-items-center ml-0 ml-md-1  txt]").text());
            Log.i(TAG, title);
            chap.setTitle(title);
            String length = chapLengths[i + 1].trim();
            chap.setLength(Integer.parseInt(length));

            int position = allLinks.size()-1-i;
            chap.setId(generateId(chap.getLink(), position, chap.getNumber(), chap.getVersion()));
            chap.setPosition(position);

            return chap;
        }).doOnNext(manga::addChapter);
    }

    /**
     * Titles seem to start with a colon which is stressful to remove from the scraping
     * So its better to remove them directly here
     */
    private static String fixTitle(String title){
        String trimmedTitle = title.trim();

        if(!trimmedTitle.isEmpty() && trimmedTitle.charAt(0) == ':') trimmedTitle = new StringBuilder(trimmedTitle).deleteCharAt(0).toString().trim();

        return !trimmedTitle.isEmpty() ? trimmedTitle : "No Title";
    }

    /**
     * This method is needed in order to split chapters joined together with a vol(for example vol 4 ch 35)
     * @param numbers
     * @return
     */
    private static List<String> splitChapterNumbers(String numbers){
        List<String> chapters = new ArrayList<>(1000);
        boolean skipNextSpace = false;
        int start = 0;

        for(int i = 0; i < numbers.length(); i++){
            boolean lastChar = i == numbers.length()-1;

            if(lastChar) chapters.add(numbers.substring(start));
            else if(numbers.charAt(i) == ' ' && !skipNextSpace){
                chapters.add(numbers.substring(start, i));
                start = i+1;
            } else if(numbers.charAt(i) == ' ' && skipNextSpace){
                skipNextSpace = false;
            }

            if(numbers.substring(i).startsWith("vol.")) skipNextSpace = true;
        }

        return chapters;
    }

    public static String fetchDownloadLink(Document doc){

        List<DataNode> downloadLink = doc.select("script").dataNodes();

        String link = downloadLink.stream().filter(p -> p.getWholeData().contains("_load_pages")).findFirst().get().getWholeData();
        return parseDownloadLink(link).trim();
    }

    private static String parseDownloadLink(String link){
        String https = Arrays.stream(link.split("\"")).filter(p -> p.contains("https")).findFirst().orElse("");
        StringBuilder build = new StringBuilder(https);
        for(int i = 0; i < build.length(); i++){
            if(build.charAt(i) == '\\') build.deleteCharAt(i);
        }

        return build.toString();
    }

    /**
     * Transfers the user defined fields value from similar chapters between the old and new
     */
    public static List<Chapter> transferChapterInfo(List<Chapter> newChaps, List<Chapter> oldChaps){
        if(oldChaps.isEmpty()) return newChaps;

        //Remove chapters that have been found from the oldChaps so we wont slow down the searching
        ArrayList<Chapter> foundChapters = new ArrayList<>(oldChaps.size());

        oldChaps.sort((o1, o2) -> Integer.compare(o2.getPosition(), o1.getPosition()));

        List<Chapter> old = newChaps.stream().filter(p -> {

            //Remove the old chapter and add it to found chapter so the searching will be faster
            if(oldChaps.contains(p)){
                foundChapters.add(oldChaps.remove(oldChaps.indexOf(p)));
                return true;
            }
            return false;
        }).collect(Collectors.toList());

        for(int i = 0; i < old.size(); i++){

            Chapter chap = old.get(i);
            Chapter oldChap = foundChapters.get(foundChapters.indexOf(chap));

            chap.setBookmarked(oldChap.isBookmarked());
            chap.setLastReadTime(oldChap.getLastReadTime());
            chap.setLastReadingPosition(oldChap.getLastReadingPosition());
            chap.setCompleted(oldChap.isCompleted());
            chap.setDownloaded(oldChap.isDownloaded());
        }

        return newChaps;
    }

    private static long generateId(String link, int position, String number, Chapter.Version version) {

        int result = 23;
        int hash = link.hashCode()
                + version.toString().hashCode()
                + position
                + number.hashCode();

        return 37 * result + hash;
    }
}
