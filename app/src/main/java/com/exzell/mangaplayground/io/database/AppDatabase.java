package com.exzell.mangaplayground.io.database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Database;

import com.exzell.mangaplayground.download.Download;
import com.exzell.mangaplayground.models.Chapter;
import com.exzell.mangaplayground.models.Manga;


@Database(version = 35, entities = {Chapter.class, Manga.class, Download.class}, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DB_NAME = "MangaDatabase";

    public static AppDatabase getDatabase(Context context){
        return Room.databaseBuilder(context, AppDatabase.class, DB_NAME)
                .addMigrations(Migrations.MIGRATION_1_9, Migrations.MIGRATION_9_10,
                        Migrations.MIGRATION_10_16, Migrations.MIGRATION_16_21, Migrations.MIGRATION_21_25,
                        Migrations.MIGRATION_26_31, Migrations.MIGRATION_31_35)
                .build();
    }

    public abstract ChapterDao getChapterDao();

    public abstract MangaDao getMangaDao();

    public abstract DownloadDao getDownloadDao();
}
