package com.exzell.mangaplayground.io.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

    const val CURRENT_VERSION: Int = 38

    @JvmField
    val MIGRATION_1_9 = object : Migration(1, 9) {
        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("CREATE TABLE chap (id INTEGER NOT NULL, number TEXT, version TEXT," +
                    "releaseDate TEXT, link TEXT, downloaded INTEGER NOT NULL, bookmarked INTEGER NOT NULL," +
                    "completed INTEGER NOT NULL, lastReadingPosition INTEGER NOT NULL, length INTEGER NOT NULL," +
                    "title TEXT, manga_id INTEGER NOT NULL, last_read_time INTEGER NOT NULL, new_chapter INTEGER NOT NULL, PRIMARY KEY(id)," +
                    " FOREIGN KEY(manga_id) REFERENCES manga(id) ON UPDATE CASCADE ON DELETE CASCADE)")

            database.execSQL("INSERT INTO chap(id, number, version, releaseDate, link, downloaded, bookmarked, completed, " + "lastReadingPosition, length, title, manga_id) SELECT * FROM chapter")

            database.execSQL("DROP TABLE chapter")

            database.execSQL("ALTER TABLE chap RENAME TO chapter")

            database.execSQL("CREATE TABLE man (id INTEGER NOT NULL, title TEXT, " +
                    "link TEXT, thumbnailLink TEXT, author TEXT, artist TEXT, summary TEXT, " +
                    " rating REAL NOT NULL, genres TEXT, votes INTEGER NOT NULL, views TEXT, " +
                    "popularity TEXT, type TEXT, status TEXT, _release INTEGER NOT NULL, " +
                    "bookmark INTEGER NOT NULL, PRIMARY KEY(id))")

            database.execSQL("INSERT INTO man SELECT * FROM manga")

            database.execSQL("DROP TABLE manga")

            database.execSQL("ALTER TABLE man RENAME TO manga")
        }
    }

    @JvmField
    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE download (id INTEGER NOT NULL, title TEXT, length INTEGER NOT NULL," +
                    "progress INTEGER NOT NULL, path TEXT, link TEXT, chapter_id INTEGER NOT NULL, state TEXT" +
                    ", chap_number TEXT, PRIMARY KEY(id), FOREIGN KEY(chapter_id) REFERENCES chapter(id) ON UPDATE CASCADE ON DELETE CASCADE)")
        }
    }

    @JvmField
    val MIGRATION_10_16 = object : Migration(10, 16) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE downloads (id INTEGER NOT NULL, title TEXT, length INTEGER NOT NULL," +
                    " path TEXT, link TEXT, chapter_id INTEGER NOT NULL, state TEXT" +
                    ", chap_number TEXT, PRIMARY KEY(id), FOREIGN KEY(chapter_id) REFERENCES chapter(id) ON UPDATE CASCADE ON DELETE CASCADE)")

            database.execSQL("INSERT INTO downloads SELECT id, title, length, path, link, chapter_id, state, chap_number FROM download")

            database.execSQL("DROP TABLE download")

            database.execSQL("ALTER TABLE downloads RENAME TO download")

            database.execSQL("CREATE INDEX chapter_manga_id_index ON chapter(manga_id)")

            database.execSQL("CREATE INDEX download_chapter_id_index ON download(chapter_id)")
        }
    }

    @JvmField
    val MIGRATION_16_21 = object : Migration(16, 21) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE chapters (id INTEGER NOT NULL PRIMARY KEY, number TEXT, " +
                    "version TEXT, release_date INTEGER NOT NULL DEFAULT 0, link TEXT, downloaded INTEGER NOT NULL, bookmarked INTEGER NOT NULL," +
                    " completed	INTEGER NOT NULL, lastReadingPosition INTEGER NOT NULL, length INTEGER NOT NULL, title TEXT," +
                    " last_read_time INTEGER NOT NULL, new_chapter INTEGER NOT NULL, manga_id INTEGER NOT NULL," +
                    " FOREIGN KEY(manga_id) REFERENCES manga(id) ON UPDATE CASCADE ON DELETE CASCADE)")

            database.execSQL("INSERT INTO chapters(id, number, version, downloaded, bookmarked, completed," +
                    " lastReadingPosition, length, title, last_read_time, new_chapter, manga_id) " +
                    "SELECT id, number, version, downloaded, bookmarked, completed, lastReadingPosition," +
                    " length, title, last_read_time, new_chapter, manga_id FROM chapter")

            database.execSQL("DROP TABLE chapter")

            database.execSQL("ALTER TABLE chapters RENAME TO chapter")

            database.execSQL("CREATE INDEX chapter_manga_id_index ON chapter(manga_id)")

            database.execSQL("CREATE INDEX manga_link_index ON manga(link)")
        }
    }

    @JvmField
    val MIGRATION_21_25 = object: Migration(21, 26) {
        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("CREATE TABLE down(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT, length INTEGER NOT NULL," +
                    " path TEXT, link TEXT, chapter_id INTEGER NOT NULL, state TEXT" +
                    ", chap_number TEXT, FOREIGN KEY(chapter_id) REFERENCES chapter(id) ON UPDATE CASCADE ON DELETE CASCADE)")

            database.execSQL("INSERT INTO down SELECT * FROM download")

            database.execSQL("DROP TABLE download")

            database.execSQL("ALTER TABLE down RENAME TO download")

            database.execSQL("CREATE INDEX download_chapter_id_index ON download(chapter_id)")
        }
    }

    @JvmField
    val MIGRATION_26_31 = object: Migration(26, 31) {
        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("CREATE TABLE down(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT, length INTEGER NOT NULL," +
                    " path TEXT, link TEXT, chapter_id INTEGER NOT NULL, state TEXT" +
                    ", chap_number TEXT, FOREIGN KEY(chapter_id) REFERENCES chapter(id) ON UPDATE CASCADE ON DELETE CASCADE)")

            database.execSQL("INSERT INTO down SELECT * FROM download")

            database.execSQL("DROP TABLE download")

            database.execSQL("ALTER TABLE down RENAME TO download")

            database.execSQL("CREATE INDEX download_chapter_id_index ON download(chapter_id)")
        }
    }

    @JvmField
    val MIGRATION_31_35 = object: Migration(31, 35) {
        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("CREATE TABLE chapters (id INTEGER NOT NULL PRIMARY KEY, number TEXT, " +
                    "version TEXT, release_date INTEGER NOT NULL DEFAULT 0, link TEXT, downloaded INTEGER NOT NULL, bookmarked INTEGER NOT NULL," +
                    " completed	INTEGER NOT NULL, lastReadingPosition INTEGER NOT NULL, length INTEGER NOT NULL, title TEXT," +
                    " last_read_time INTEGER NOT NULL, new_chapter INTEGER NOT NULL, position INTEGER NOT NULL DEFAULT 0, manga_id INTEGER NOT NULL," +
                    " FOREIGN KEY(manga_id) REFERENCES manga(id) ON UPDATE CASCADE ON DELETE CASCADE)")

            database.execSQL("INSERT INTO chapters(id, number, version, downloaded, bookmarked, completed," +
                    " lastReadingPosition, length, title, last_read_time, new_chapter, manga_id) " +
                    "SELECT id, number, version, downloaded, bookmarked, completed, lastReadingPosition," +
                    " length, title, last_read_time, new_chapter, manga_id FROM chapter")

            database.execSQL("DROP TABLE chapter")

            database.execSQL("ALTER TABLE chapters RENAME TO chapter")

            database.execSQL("CREATE INDEX chapter_manga_id_index ON chapter(manga_id)")

        }
    }

    @JvmField
    val MIGRATION_35_38 = object: Migration(35, 38) {
        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("CREATE TABLE man (id INTEGER NOT NULL, title TEXT, " +
                    "link TEXT, thumbnailLink TEXT, author TEXT, artist TEXT, summary TEXT, " +
                    " rating REAL NOT NULL, genres TEXT, votes INTEGER NOT NULL, views TEXT, " +
                    "popularity TEXT, type TEXT, status TEXT, _release INTEGER NOT NULL, " +
                    "bookmark INTEGER NOT NULL, PRIMARY KEY(id))")

            database.execSQL("INSERT INTO man SELECT * FROM manga")

            database.execSQL("DROP TABLE manga")

            database.execSQL("ALTER TABLE man RENAME TO manga")

            database.execSQL("CREATE INDEX manga_link_index ON manga(link)")
        }
    }
}
