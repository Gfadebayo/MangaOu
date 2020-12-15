package com.exzell.mangaplayground.io.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.exzell.mangaplayground.download.Download;

import java.util.List;

@Dao
public interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addDownloads(List<Download> d);

//    @Delete
    @Query("SELECT path FROM download WHERE state IS 'CANCELLED'")
    String deleteDownloads();

    @Update
    void updateDownloads(List<Download> d);

    @Query("SELECT * FROM download")
    LiveData<List<Download>> getAllDownloads();

    @Query("SELECT * FROM download WHERE state IS NOT 'DOWNLOADED'")
    LiveData<List<Download>> getPendingDownloadsLive();

    @Query("SELECT * FROM download WHERE state IS NOT 'DOWNLOADED'")
    List<Download> getPendingDownloads();

    @Query("SELECT path FROM download WHERE chapter_id =:id AND state = 'DOWNLOADED'")
    String getPathFromId(long id);
}
