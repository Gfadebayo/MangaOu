package com.exzell.mangaplayground;

import com.exzell.mangaplayground.models.Chapter;
import com.exzell.mangaplayground.models.Manga;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;

public class DownloadFileUnitTest {

    private String getHtml(String pat){

        File path = new File(pat);
        StringBuilder build = new StringBuilder();
        try {
            FileReader reader = new FileReader(path);

            BufferedReader read = new BufferedReader(reader);
            String line;

            while((line = read.readLine()) != null){
                build.append(line);
            }

            read.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return build.toString();
    }

    @Test
    public void testDiownload(){
        Document doc = Jsoup.parse(getHtml("D:\\Cgpa Calculator\\download file.html"));

        Elements nextLink = doc.select("a[href*=minamoto-kun-monogatari]");
        Optional<Element> next = nextLink.stream().filter(p -> p.text().contains("Next")).findFirst();


        List<DataNode> downloadLink = doc.select("script").dataNodes();

        String link = downloadLink.stream().filter(p -> p.getWholeData().contains("_load_pages")).findFirst().get().getWholeData();
        parseDownloadLink(link);
//        assertEquals(1, var_load_pages.size());
    }

    @Test
    public void testPopularPage(){
        Document doc = Jsoup.parse(getHtml("D:\\Cgpa Calculator\\genre.html"));

        Elements mangaHtml = doc.select("a[class=cover]");

        mangaHtml.forEach(m -> {
            String mlink = m.attr("href");
            String title = m.attr("title");
            String thumbLink = m.select("img[src*=//file-thumb.mangapark.net/W300/]").attr("src");
            System.out.println(mlink + "\n" + title + "\n" + thumbLink);
        });

    }

    @Test
    public void testGenrePage(){
        Document doc = Jsoup.parse(getHtml("D:\\Cgpa Calculator\\genre.html"));

        Elements thumbnail = doc.select("img[src*=//file-thumb.mangapark.net/W300/]");

        thumbnail.forEach(c -> {
            System.out.println(c.attr("src") + "\n" + c.attr("alt"));
        });
    }

    private String parseDownloadLink(String link){
        String https = Arrays.stream(link.split("\"")).filter(p -> p.contains("https")).findFirst().get();
        StringBuilder build = new StringBuilder(https);
        for(int i = 0; i < build.length(); i++){
            if(build.charAt(i) == '\\') build.deleteCharAt(i);
        }

        return build.toString();
    }

    private static Observable<Chapter> createChapters(Element parent, Elements latestChapters, Manga manga, Chapter.Version version) {
        Elements allLinks = parent.select("a[class=ml-1 visited ch]");
        List<String> latestLinks = latestChapters.stream().map(element -> element.attr("href")).collect(Collectors.toList());
//        Elements allTitles = parent.select("div[class=d-none d-md-flex align-items-center ml-0 ml-md-1  txt]");
        String[] allTime = parent.select("span[class=time]").text().split("ago");
        Elements newChapter = parent.select("li[class*=d-flex py-1 item new]");

        List<String> newChapNum = Stream.of(newChapter.text().split(" 1 3 6 10 all of ")).filter(p -> p.startsWith("ch.")).collect(Collectors.toList());

        String[] chapLengths = parent.select("em").text().split("1 3 6 10 all of ");
        Elements li = parent.select("li[class*=d-flex py-1 item]");


        return Observable.range(0, allLinks.size()).map(i -> {
            Chapter chap = new Chapter(manga.getId());
            chap.setVersion(version);


            String chapterNumber = allLinks.get(i).text();
//            chap.setNumber(chapterNumber);
//            chap.setReleaseDate(allTime[i] + " ago");

            String link = allLinks.get(i).attr("href");
            chap.setLink(link);

            boolean isLatest = latestLinks.contains(link);
            chap.setNewChap(isLatest);

            String title = li.get(i).select("div[class=d-none d-md-flex align-items-center ml-0 ml-md-1  txt]").text();
            chap.setTitle(title);
            String length = chapLengths[i + 1].trim();
            chap.setLength(Integer.parseInt(length));


//            Map<Integer, String> titleRegexMatch = regexIt(title, null);
//            boolean hasTitle = titleRegexMatch.containsKey(3) && !titleRegexMatch.get(3).isEmpty();
//
//            Map<Integer, String> numberMatch = regexIt(chapterNumber);


//            String[] numberSplit = chapterNumber.split("(ch|Chapter)\\.?\\s*([\\d]+)");

            String[] titleSplit = title.split("(ch|Chapter)\\.?\\s*[\\d\\W]*:");

            if (titleSplit.length <= 1) {
                if (title.isEmpty()) {
                    title = chapterNumber.split("(ch|Chapter)\\.?\\s*[\\d\\W]*:")[1];
                }  //use title itself otherwise


            } else title = titleSplit[1];

            Pattern numberPattern = Pattern.compile("(ch|Chapter)\\.?\\s?([\\d\\.]+)");
            Matcher numberMatcher = numberPattern.matcher(chapterNumber);

            while (numberMatcher.find()) {
                int groupCount = numberMatcher.groupCount();
                String number = numberMatcher.group(groupCount);
                String ok = "";
            }
            return chap;
        });
    }

    public static Map<Integer, String> regexIt(String string, String sequence) {
        Map<Integer, String> groups = new HashMap<>();

        Pattern pattern = Pattern.compile(string);
        Matcher matcher = pattern.matcher("((ch\\.?|Chapter\\.?)\\s*(\\d\\.?)+:?\\s?)?([\\S\\s]*)");

        boolean matches = matcher.matches();

        if (matches) {
            String chapNumber = matcher.group(2);
            String title = matcher.group(3);

            groups.put(2, chapNumber);
            groups.put(3, title);
        }

        return groups;
    }

    //DONE
    @Test
    public void createChapterWithObservable() {

        Manga manga = new Manga();
        manga.setId(0x0002);


        Document doc = Jsoup.parse(getHtml("D:\\Manga - manga file.html"));
        Elements select = doc.body().select("div[id*=stream]");

        Elements latestChapters = doc.body().select("ul[class=lest]").first().select("a[class=visited ch]");
        Observable.range(0, select.size()).flatMap(integer -> Observable.just(select.get(integer))
//                .subscribeOn(Schedulers.newThread())
                .flatMap((Function<Element, ObservableSource<Chapter>>) ele -> {
                    String version = ele.select("span[class*=ml-1 stream-text]").text();

                    System.out.printf("***************%s*************\n\n", version);
                    Chapter.Version vers = Stream.of(Chapter.Version.values())
                            .filter(p -> version.equalsIgnoreCase(p.getDispName()))
                            .findFirst().get();

                    return createChapters(ele, latestChapters, manga, vers);
                }, 2))
                /*.doOnNext(c -> {
    System.out.println(c.getNumber());
}))*/.subscribe();
//
    }

    @Test
    public void testTime() {

        StringBuilder timeBuild = new StringBuilder("");
        long time = (System.currentTimeMillis());

        while (!timeBuild.toString().isEmpty() && !timeBuild.toString().trim().equals("ago")) {
            time = parseDate(timeBuild, time);
        }

        System.out.println("Hours is: " + time);

        String date = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(new Date(time));

        System.out.println(date);
    }

    private static long parseDate(StringBuilder time, long oldTime){

        int endIndex = 0;
        int startIndex;
        long multiplierToSecond = 1;

        if(time.toString().contains("second")){
            endIndex = time.indexOf("second");

        }else if(time.toString().contains("minute")){
            endIndex = time.indexOf("minute");
            multiplierToSecond = 60;

        }else if(time.toString().contains("hour")){
            endIndex = time.indexOf("hour");
            multiplierToSecond = 3600;

        }else if(time.toString().contains("day")){
            endIndex = time.indexOf("day");
            multiplierToSecond = 3600 * 24;

        }else if(time.toString().contains("week")){
            endIndex = time.indexOf("week");
            multiplierToSecond = 7 * 3600 * 24;

        }else if(time.toString().contains("month")){
            endIndex = time.indexOf("month");
            multiplierToSecond = 30 * 3600 * 24;

        }else if(time.toString().contains("year")){
            endIndex = time.indexOf("year");
            multiplierToSecond = 365 * 24 * 3600;
        }



        startIndex = time.lastIndexOf(" ", endIndex-2);
        startIndex = startIndex == -1 ? 0 : startIndex;
        String timeStr = time.substring(startIndex, endIndex).trim();
        int timeInSec = Character.isDigit(timeStr.charAt(0)) ? Integer.parseInt(timeStr) : 1;
        oldTime = oldTime - ((timeInSec * multiplierToSecond) * 1000);


        int nextSpaceIndex = time.indexOf(" ", endIndex);
        if(nextSpaceIndex != -1) time.delete(startIndex, nextSpaceIndex+1);
        else time.delete(startIndex, time.length());

        return oldTime;
    }

    @Test
    public void testPageForPosition(){
        Document doc = Jsoup.parse(getHtml("D:\\Cgpa Calculator\\Manga - manga file.html"));

        Elements allLinks = doc.select("a[class=ml-1 visited ch]");


    }
}
