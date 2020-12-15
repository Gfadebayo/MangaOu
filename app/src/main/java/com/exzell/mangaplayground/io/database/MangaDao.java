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
    void insertMangas(List<Manga> mangas);

    @Delete
    void deleteMangas(List<Manga> mangas);

    @Update
    void updateMangas(List<Manga> mangas);

    @Transaction
    @Query("SELECT * FROM manga")
    List<DBManga> getMangas();

    @Transaction
    @Query("SELECT manga.* FROM manga, chapter ON chapter.downloaded=1")
    List<DBManga> getDownloadedManga();

    @Transaction
    @Query("SELECT manga.* FROM manga, chapter ON chapter.bookmarked=1")
    List<DBManga> getBookmarkedManga();

    @Transaction
    @Query("SELECT * FROM manga WHERE bookmark=1")
    LiveData<List<DBManga>> bookmarks();

    @Transaction
    @Query("SELECT * FROM manga WHERE bookmark=1")
    List<DBManga> notLiveBookmarks();

    @Transaction
    @Query("SELECT * FROM manga WHERE id IN (SELECT chapter.manga_id FROM chapter, download ON chapter.id=download.chapter_id AND download.state IS 'DOWNLOADED')")
    LiveData<List<DBManga>> downloads();

    @Query("SELECT * FROM manga WHERE title =:ti")
    Manga isPresent(String ti);

    @Query("DELETE FROM manga")
    void deleteAll();

    @Transaction
    @Query("SELECT DISTINCT manga.* FROM manga, chapter ON chapter.manga_id=manga.id AND chapter.last_read_time =:time")
    List<DBManga> getMangaLastChapter(long time);

    @Transaction
    @Query("SELECT * FROM manga WHERE id =:mangaId")
    DBManga getMangaFromId(long mangaId);

    @Transaction
    @Query("SELECT * FROM manga WHERE id =:mangaId")
    DBManga getLiveMangaFromId(long mangaId);

    @Transaction
    @Query("SELECT * FROM manga WHERE link =:link")
    DBManga getMangaFromLink(String link);

    @Transaction
    @Query("SELECT manga.* FROM manga, chapter ON manga.id=chapter.manga_id AND chapter.id =:id")
    DBManga getMangaFromChapter(long id);
}
