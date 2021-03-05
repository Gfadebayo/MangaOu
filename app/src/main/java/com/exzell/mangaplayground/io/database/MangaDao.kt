package com.exzell.mangaplayground.io.database

import androidx.room.*
import com.exzell.mangaplayground.models.Manga
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMangas(mangas: Manga): Long

    @Delete
    fun deleteMangas(mangas: List<Manga>)

    @Update
    fun updateMangas(mangas: List<Manga>)

    @Query("SELECT * FROM manga")
    @Transaction
    fun getMangas(): List<DBManga>

    @Query("SELECT id, title, link, thumbnailLink FROM manga WHERE bookmark=1")
    fun bookmarks(): Flow<List<BookmarkInfo>>

    @Transaction
    @Query("SELECT * FROM manga WHERE bookmark=1")
    fun notLiveBookmarks(): List<DBManga>

    @Query("SELECT link, id, title, thumbnailLink FROM manga WHERE id IN (SELECT chapter.manga_id FROM chapter, download ON chapter.id=download.chapter_id AND download.state IS 'DOWNLOADED')")
    fun downloads(): Flow<List<BookmarkInfo>>

    @Transaction
    @Query("SELECT * FROM manga WHERE id =:mangaId")
    fun getMangaFromId(mangaId: Long): DBManga

    @Transaction
    @Query("SELECT * FROM manga WHERE link =:link")
    fun getMangaFromLink(link: String): DBManga

    @Transaction
    @Query("SELECT manga.* FROM manga, chapter ON manga.id=chapter.manga_id AND chapter.id =:chapter_id")
    fun getMangaFromChapter(chapter_id: Long): DBManga

    @Transaction
    @Query("SELECT DISTINCT manga.* FROM manga, chapter ON manga.id=chapter.manga_id AND chapter.last_read_time >=:read_time ORDER BY chapter.last_read_time DESC")
    fun getMangaFromChapterTime(read_time: Long): List<DBManga>

    @Query("SELECT m.id id, m.title title, m.link link, m.thumbnailLink thumbnail_link," +
            " c.id chapter_id, c.number number, c.last_read_time last_read_time FROM manga m" +
            " INNER JOIN chapter c ON m.id=c.manga_id AND c.last_read_time >=:read_time" +
            " ORDER BY c.last_read_time DESC")
    fun lastReadInfo(read_time: Long): List<HistoryInfo>
}