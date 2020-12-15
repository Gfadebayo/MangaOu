package com.exzell.mangaplayground.io.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.exzell.mangaplayground.models.Chapter;

import java.util.List;

@Dao
public interface ChapterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChapters(List<Chapter> chapters);

    @Update
    void updateChapters(List<Chapter> chapters);

    @Delete
    void deleteChapters(List<Chapter> chapters);

    @Query("DELETE FROM chapter")
    void deleteAll();

    @Query("SELECT DISTINCT last_read_time FROM chapter WHERE lastReadingPosition > 0 ORDER BY last_read_time DESC")
    List<Long> allTime();

    @Query("SELECT * FROM chapter WHERE last_read_time > 0")
    List<Chapter> historyChapters();

    @Query("SELECT DISTINCT chapter.manga_id FROM chapter, download ON chapter.id = download.chapter_id AND download.state IS 'DOWNLOADED'")
    List<Long> downloadedChapters();

    @Query("UPDATE chapter SET last_read_time = 0 WHERE id =:id")
    void resetTime(long id);
}
