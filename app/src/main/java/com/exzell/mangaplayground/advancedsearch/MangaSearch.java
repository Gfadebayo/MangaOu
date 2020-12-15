package com.exzell.mangaplayground.advancedsearch;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.exzell.mangaplayground.advancedsearch.Type;

public final class MangaSearch {

    public static final String STATUS_ONGOING = "ongoing";
    public static final String STATUS_COMPLETED = "completed";

    public static final String GENRE_AND = "and";
    public static final String GENRE_OR = "or";

    public static final String SEARCH_CONTAIN = "contains";
    public static final String SEARCH_END = "end";
    public static final String SEARCH_BEGIN = "begins";

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
    private int chapters;
    private String status;
    private String genreInclusion;
    private int rating;
    private Type type;
    private String titleContain;
    private String authorContain;
    private Order order;

    private MangaSearch(Builder build){
        genre = build.genre;
        this.release = build.release;
        this.title = build.title;
        this.author = build.author;
        this.chapters = build.chapters;
        this.status = build.status;
        this.genreInclusion = build.genreInclusion;
        this.authorContain = build.authorContain;
        this.titleContain = build.titleContain;
        this.type = build.type;
        this.rating = build.rating;
        this.order = build.order;
    }

    public Map<String, String> searchQuery(){

        Map<String, String> search = new HashMap<>(11);
        if(title != null) search.put(KEY_TITLE, title);
        if(titleContain != null) search.put(KEY_MATCH_TITLE, titleContain);
        if(author != null) search.put(KEY_AUTHOR, author);
        if(authorContain != null) search.put(KEY_MATCH_AUTHOR, authorContain);
        if(status != null) search.put(KEY_STATUS, status);
        if(chapters != -1) search.put(KEY_CHAPTERS, String.valueOf(chapters));
        if(rating != -1) search.put(KEY_RATING, String.valueOf(rating));
        if(release != -1) search.put(KEY_RELEASE, String.valueOf(release));
        if(type != null) search.put(KEY_TYPE, type.value);
        if(genre != null && !genre.isEmpty()) {
            StringBuilder build = new StringBuilder();
            genre.forEach(c -> {
                build.append(c.name).append(",");
            });
            if(build.toString().endsWith(",")) build.deleteCharAt(build.length()-1);
            search.put(KEY_GENRE, build.toString());
        }
        if(genreInclusion != null) search.put(KEY_GENRE_INCL, genreInclusion);
        if(order != null) search.put(KEY_ORDER, order.val);

        return search;
    }

    public static MangaSearch from(Map<String, String> query){
        Builder build = new Builder();
        if(query.containsKey(KEY_AUTHOR)) build.setAuthor(query.get(KEY_AUTHOR));
        if(query.containsKey(KEY_TITLE)) build.setTitle(query.get(KEY_TITLE));
        if(query.containsKey(KEY_RELEASE)) build.setRelease(Integer.parseInt(query.get(KEY_RELEASE)));
        if(query.containsKey(KEY_RATING)) build.setRating(Integer.valueOf(query.get(KEY_RATING)));
        if(query.containsKey(KEY_MATCH_AUTHOR)) build.setAuthorContain(query.get(KEY_MATCH_AUTHOR));
        if(query.containsKey(KEY_MATCH_TITLE)) build.setTitleContain(query.get(KEY_MATCH_TITLE));
        if(query.containsKey(KEY_CHAPTERS)) build.setChapters(Integer.parseInt(query.get(KEY_CHAPTERS)));
        if(query.containsKey(KEY_STATUS)) build.setStatus(query.get(KEY_STATUS));
        if(query.containsKey(KEY_GENRE_INCL)) build.setGenreInclusion(query.get(KEY_GENRE_INCL));

        if(query.containsKey(KEY_GENRE)) {
            Stream.of(query.get(KEY_GENRE).split(",")).forEach(s -> Stream.of(Genre.values()).forEach(c -> {
                if (c.name.equals(s)) build.addGenre(c.dispName);
            }));
        }

        if(query.containsKey(KEY_TYPE)) {
            Optional<String> first = Stream.of(Type.values()).filter(type -> type.value
                    .equals(query.get(KEY_ORDER))).map(type -> type.dispName).findFirst();

            build.setType(first.get());
        }

        if(query.containsKey(KEY_ORDER)){
            Optional<String> first = Stream.of(Order.values()).filter(order -> order.val
                    .equals(query.get(KEY_ORDER))).map(order -> order.dispName).findFirst();

            build.setOrder(first.orElse(null));
        }

        return new MangaSearch(build);
    }

    public static final class Builder{

        private List<Genre> genre = new ArrayList<>();
        private int release = -1;
        private String title;
        private String author;
        private int chapters = -1;
        private String status;
        private String genreInclusion;
        private Type type;
        private int rating = -1;
        private String titleContain;
        private String authorContain;
        private Order order;


        public Builder addGenre(String genreDispName){
            if(genreDispName != null && !genreDispName.isEmpty()) {
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
            if(release != -1) this.release = release;
            return this;
        }

        public Builder setType(String typeDispName){
            if(typeDispName != null && !typeDispName.isEmpty()) {
                Optional<Type> searchType = Stream.of(Type.values()).filter(type -> type.dispName.
                        equalsIgnoreCase(typeDispName)).findFirst();

                this.type = searchType.get();
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
            if(rating > -1) this.rating = rating;
            return this;
        }

        public Builder setTitle(String title){
            if(!title.isEmpty()) this.title = title;
            return this;
        }

        public Builder setAuthor(String author){
            if(!author.isEmpty()) this.author = author;
            return this;
        }

        public Builder setChapters(int chapters){
            if(chapters != -1) this.chapters = chapters;
            return this;
        }

        public Builder setStatus(String status){
            if(!status.isEmpty()) this.status = status;
            return this;
        }

        public Builder setGenreInclusion(String inclusion){
            if(!inclusion.isEmpty()) this.genreInclusion = inclusion;
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
