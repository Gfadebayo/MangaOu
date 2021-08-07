package com.exzell.mangaplayground.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.exzell.mangaplayground.advancedsearch.Genre;
import com.exzell.mangaplayground.advancedsearch.Type;
import com.exzell.mangaplayground.io.database.MangaTypeConverter;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "manga", indices = @Index(name = "manga_link_index", value = "link"))
public class Manga {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title = "";

    @ColumnInfo(name = "alt_title")
    @TypeConverters(MangaTypeConverter.class)
    private List<String> altTitle = new ArrayList<>();

    private String link = "";

    private String thumbnailLink = "";

    private boolean bookmark;

    private String author = "";

    private String artist = "";

    private String summary = "";

    private double rating;

    @TypeConverters(MangaTypeConverter.class)
    private List<Genre> genres = new ArrayList<>();

    private int votes;

    private String views = "";

    private String popularity = "";

    @TypeConverters(MangaTypeConverter.class)
    private Type type = Type.UNKNOWN;

    private String status = "";

    @ColumnInfo(name = "_release")
    private int release;

    @Ignore
    private List<Chapter> chapters = new ArrayList<>();

    public Manga(){}

    @Ignore
    public Manga(String link) {
        this.link = link;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAltTitle() {
        return altTitle;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setThumbnailLink(String thumbnailLink) {
        this.thumbnailLink = thumbnailLink;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setArtist(String artist){this.artist = artist;}

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setBookmark(boolean mark){this.bookmark = mark;}

    public void addGenres(Genre genre) {
        this.genres.add(genre);
    }

    public void setGenres(List<Genre> genres){
        this.genres = genres;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public void setPopularity(String popularity) {
        this.popularity = popularity;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRelease(int release) {
        this.release = release;
    }

    public void addChapter(Chapter chapter){
        this.chapters.add(chapter);
    }

    public void setChapters(List<Chapter> chapters){
        this.chapters = chapters;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setAltTitle(List<String> altTitle) {
        this.altTitle = altTitle;
    }

    public String getLink() {
        return link;
    }

    public String getThumbnailLink() {
        return thumbnailLink;
    }

    public String getAuthor() {
        return author;
    }

    public String getArtist(){ return artist; }

    public String getSummary() {
        return summary;
    }

    public double getRating() {
        return rating;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public int getVotes() {
        return votes;
    }

    public String getViews() {
        return views;
    }

    public String getPopularity() {
        return popularity;
    }

    public Type getType() {
        return type;
    }

    public boolean isBookmark() {
        return bookmark;
    }

    public String getStatus() {
        return status;
    }

    public int getRelease() {
        return release;
    }

    public List<Chapter> getChapters(){return chapters;}

    @Override
    public int hashCode() {
        int result = 23;
        int hash = link.hashCode()
                + author.hashCode()
                + title.hashCode();

        return 37 * result + hash;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!(obj instanceof Manga)) return false;

        Manga manObj = (Manga) obj;

        return this.title.equals(manObj.title) &&
                this.link.equals(manObj.link);
    }

    @NonNull
    @Override
    public String toString() {
        return title + " by " + author;
    }
}
