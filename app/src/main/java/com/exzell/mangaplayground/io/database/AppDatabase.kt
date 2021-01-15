package com.exzell.mangaplayground.io.database

import android.content.Context

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Database

import com.exzell.mangaplayground.download.Download
import com.exzell.mangaplayground.models.Chapter
import com.exzell.mangaplayground.models.Manga

import javax.inject.Singleton

import dagger.Module
import dagger.Provides


@Database(version = Migrations.CURRENT_VERSION, entities = [Chapter::class, Manga::class, Download::class], exportSchema = false)
@Module
abstract class AppDatabase : RoomDatabase() {

    abstract val chapterDao: ChapterDao

    abstract val mangaDao: MangaDao

    abstract val downloadDao: DownloadDao

    companion object {

        val DB_NAME = "MangaDatabase"

        @Singleton
        @Provides
        fun getDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                    .addMigrations(Migrations.MIGRATION_1_9, Migrations.MIGRATION_9_10,
                            Migrations.MIGRATION_10_16, Migrations.MIGRATION_16_21, Migrations.MIGRATION_21_25,
                            Migrations.MIGRATION_26_31, Migrations.MIGRATION_31_35, Migrations.MIGRATION_35_38)
                    .build()
        }
    }
}
