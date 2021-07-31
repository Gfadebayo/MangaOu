package com.exzell.mangaplayground.io.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.exzell.mangaplayground.models.Chapter
import com.exzell.mangaplayground.models.Download
import com.exzell.mangaplayground.models.Manga
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


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
                            Migrations.MIGRATION_25_31, Migrations.MIGRATION_31_35, Migrations.MIGRATION_35_38,
                            Migrations.MIGRATION_38_43, Migrations.MIGRATION_43_52)
                    .build()
        }
    }
}
