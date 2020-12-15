package com.exzell.mangaplayground.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.exzell.mangaplayground.download.Download;
import com.exzell.mangaplayground.io.database.MangaTypeConverter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

@Entity(tableName = "chapter", foreignKeys = @ForeignKey(entity = Manga.class,
        parentColumns = "id", childColumns = "manga_id",
        onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE), indices = @Index(value = "manga_id", name = "chapter_manga_id_index"))
public class Chapter {

    @PrimaryKey
    private long id;

    private String number = "";

    @TypeConverters(MangaTypeConverter.class)
    private Version version = Version.VERSION_DUCK;

    @ColumnInfo(name = "release_date", defaultValue = "0")
    private long releaseDate;

    private String link = "";

    private boolean downloaded;

    private boolean bookmarked;

    private boolean completed;

    private int lastReadingPosition;

    private int length;

    private String title = "";

    @ColumnInfo(name = "last_read_time")
    private long lastReadTime = 0;

    @ColumnInfo(name = "new_chapter")
    private boolean newChap;

    @ColumnInfo(name = "manga_id")
    private long mangaId;

    /**The position of the chapter as gotten from the html doc*/
    @ColumnInfo(defaultValue = "0")
    private int position;

    //For cases when the the chapter might be downloaded
    //so as to inform the user... nothing more
    @Ignore
    private Download.State downloadState;

    /**The start position of the chapter page considering every single chapters in the manga*/
    @Ignore
    private int offset = 0;

    public Chapter(){}

    @Ignore
    public Chapter(long mangaId){
        this.mangaId = mangaId;
    }

    public void setId(long id){
        this.id = id;
    }

    public void setMangaId(long mangaId){this.mangaId = mangaId;}

    public void setNumber(String number) {
        this.number = number;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public void setReleaseDate(Long releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public void setBookmarked(boolean bookmarked){this.bookmarked = bookmarked;}

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setLastReadingPosition(int lastReadingPosition) {
        this.lastReadingPosition = lastReadingPosition;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setNewChap(boolean newChap) {
        this.newChap = newChap;
    }

    public void setLastReadTime(long lastReadTime) {
        this.lastReadTime = lastReadTime;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getId(){return id;}

    public long getMangaId(){return mangaId;}

    public String getNumber() {
        return number;
    }

    public Version getVersion() {
        return version;
    }

    public long getReleaseDate() {
        return releaseDate;
    }

    public String getLink() {
        return link;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getLastReadingPosition() {
        return lastReadingPosition;
    }

    public int getLength() {
        return length;
    }

    public String getTitle() {
        return title;
    }

    public boolean isNewChap() {
        return newChap;
    }

    public long getLastReadTime() {
        return lastReadTime;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public int hashCode() {
        return (int) id;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!(obj instanceof Chapter)) return false;

        Chapter o = ((Chapter) obj);

        return link.equals(o.link)
                && version.equals(o.version)
                && number.equals(o.number);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%s (%s)", number, version);
    }

    public Download.State getDownloadState() {
        return downloadState;
    }

    public void setDownloadState(Download.State downloadState) {
        this.downloadState = downloadState;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public enum Version{
        VERSION_FOX("Version Fox"),
        VERSION_MINI("Version Mini"),
        VERSION_ROCK("Version Rock"),
        VERSION_PANDA("Version Panda"),
        VERSION_DUCK("Version Duck");

        Version(String dispName){
            this.dispName = dispName;
        }

        public String getDispName() {
            return dispName;
        }

        private String dispName;
    }
}
