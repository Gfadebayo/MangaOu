package com.exzell.mangaplayground.io.database

import androidx.room.*
import com.exzell.mangaplayground.models.Chapter
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertChapters(chapters: List<Chapter>)

    @Update
    fun updateChapters(chapters: List<Chapter>)

    @Update(entity = Chapter::class)
    fun updateChaptersTime(chapters: List<ChapterTimeUpdate>)

    @Delete
    fun deleteChapters(chapters: List<Chapter>)

    @Query("DELETE FROM chapter")
    fun deleteAll()

    @Query("SELECT min(time) FROM (SELECT max(last_read_time) time FROM chapter GROUP BY manga_id HAVING last_read_time > 0)")
    fun allMangaTime(): Flow<Long>

    @Query("SELECT DISTINCT chapter.manga_id FROM chapter, download ON chapter.id = download.chapter_id AND download.state IS 'DOWNLOADED'")
    fun downloadedChapters(): List<Long>

    @Query("SELECT c.id id, c.number number, c.lastReadingPosition lastPosition, c.length length, c.last_read_time lastTime" +
            " FROM chapter c WHERE c.manga_id =:mangaId AND c.last_read_time>0 ORDER BY c.last_read_time DESC")
    fun allReadChaptersWithMangaId(mangaId: Long): Flow<List<ReadChaptersInfo>>
}