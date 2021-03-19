package com.exzell.mangaplayground.utils

import com.exzell.mangaplayground.models.Chapter
import org.jsoup.nodes.DataNode
import org.jsoup.nodes.Document
import java.util.*

/**
 * Titles seem to start with a colon which is stressful to remove from the scraping
 * So its better to remove them directly here
 */
fun fixTitle(title: String): String {
    var trimmedTitle = title.trim { it <= ' ' }
    if (!trimmedTitle.isEmpty() && trimmedTitle[0] == ':') trimmedTitle = StringBuilder(trimmedTitle).deleteCharAt(0).toString().trim { it <= ' ' }
    return if (!trimmedTitle.isEmpty()) trimmedTitle else "No Title"
}

/**
 * This method is needed in order to split chapters joined together with a vol(for example vol 4 ch 35)
 * @param numbers
 * @return
 */
fun splitChapterNumbers(numbers: String): List<String> {
    val chapters: MutableList<String> = ArrayList(1000)
    var skipNextSpace = false
    var start = 0
    for (i in 0 until numbers.length) {
        val lastChar = i == numbers.length - 1
        if (lastChar) chapters.add(numbers.substring(start)) else if (numbers[i] == ' ' && !skipNextSpace) {
            chapters.add(numbers.substring(start, i))
            start = i + 1
        } else if (numbers[i] == ' ' && skipNextSpace) {
            skipNextSpace = false
        }
        if (numbers.substring(i).startsWith("vol.")) skipNextSpace = true
    }
    return chapters
}

fun fetchDownloadLink(doc: Document): String {
    val downloadLink = doc.select("script").dataNodes()
    val link = downloadLink.stream().filter { p: DataNode -> p.wholeData.contains("_load_pages") }.findFirst().get().wholeData
    return parseDownloadLink(link).trim { it <= ' ' }
}

private fun parseDownloadLink(link: String): String {
    val https = Arrays.stream(link.split("\"".toRegex()).toTypedArray()).filter { p: String -> p.contains("https") }.findFirst().orElse("")
    val build = StringBuilder(https)
    for (i in 0 until build.length) {
        if (build[i] == '\\') build.deleteCharAt(i)
    }
    return build.toString()
}

/**
 * Transfers the user defined fields value from similar chapters between the old and new
 * @return the merged chapters
 */
fun transferChapterInfo(newChaps: List<Chapter>, oldChaps: MutableList<Chapter>): List<Chapter> {
    if (oldChaps.isEmpty()) return newChaps

    oldChaps.sortedByDescending { it.position }

    //newChaps that are already in oldChaps
    val old = newChaps.filter {
        oldChaps.contains(it)
    }

    old.forEach {
        oldChaps.find { old: Chapter -> it == old }?.let { old: Chapter ->
            it.isBookmarked = old.isBookmarked
            it.lastReadTime = old.lastReadTime
            it.lastReadingPosition = old.lastReadingPosition
            it.isCompleted = old.isCompleted
            it.isDownloaded = old.isDownloaded
        }
    }
    return newChaps
}

fun generateId(link: String, position: Int, number: String, version: Chapter.Version): Long {
    val result = 23
    val hash = (link.hashCode()
            + version.toString().hashCode()
            + position
            + number.hashCode())
    return (37 * result + hash).toLong()
}