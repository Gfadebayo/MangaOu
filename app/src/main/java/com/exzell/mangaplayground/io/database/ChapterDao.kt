package com.exzell.mangaplayground.io.database

import androidx.room.*
import com.exzell.mangaplayground.models.Chapter
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChapters(chapters: List<Chapter>)

    @Update
    fun updateChapters(chapters: List<Chapter>)

    @Update(entity = Chapter::class)
    fun updateChaptersTime(chapters: List<ChapterTimeUpdate>)

    @Delete
    fun deleteChapters(chapters: List<Chapter>)

    @Query("DELETE FROM chapter")
    fun deleteAll()

    @Query("SELECT max(last_read_time) FROM chapter GROUP BY manga_id HAVING last_read_time > 0 ORDER BY last_read_time DESC")
    fun allMangaTime(): Flow<List<Long>>

    @Query("SELECT DISTINCT chapter.manga_id FROM chapter, download ON chapter.id = download.chapter_id AND download.state IS 'DOWNLOADED'")
    fun downloadedChapters(): List<Long>
}