package com.exzell.mangaplayground.advancedsearch;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class MangaSearch {

    public static final String STATUS_ONGOING = "ongoing";
    public static final String STATUS_COMPLETED = "completed";

    public static final String GENRE_AND = "and";
    public static final String GENRE_OR = "or";

    public static final String SEARCH_CONTAIN = "contains";
    public static final String SEARCH_END = "end";
    public static final String SEARCH_BEGIN = "begins";

    public static final int[] RELEASE_POINTS = {1946, 2017};

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
    private static final String KEY_ORDER ="orderby";


    private List<Genre> genre;
    private int release;
    private String title;
    private String author;
    private int chapterAmount;
    private String status;
    private String genreInclusion;
    private int rating;
    private Type type;
    private String titleContain;
    private String authorContain;
    private Order order;

    private MangaSearch(Builder build) {
        if (!build.genre.isEmpty()) genre = build.genre;
        if (build.release > 0) this.release = build.release;
        if (!build.title.isEmpty()) this.title = build.title;
        if (!build.author.isEmpty()) this.author = build.author;
        if (build.chapterAmount > 0) this.chapterAmount = build.chapterAmount;
        if (!build.status.isEmpty()) this.status = build.status;
        if (!build.genre.isEmpty()) this.genreInclusion = build.genreInclusion;
        if (!build.authorContain.isEmpty()) this.authorContain = build.authorContain;
        if (!build.titleContain.isEmpty()) this.titleContain = build.titleContain;
        if (build.type != null) this.type = build.type;
        if (build.rating > 0) this.rating = build.rating;
        if (build.order != null) this.order = build.order;
    }

    public static MangaSearch from(Map<String, String> query) {
        Builder build = new Builder();
        if (query.containsKey(KEY_AUTHOR)) build.setAuthor(query.get(KEY_AUTHOR));
        if (query.containsKey(KEY_TITLE)) build.setTitle(query.get(KEY_TITLE));
        if (query.containsKey(KEY_RELEASE))
            build.setRelease(Integer.parseInt(query.get(KEY_RELEASE)));
        if (query.containsKey(KEY_RATING)) build.setRating(Integer.valueOf(query.get(KEY_RATING)));
        if (query.containsKey(KEY_MATCH_AUTHOR))
            build.setAuthorContain(query.get(KEY_MATCH_AUTHOR));
        if (query.containsKey(KEY_MATCH_TITLE)) build.setTitleContain(query.get(KEY_MATCH_TITLE));
        if (query.containsKey(KEY_CHAPTERS))
            build.setChapterAmount(Integer.parseInt(query.get(KEY_CHAPTERS)));
        if (query.containsKey(KEY_STATUS)) build.setStatus(query.get(KEY_STATUS));
        if (query.containsKey(KEY_GENRE_INCL)) build.setGenreInclusion(query.get(KEY_GENRE_INCL));

        if (query.containsKey(KEY_GENRE)) {
            Stream.of(query.get(KEY_GENRE).split(",")).forEach(s -> Stream.of(Genre.values()).forEach(c -> {
                if (c.name.equals(s)) build.addGenre(c.dispName);
            }));
        }

        if (query.containsKey(KEY_TYPE)) {
            Optional<String> first = Stream.of(Type.values()).filter(type -> type.value
                    .equals(query.get(KEY_ORDER))).map(type -> type.dispName).findFirst();

            build.setType(first.get());
        }

        if (query.containsKey(KEY_ORDER)) {
            Optional<String> first = Stream.of(Order.values()).filter(order -> order.val
                    .equals(query.get(KEY_ORDER))).map(order -> order.dispName).findFirst();

            build.setOrder(first.orElse(null));
        }

        return new MangaSearch(build);
    }

    public List<Genre> getGenre() {
        return genre;
    }

    public int getRelease() {
        return release;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getChapterAmount() {
        return chapterAmount;
    }

    public String getStatus() {
        return status;
    }

    public String getGenreInclusion() {
        return genreInclusion;
    }

    public int getRating() {
        return rating;
    }

    public Type getType() {
        return type;
    }

    public String getTitleContain() {
        return titleContain;
    }

    public String getAuthorContain() {
        return authorContain;
    }

    public Order getOrder() {
        return order;
    }

    public Map<String, String> searchQuery() {

        Map<String, String> search = new HashMap<>(11);
        if (title != null) search.put(KEY_TITLE, title);
        if (titleContain != null) search.put(KEY_MATCH_TITLE, titleContain);
        if (author != null) search.put(KEY_AUTHOR, author);
        if (authorContain != null) search.put(KEY_MATCH_AUTHOR, authorContain);
        if (status != null) search.put(KEY_STATUS, status);
        if (chapterAmount > 0) search.put(KEY_CHAPTERS, String.valueOf(chapterAmount));
        if (rating > 0) search.put(KEY_RATING, String.valueOf(rating));
        if (release > 0) search.put(KEY_RELEASE, String.valueOf(release));
        if (type != null) search.put(KEY_TYPE, type.value);
        if (genre != null && !genre.isEmpty()) {
            StringBuilder build = new StringBuilder();
            genre.forEach(c -> {
                build.append(c.name).append(",");
            });
            if (build.toString().endsWith(",")) build.deleteCharAt(build.length() - 1);
            search.put(KEY_GENRE, build.toString());
        }
        if (genreInclusion != null) search.put(KEY_GENRE_INCL, genreInclusion);
        if(order != null) search.put(KEY_ORDER, order.val);

        return search;
    }

    public static final class Builder {

        private List<Genre> genre = new ArrayList<>();
        private int release = -1;
        private String title = "";
        private String author = "";
        private int chapterAmount = -1;
        private String status = "";
        private String genreInclusion = "";
        private Type type = Type.UNKNOWN;
        private int rating = -1;
        private String titleContain = "";
        private String authorContain = "";
        private Order order;


        public Builder addGenre(String genreDispName) {
            if (genreDispName != null && !genreDispName.isEmpty()) {
                Optional<Genre> searchType = Stream.of(Genre.values()).filter(genre -> genre.dispName.
                        equalsIgnoreCase(genreDispName)).findFirst();

                this.genre.add(searchType.get());
            }

            return this;
        }

        public Builder addGenres(List<String> genreNames){
            genreNames.forEach(this::addGenre);
            return this;
        }

        public Builder setRelease(int release){
            if (release > 0) this.release = release;
            return this;
        }

        public Builder setType(String typeDispName){
            if(typeDispName != null && !typeDispName.isEmpty()) {
                Optional<Type> searchType = Stream.of(Type.values()).filter(type -> type.dispName.
                        equalsIgnoreCase(typeDispName)).findFirst();

                searchType.ifPresent(t -> type = t);
            }
            return this;
        }

        public Builder setOrder(String orderDispName){
            if(orderDispName != null && !orderDispName.isEmpty()) {
                Optional<Order> searchType = Stream.of(Order.values()).filter(order -> order.dispName.
                        equalsIgnoreCase(orderDispName)).findFirst();

                order = searchType.orElse(null);
            }
            return this;
        }

        public Builder setRating(int rating){
            if (rating > 0) this.rating = rating;
            return this;
        }

        public Builder setTitle(String title) {
            if (!title.isEmpty()) this.title = title;
            return this;
        }

        public Builder setAuthor(String author) {
            if (!author.isEmpty()) this.author = author;
            return this;
        }

        public Builder setChapterAmount(int chapterAmount) {
            if (chapterAmount > 0) this.chapterAmount = chapterAmount;
            return this;
        }

        public Builder setStatus(String status) {
            if (!status.isEmpty()) this.status = status;
            return this;
        }

        public Builder setGenreInclusion(String inclusion) {
            if (!inclusion.isEmpty()) this.genreInclusion = inclusion;
            return this;
        }

        public Builder setTitleContain(String contain){
            if(!contain.isEmpty()) this.titleContain = contain;
            return this;
        }

        public Builder setAuthorContain(String contain){
            if(!contain.isEmpty()) this.authorContain = contain;
            return this;
        }

        public MangaSearch build(){
            return new MangaSearch(this);
        }
    }
}
