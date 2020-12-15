package com.exzell.mangaplayground.download;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.exzell.mangaplayground.io.database.MangaTypeConverter;
import com.exzell.mangaplayground.models.Chapter;

@Entity(tableName = "download", foreignKeys = @ForeignKey(parentColumns = "id",
        childColumns = "chapter_id", onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE, entity = Chapter.class),
        indices = @Index(value = "chapter_id", name = "download_chapter_id_index"))
public class Download {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;

    private int length;

    @Ignore
    private int progress;

    private String path;

    private String link;

    @ColumnInfo(name = "chap_number")
    private String chapNumber;

    @ColumnInfo(name = "chapter_id")
    private long chapterId;

    @TypeConverters(MangaTypeConverter.class)
    private State state = State.QUEUED;

    public Download(){}

    @Ignore
    public Download(long chapId, String title, String chNum, String path, String link,  int length) {
        this.chapterId = chapId;
        this.title = title;
        this.chapNumber = chNum;
        this.path = path;
        this.link = link;
        this.length = length;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getChapterId() {
        return chapterId;
    }

    public void setChapterId(long chapterId) {
        this.chapterId = chapterId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getChapNumber() {
        return chapNumber;
    }

    public void setChapNumber(String chapNumber) {
        this.chapNumber = chapNumber;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null) return false;
        else if(!(obj instanceof Download)) return false;

        Download o = (Download) obj;

        return this.title.equals(o.title) && this.chapNumber.equals(o.chapNumber)
                && this.path.equals(o.path)
                && this.chapterId == o.chapterId && this.length == o.length;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        int mul = title.hashCode() + chapNumber.hashCode() + path.hashCode() + (int) (chapterId ^ (chapterId >>> 32)) + length;

        return 37 * (hash + mul);
    }

    public enum State{
        DOWNLOADED,
        DOWNLOADING,
        PAUSED,
        QUEUED,
        CANCELLED,
        ERROR;
    }
}
