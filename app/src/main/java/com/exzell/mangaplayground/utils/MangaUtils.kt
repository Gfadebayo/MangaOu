package com.exzell.mangaplayground.utils;

import android.util.Log;

import androidx.room.util.StringUtil;

import com.exzell.mangaplayground.BuildConfig;
import com.exzell.mangaplayground.advancedsearch.Genre;
import com.exzell.mangaplayground.advancedsearch.Type;
import com.exzell.mangaplayground.models.Manga;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import timber.log.Timber;

import static com.exzell.mangaplayground.BuildConfig.*;

public class MangaUtils {
    private static final String TAG = "MangaUtils";


    public static void addMangaDetails(Document mangaHtml, Manga manga){

        mangaHtml.body().select("script").remove();
        mangaHtml.body().select("header").remove();

        parseMangaSection(mangaHtml.body(), manga);

//        long id = ((manga.getThumbnailLink().hashCode() + manga.getLink().hashCode()) * 23);
//        manga.setId(id);
    }

    /**
     * Parses the section manga of the document which contains the title, img link, author name,
     * getRating, summary and popularity
     * @param body The Jsoup element of the html of the particular manga
     * @return An array of size 3 where index 0 is the name of the manga and 1 is the index of the thumbnail
     * and index 2 is the author name
     */
    private static void parseMangaSection(Element body, Manga manga){

        Element mangaClass = body.select("section[class=manga]").first();

        //fetch the name and thumbnail
        mangaClass.getElementsByTag("img").forEach(c -> {
            String thumbNail = c.attr("src");

            if(!thumbNail.startsWith("https:")) thumbNail = "https:" + thumbNail;
            manga.setThumbnailLink(thumbNail);

            String name = c.attr("title");
            Timber.i(name);
            manga.setTitle(correctTitle(name));
        });

        //fetch author
        mangaClass.getElementsByTag("th").forEach(c -> {
            if(c.text().contains("Author")) {
                String author = c.nextElementSibling().getElementsByTag("a").attr("title");
                manga.setAuthor(author);
                Timber.i("Author name is: %s", author);
            }

            if(c.text().contains("Artist")) {
                String artist = c.nextElementSibling().getElementsByTag("a").attr("title");
                if(artist != null && !artist.isEmpty()) manga.setArtist(artist);
                Timber.i("Artist name is: %s", artist);
            }

            if(c.text().contains("Rating")){
                String rating = c.nextElementSibling().text();
                Timber.i("Rating of:%s", rating);
                parseRatingAndVotes(manga, rating);
            }

            if(c.text().contains("Popularity")){
                String popularity = c.nextElementSibling().text();
                Timber.i("Popularity: %s", popularity);
                parsePopularityAndViews(manga, popularity);
            }

            if(c.text().contains("Type")){
                String type = c.nextElementSibling().text().split("-")[0].trim();
                Timber.i("Type: %s", type);
                Optional<Type> first = Stream.of(Type.values()).filter(type1 -> type1.dispName.equals(type)).findFirst();

//                first.ifPresent(g -> g.getDispName());
                first.ifPresent(type1 -> {
                    Timber.tag(TAG).w(type1.name());
                    manga.setType(type1);
                });
            }

            if(c.text().contains("Genre")){
                c.nextElementSibling().getElementsByTag("a").forEach(a -> {
                    Timber.i(a.text());
                    Optional<Genre> genre = Stream.of(Genre.values()).filter(genre1 -> genre1.
                            dispName.equals(a.text())).findFirst();

                    genre.ifPresent(manga::addGenres);
                });
            }

            if(c.text().contains("Release")){
                Timber.i(c.nextElementSibling().text());
                manga.setRelease(Integer.parseInt(c.nextElementSibling().text()));
            }

            if(c.text().contains("Status")){
                String status = c.nextElementSibling().text();
                Timber.i(status);
                manga.setStatus(status);
            }
        });

        //get summary
        String summary = mangaClass.getElementsByClass("summary").text();
        manga.setSummary(summary);
        if(DEBUG) Log.i(TAG, "Summary: " + summary);
    }

