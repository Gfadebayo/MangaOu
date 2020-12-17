package com.exzell.mangaplayground.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.exzell.mangaplayground.UpdateService;
import com.exzell.mangaplayground.io.Repository;
import com.exzell.mangaplayground.io.database.DBManga;
import com.exzell.mangaplayground.models.Chapter;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public List<Long> getTimes(){
        return mRepo.allDate();
    }

    public List<String> getAllTime(){
        List<String> times = mRepo.allDate().stream().map(aLong -> {
            Calendar inst = Calendar.getInstance();
            inst.setTimeInMillis(aLong);
            return getDayTitle((int) Math.abs(System.currentTimeMillis() - inst.getTimeInMillis()));
//                Calendar.getInstance().
        }).collect(Collectors.toList());

        return times;
    }

    public String getDayTitle(int day){
        if(day == 0) return "Today";
        else if(day == 1) return "Yesterday";
        else{
            return day + "days ago";
        }
    }

    public List<DBManga> getHistoryManga(long time){
        return mRepo.lastReadMangas(time);
    }

    public void startUpdating(){
        Intent bookmarkIntent = new Intent(mContext, UpdateService.class);
        mContext.startService(bookmarkIntent);
    }

    public void removeFromHistory(Chapter lastChapter) {
        mRepo.resetTime(lastChapter);
    }
}
