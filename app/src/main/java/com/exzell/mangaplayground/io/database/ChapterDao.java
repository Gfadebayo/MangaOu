package com.exzell.mangaplayground.io.database;

import androidx.lifecycle.LiveData;
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

    @Query("SELECT max(last_read_time) FROM chapter GROUP BY manga_id HAVING last_read_time > 0 ORDER BY last_read_time DESC")
    LiveData<List<Long>> allMangaTime();

    @Query("SELECT DISTINCT chapter.manga_id FROM chapter, download ON chapter.id = download.chapter_id AND download.state IS 'DOWNLOADED'")
    List<Long> downloadedChapters();
}