    /**
     * Creates the manga objects to be used in the search fragment
     * based on the information given from the html
     * @param html The object containing the html
     * @return a list of search mangas
     */
    public static List<Manga> createSearchManga(Element html){
        List<Manga> mangas = new ArrayList<>();

        html.getElementsByTag("table").forEach(c -> {

            Elements aTag = c.getElementsByClass("cover");

            if(aTag.size() == 0) return;
            Manga manga = new Manga();
            String link = aTag.attr("href");
            String title = aTag.attr("title");

            String thumbnail = aTag.first().getElementsByTag("img").attr("src");
            if(!thumbnail.startsWith("https://")) thumbnail = "https://" + thumbnail;

            String rate = c.getElementsByClass("rate").attr("title");

            manga.setTitle(correctTitle(title));
            manga.setLink(link);
            manga.setThumbnailLink(thumbnail);

            long id = ((thumbnail.hashCode() + link.hashCode()) * 23);
            manga.setId(id);
            parseRatingAndVotes(manga, rate);

            c.getElementsByTag("b").forEach(b -> {
                if(b.hasClass("rank")) {
                    String pop = b.text();
                    manga.setPopularity(pop);
                    Log.i(TAG, pop);
                }

                else if(b.text().contains("Authors/Artists")) {
                    String text = b.nextElementSibling().text();
                    Log.i(TAG, text);
                }

                else if(b.text().contains("Status")){
                    String status = b.nextElementSibling().text();
                    manga.setStatus(status);
                    Log.i(TAG, status);
                }

                else if(b.text().contains("Genre")){
                    Elements genreTag = b.nextElementSibling().parent().getElementsByTag("a");
                    String[] genres = genreTag.text().split("\\s+");
                    Stream.of(genres).forEach(gen -> {
                        Optional<Genre> genre = Stream.of(Genre.values()).filter(p -> p.dispName.equals(gen)).findFirst();
                        genre.ifPresent(manga::addGenres);
                    });
                }
            });

            mangas.add(manga);
        });

        return mangas;
    }

    private static void parseRatingAndVotes(Manga manga, String rate){
        //Format: Average x / 10 out of xxx votes.
        //split it by space, if the first character is a string, ignore it

        String[] spltRate = rate.split("/");
        String rat = Stream.of(spltRate[0].split("\\s+")).filter(s -> Character.isDigit(s.charAt(0)))
                .findFirst().get();

        String votes = Stream.of(spltRate[1].trim().split("\\s+")).skip(1).filter(s -> !s.isEmpty() && Character.isDigit(s.charAt(0))).findFirst().get();
//TODO: 10 / 10 out of 10 total votes gives an error...Fix it

        manga.setRating(Double.parseDouble(rat) * 0.5);
        manga.setVotes(Integer.parseInt(votes));

//        Log.i(TAG, "Rating of: " + getRating.get(0) + "Votes of: " + getRating.get(1));
    }

    private static void parsePopularityAndViews(Manga manga, String popu){
        //Format: xth, it has xx monthly views

        String[] split = popu.split(",");

        String popular = split[0].trim();
        String views = Stream.of(split[1].trim().split("\\s+")).filter(p -> Character.isDigit(p.charAt(0))).findFirst().get();

        manga.setPopularity(popular);
        manga.setViews(views);


        //TODO: its possible a manga has ? popularity which limits our array to just 1 value
    }

    private static String correctTitle(String mangaTitle){
        String trim = mangaTitle.trim();
        if(trim.charAt(0) == ':') return new StringBuilder(trim).deleteCharAt(0).toString();
        else return trim;
    }

    private static String parseMangaChapterLink(String chapterLink){
        int slashIndex = chapterLink.lastIndexOf("/");
        return new StringBuilder(chapterLink).deleteCharAt(slashIndex).toString();
    }
}
