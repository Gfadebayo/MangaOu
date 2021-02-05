package com.exzell.mangaplayground.io.database;

import androidx.room.Ignore;
import androidx.room.Relation;

import com.exzell.mangaplayground.models.Chapter;
import com.exzell.mangaplayground.models.Manga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class DBManga extends Manga {

    @Relation(parentColumn = "id", entityColumn = "manga_id")
    private List<Chapter> dbChapter;

    @Override
    public List<Chapter> getChapters() {
        return dbChapter;
    }


    public void setDbChapter(List<Chapter> dbChapter) {
        this.dbChapter = dbChapter;
    }

    public Chapter getLastChapter(){
        return dbChapter.stream().max((o1, o2) -> Long.compare(o1.getLastReadTime(), o2.getLastReadTime())).get();
    }

    public long getLastReadTime(){
        return getLastChapter().getLastReadTime();
    }
}
