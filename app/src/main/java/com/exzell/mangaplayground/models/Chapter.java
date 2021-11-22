package com.exzell.mangaplayground.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.exzell.mangaplayground.io.database.MangaTypeConverter;

@Entity(tableName = "chapter",
        foreignKeys = @ForeignKey(entity = Manga.class,
                parentColumns = "id", childColumns = "manga_id",
                onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE),
        indices = {@Index(value = "manga_id", name = "chapter_manga_id_index"),
                @Index(value = {"link", "version", "manga_id", "number"}, name = "chapter_id_index", unique = true)})
public class Chapter {

    //More of a serial number actually
    //Its not a primary key, but is still needed for things like foreign key
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(defaultValue = "0")
    private float number = 0;

    @TypeConverters(MangaTypeConverter.class)
    private Version version = Version.VERSION_DUCK;

    @ColumnInfo(name = "release_date", defaultValue = "0")
    private long releaseDate;

    private String link = "";

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

    /**
     * The position of the chapter as gotten from the html doc
     */
    @ColumnInfo(defaultValue = "0")
    private int position;

    //For cases when the the chapter might be downloaded
    //so as to inform the user... nothing more
    @Ignore
    private Download.State downloadState;

    /**
     * The start position of the chapter page considering every single chapters in the manga
     */
    @Deprecated
    @Ignore
    private int offset = 0;

    public Chapter() {
    }

    @Ignore
    public Chapter(long mangaId) {
        this.mangaId = mangaId;
    }

    public long getId() {
        return id;
    }

    public void setId(long sn) {
        this.id = sn;
    }

    public long getMangaId() {
        return mangaId;
    }

    public void setMangaId(long mangaId) {
        this.mangaId = mangaId;
    }

    public float getNumber() {
        return number;
    }

    public void setNumber(float number) {
        this.number = number;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public long getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Long releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getLastReadingPosition() {
        return lastReadingPosition;
    }

    public void setLastReadingPosition(int lastReadingPosition) {
        this.lastReadingPosition = lastReadingPosition;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isNewChap() {
        return newChap;
    }

    public void setNewChap(boolean newChap) {
        this.newChap = newChap;
    }

    public long getLastReadTime() {
        return lastReadTime;
    }

    public void setLastReadTime(long lastReadTime) {
        this.lastReadTime = lastReadTime;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public int hashCode() {
        if (id != 0) return (int) id;
        else {
            int result = 23;
            float hash = link.hashCode()
                    + version.toString().hashCode()
                    + position
                    + number
                    + mangaId;
            return (int) (37 * result + hash);
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Chapter)) return false;

        Chapter o = ((Chapter) obj);

        return link.equals(o.link)
                && version.equals(o.version)
                && number == o.number
                && mangaId == o.mangaId;
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

    public enum Version {
        VERSION_FOX("Version Fox"),
        VERSION_MINI("Version Mini"),
        VERSION_ROCK("Version Rock"),
        VERSION_PANDA("Version Panda"),
        VERSION_DUCK("Version Duck");

        private String dispName;

        Version(String dispName) {
            this.dispName = dispName;
        }

        public String getDispName() {
            return dispName;
        }
    }
}
