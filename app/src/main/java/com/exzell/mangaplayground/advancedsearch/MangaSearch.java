package com.exzell.mangaplayground.advancedsearch;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class MangaSearch {

    public static final int RELEASE_DATE_START = 1946;
    public static final int RELEASE_DATE_END = 2020;

    //Keys (search headers)
    private static final String KEY_RATING = "rating";
    private static final String KEY_TYPE = "types";
    private static final String KEY_RELEASE = "years";
    private static final String KEY_TITLE = "q";
    private static final String KEY_GENRE = "genres";
    private static final String KEY_AUTHOR = "autart";
    private static final String KEY_MATCH_AUTHOR = "autart-match";
    private static final String KEY_MATCH_TITLE = "name-match";
    private static final String KEY_GENRE_INCL = "genre-mode";
    private static final String KEY_CHAPTERS = "chapters";
    private static final String KEY_STATUS = "status";
    private static final String KEY_ORDER = "orderby";

    private List<Genre> genre = new ArrayList<>();
    private int release;
    private String title = "";
    private String author = "";
    private int chapterAmount;
    private String status = "";
    private String genreInclusion = "";
    private Type type;
    private int rating;
    private String titleContain = "";
    private String authorContain = "";
    private Order order;

    public static MangaSearch from(Map<String, String> query) {
        MangaSearch search = new MangaSearch();

        if (query.containsKey(KEY_AUTHOR)) search.author = (query.get(KEY_AUTHOR));

        if (query.containsKey(KEY_TITLE)) search.title = (query.get(KEY_TITLE));

        if (query.containsKey(KEY_RELEASE))
            search.release = (Integer.parseInt(query.get(KEY_RELEASE)));

        if (query.containsKey(KEY_RATING))
            search.rating = (Integer.parseInt(query.get(KEY_RATING)));

        if (query.containsKey(KEY_MATCH_AUTHOR))
            search.authorContain = (query.get(KEY_MATCH_AUTHOR));

        if (query.containsKey(KEY_MATCH_TITLE)) search.titleContain = (query.get(KEY_MATCH_TITLE));

        if (query.containsKey(KEY_CHAPTERS))
            search.chapterAmount = (Integer.parseInt(query.get(KEY_CHAPTERS)));

        if (query.containsKey(KEY_STATUS)) search.status = (query.get(KEY_STATUS));

        if (query.containsKey(KEY_GENRE_INCL)) search.genreInclusion = (query.get(KEY_GENRE_INCL));

        if (query.containsKey(KEY_GENRE)) {
            List<String> genresUsed = Arrays.asList(query.get(KEY_GENRE).split(","));
            Stream.of(Genre.values()).filter(genre -> genresUsed.contains(genre.name))
                    .forEach(search::addGenre);
        }

        if (query.containsKey(KEY_TYPE)) {
            search.type = Stream.of(Type.values()).filter(type -> type.value
                    .equals(query.get(KEY_ORDER))).findFirst().get();
        }

        if (query.containsKey(KEY_ORDER)) {
            search.order = Stream.of(Order.values()).filter(order -> order.val
                    .equals(query.get(KEY_ORDER))).findFirst().get();
        }

        return search;
    }

    public List<Genre> getGenre() {
        return genre;
    }

    public int getRelease() {
        return release;
    }

    public void setRelease(int release) {
        this.release = release;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getChapterAmount() {
        return chapterAmount;
    }

    public void setChapterAmount(int chapterAmount) {
        this.chapterAmount = chapterAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGenreInclusion() {
        return genreInclusion;
    }

    public void setGenreInclusion(String inclusion) {
        genreInclusion = inclusion;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getTitleContain() {
        return titleContain;
    }

    public void setTitleContain(String contain) {
        titleContain = contain;
    }

    public String getAuthorContain() {
        return authorContain;
    }

    public void setAuthorContain(String contain) {
        authorContain = contain;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void addGenre(Genre genre) {
        this.genre.add(genre);
    }

    public Map<String, String> searchQuery() {

        Map<String, String> search = new HashMap<>(11);
        if (title != null && !title.isEmpty()) search.put(KEY_TITLE, title);
        if (titleContain != null && !titleContain.isEmpty())
            search.put(KEY_MATCH_TITLE, titleContain);
        if (author != null && !author.isEmpty()) search.put(KEY_AUTHOR, author);
        if (authorContain != null && !authorContain.isEmpty())
            search.put(KEY_MATCH_AUTHOR, authorContain);
        if (status != null && !status.isEmpty()) search.put(KEY_STATUS, status);
        if (chapterAmount > 0) search.put(KEY_CHAPTERS, String.valueOf(chapterAmount));
        if (rating > 0) search.put(KEY_RATING, String.valueOf(rating));
        if (release > 0) search.put(KEY_RELEASE, String.valueOf(release));
        if (type != null) search.put(KEY_TYPE, type.value);
        if (order != null) search.put(KEY_ORDER, order.val);
        if (genre != null && !genre.isEmpty()) {
            StringBuilder build = new StringBuilder();
            genre.forEach(c -> {
                build.append(c.name).append(",");
            });
            if (build.toString().endsWith(",")) build.deleteCharAt(build.length() - 1);
            search.put(KEY_GENRE, build.toString());
        }
        if (genreInclusion != null) search.put(KEY_GENRE_INCL, genreInclusion);

        return search;
    }
}
