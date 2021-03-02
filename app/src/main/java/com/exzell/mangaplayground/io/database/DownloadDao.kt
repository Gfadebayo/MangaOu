package com.exzell.mangaplayground.io.database

import androidx.room.*
import com.exzell.mangaplayground.download.Download
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addDownloads(d: List<Download>)

    //    @Delete
    @Query("SELECT path FROM download WHERE state IS 'CANCELLED'")
    fun deleteDownloads(): String

    @Update
    fun updateDownloads(d: List<Download>)

    @Query("SELECT * FROM download")
    fun getAllDownloads(): Flow<List<Download>>

    @Query("SELECT * FROM download WHERE state IS NOT 'DOWNLOADED'")
    fun getPendingDownloadsLive(): Flow<List<Download>>

    @Query("SELECT * FROM download WHERE state IS NOT 'DOWNLOADED'")
    fun getPendingDownloads(): List<Download>

    @Query("SELECT path FROM download WHERE chapter_id =:id AND state = 'DOWNLOADED'")
    fun getPathFromId(id: Long): String
}