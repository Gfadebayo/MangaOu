package com.exzell.mangaplayground.io.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.exzell.mangaplayground.models.Manga;

import java.util.List;

@Dao
public interface MangaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMangas(Manga mangas);

    @Delete
    void deleteMangas(List<Manga> mangas);

    @Update
    void updateMangas(List<Manga> mangas);

    @Query("UPDATE manga SET bookmark =:bookmark WHERE link =:link")
    void changeBookmark(int bookmark, String link);

    @Transaction
    @Query("SELECT * FROM manga")
    List<DBManga> getMangas();

    @Transaction
    @Query("SELECT * FROM manga WHERE bookmark=1")
    LiveData<List<DBManga>> bookmarks();

    @Transaction
    @Query("SELECT * FROM manga WHERE bookmark=1")
    List<DBManga> notLiveBookmarks();

    @Transaction
    @Query("SELECT * FROM manga WHERE id IN (SELECT chapter.manga_id FROM chapter, download ON chapter.id=download.chapter_id AND download.state IS 'DOWNLOADED')")
    LiveData<List<DBManga>> downloads();

    @Transaction
    @Query("SELECT DISTINCT manga.* FROM manga, chapter ON chapter.manga_id=manga.id AND chapter.last_read_time =:time")
    List<DBManga> getMangaLastChapter(long time);

    @Transaction
    @Query("SELECT * FROM manga WHERE id =:mangaId")
    DBManga getMangaFromId(long mangaId);

    @Transaction
    @Query("SELECT * FROM manga WHERE link =:link")
    DBManga getMangaFromLink(String link);

    @Transaction
    @Query("SELECT manga.* FROM manga, chapter ON manga.id=chapter.manga_id AND chapter.id =:id")
    DBManga getMangaFromChapter(long id);
}
