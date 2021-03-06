package com.exzell.mangaplayground.io.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.exzell.mangaplayground.models.Chapter
import com.exzell.mangaplayground.models.Manga

/**
 * A manga class that goes straight to the point and only fetches members it needs
 */
@Entity
data class BookmarkInfo(val id: Long,
                        val title: String,
                        val link: String,
                        val thumbnailLink: String,
                        var bookmark: Boolean = true) {

}

data class HistoryInfo(val id: Long,
                       val title: String,
                       val link: String,
                       @ColumnInfo(name = "thumbnail_link") val thumbnailLink: String,
                       @ColumnInfo(name = "chapter_id") val chapterId: Long,
                       @ColumnInfo(name = "number") val lastChapterNumber: String,
                       @ColumnInfo(name = "last_read_time") val lastReadTime: Long)

fun BookmarkInfo.createManga() = Manga(link).apply {
    this.thumbnailLink = this@createManga.thumbnailLink
    title = this@createManga.title
    id = this@createManga.id
}

fun Manga.toBookmarkInfo() = BookmarkInfo(id, title, link, thumbnailLink)

/**
 * DBManga is used as it already caters for last chapter
 */
fun HistoryInfo.createManga() = DBManga().apply {
    title = this@createManga.title
    link = this@createManga.link
    thumbnailLink = this@createManga.thumbnailLink

    setDbChapter(listOf(Chapter().apply {
        id = this@createManga.chapterId
        lastReadTime = this@createManga.lastReadTime
        number = this@createManga.lastChapterNumber
    }))

}