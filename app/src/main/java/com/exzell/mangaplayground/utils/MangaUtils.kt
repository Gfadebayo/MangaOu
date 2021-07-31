package com.exzell.mangaplayground.utils

import com.exzell.mangaplayground.models.Manga

fun parseRatingAndVotes(manga: Manga, rate: String) {
    //Format: Average x / 10 out of xxx votes.
    //split it by space, if the first character is a string, ignore it
    val rateAndVote = rate.split("/".toRegex())

    val rat = rateAndVote[0].split("\\s+".toRegex()).first { it[0].isDigit() }

    val votes = rateAndVote[1].trim().split("\\s+".toRegex()).drop(1).first { it.isNotEmpty() && it[0].isDigit() }

    manga.rating = rat.toDouble() * 0.5
    manga.votes = votes.toInt()
}

fun parsePopularityAndViews(manga: Manga, popu: String) {
    //Format: xth, it has xx monthly views
    val popuAndViews = popu.split(",".toRegex())

    val popular = popuAndViews[0].trim()

    val views = popuAndViews[1].trim().split("\\s+".toRegex()).first { it[0].isDigit() }

    manga.popularity = popular
    manga.views = views


    //TODO: its possible a manga has ? popularity which limits our array to just 1 value
}

fun correctTitle(mangaTitle: String): String {
    return mangaTitle.trim().also {
        if (it[0] == ':') it.removeRange(0, 1)
    }
}

fun correctThumbnailLink(link: String): String {
    return link.trim().let { thumb: String ->
        if (!thumb.startsWith("https:")) "https:$thumb"
        else thumb
    }
}

private fun parseMangaChapterLink(chapterLink: String): String {
    val slashIndex = chapterLink.lastIndexOf("/")
    return StringBuilder(chapterLink).deleteCharAt(slashIndex).toString()
}

fun Manga.transferInfo(to: Manga, andChapters: Boolean = true) {
    to.id = id
    to.isBookmark = isBookmark

    if (andChapters)
        to.chapters = transferChapterInfo(to.chapters.onEach { it.mangaId = id }, chapters)
}