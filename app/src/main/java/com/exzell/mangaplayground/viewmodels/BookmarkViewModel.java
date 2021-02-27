package com.exzell.mangaplayground.viewmodels;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.io.Repository;
import com.exzell.mangaplayground.io.database.DBManga;
import com.exzell.mangaplayground.models.Chapter;
import com.exzell.mangaplayground.models.Manga;
import com.exzell.mangaplayground.utils.DateUtilsKt;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class BookmarkViewModel extends AndroidViewModel {

    @Inject
    Repository mRepo;

    private Context mContext;

    public BookmarkViewModel(@NonNull Application application) {
        super(application);
        mContext = application.getApplicationContext();
    }

    /**
     * Bookmarks gives either the bookmarked(true) mangas or downloaded(false) mangas.
     */
    public LiveData<List<DBManga>> getBookmarks(boolean forBookmarks){
        return forBookmarks ? mRepo.getBookmarkedManga() : mRepo.getDownloadedMangas();
    }

    public LiveData<List<Long>> getTimes(){
        return mRepo.allMangaTime();
    }

    public String getDayTitle(int day){
        if(day == 0) return mContext.getString(R.string.today);
        else if(day == 1) return mContext.getString(R.string.yesterday);
        else if(day < 30){
            return day + " " + mContext.getString(R.string.days_ago);
        }else{
            Calendar todayExact = DateUtilsKt.reset(Calendar.getInstance(), null);
            todayExact.add(Calendar.DAY_OF_MONTH, day * -1);

            return SimpleDateFormat.getDateInstance().format(todayExact.getTime());
        }
    }

    public void deleteBookmarks(List<DBManga> mangas){
        mangas.forEach(m -> m.setBookmark(false));

        mRepo.updateManga(false, mangas.toArray(new Manga[0]));
    }

    public List<DBManga> getHistoryManga(long time){
        return mRepo.lastReadMangas(time);
    }

    public void removeFromHistory(Chapter lastChapter) {
        lastChapter.setLastReadTime(0);
        mRepo.updateChapters(Collections.singletonList(lastChapter));
    }
}
