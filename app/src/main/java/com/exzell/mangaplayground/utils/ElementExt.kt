package com.exzell.mangaplayground.utils

import com.exzell.mangaplayground.advancedsearch.Genre
import com.exzell.mangaplayground.advancedsearch.Type
import com.exzell.mangaplayground.models.Chapter
import com.exzell.mangaplayground.models.Manga
import io.reactivex.rxjava3.core.Observable
import org.jsoup.nodes.Element
import timber.log.Timber
import java.util.regex.Pattern

/**
 * Removes some unneeded tags first, then calls [parseMangaSection]
 * which parses the section tag of the document where every required info is.
 * @param manga The manga that should be have it's data updated from the results gotten
 */
fun Element.createManga(manga: Manga) {
    select("script").remove()
    select("header").remove()
    parseMangaSection(this, manga)
}

private fun parseMangaSection(body: Element, manga: Manga) {
    val mangaClass = body.select("section[class=manga]").first()

    //fetch the name and thumbnail
    mangaClass.getElementsByTag("img").forEach {

        manga.thumbnailLink = correctThumbnailLink(it.attr("src"))

        val name = it.attr("title")
        manga.title = correctTitle(name)
    }

    //fetch author
    mangaClass.getElementsByTag("th").forEach {
        if (it.text().contains("Author")) {
            manga.author = it.nextElementSibling().getElementsByTag("a").attr("title").capitalize()
        }

        if (it.text().contains("Artist")) {
            val artist = it.nextElementSibling().getElementsByTag("a").attr("title")
            if (artist != null && artist.isNotEmpty()) manga.artist = artist
        }

        if (it.text().contains("Rating")) {
            parseRatingAndVotes(manga, it.nextElementSibling().text())
        }

        if (it.text().contains("Popularity")) {
            parsePopularityAndViews(manga, it.nextElementSibling().text())
        }

        if (it.text().contains("Type")) {
            val type = it.nextElementSibling().text().split("-".toRegex())[0].trim()
            val first = Type.values().find { typ: Type -> typ.dispName == type }
            manga.type = first
        }

        if (it.text().contains("Genre")) {
            it.nextElementSibling().getElementsByTag("a").forEach { a: Element ->
                manga.addGenres(Genre.values().first { gen ->
                    gen.dispName.replace("\\s", "")
                            .compareTo(a.text().replace("\\s", ""), true) == 0
                })
            }
        }

        if (it.text().contains("Release")) {
            manga.release = it.nextElementSibling().text().toInt()
        }

        if (it.text().contains("Status")) {
            val status = it.nextElementSibling().text()
            manga.status = status
        }

        if (it.text().contains("Alternative")) {
            val altNames = it.nextElementSibling().text()
            manga.altTitle = altNames.split(";".toRegex()).onEach { it.trim() }
        }
    }

    manga.summary = mangaClass.getElementsByClass("summary").text()
}

/**
 * Creates the manga objects to be used in the search fragment
 * based on the information given from the html
 * @param html The object containing the html
 * @return a list of search mangas
 */
fun Element.createSearchManga(pos: Int): List<Manga> {
    var i = pos

    return getElementsByTag("table").mapNotNull {
        val aTag = it.getElementsByClass("cover")
        if (aTag.size == 0) return@mapNotNull null

        Manga(aTag.attr("href")).apply {
            id = i++.toLong()
            title = correctTitle(aTag.attr("title"))
            thumbnailLink = correctThumbnailLink(aTag.first().getElementsByTag("img").attr("src"))

            parseRatingAndVotes(this, it.getElementsByClass("rate").attr("title"))

            it.getElementsByTag("b").forEach { b: Element ->
                when {
                    b.hasClass("rank") -> {
                        popularity = b.text()
                    }

                    b.text().contains("Authors/Artists") -> {
                        author = b.nextElementSibling().text()
                    }

                    b.text().contains("Status") -> {
                        status = b.nextElementSibling().text()
                    }

                    b.text().contains("Genre") -> {
                        val genreTag = b.nextElementSibling().parent().getElementsByTag("a").map { it.text().lowercase().replace(" ", "") }
                        genres = Genre.values().filter { genreTag.contains(it.dispName.lowercase().replace(" ", "")) }
                    }
                }
            }
        }
    }
}

/**
 * Uses RxJava to concurrently create the manga chapters and inserts them into the manga
 * This method returns a Disposable for the caller to properly dispose
 */
fun Element.createChapterWithObservable(manga: Manga) {
    val select = select("div[id*=stream]")
    val latestChapters = select("ul[class=lest]").first().select("a[class=visited ch]")
    val latestLinks = latestChapters.map { element: Element -> element.attr("href") }


    Observable.range(0, select.size).flatMap({
        val version = select[it].select("span[class*=ml-1 stream-text]").text()

        val versionTranslate = Chapter.Version.values().first {
            version.equals(it.dispName, ignoreCase = true)
        }

        createChapters(select[it], manga, versionTranslate, latestLinks)
    }, 30).map { chapter: Chapter? -> manga }.blockingSubscribe()
}

private fun createChapters(parent: Element, manga: Manga, chapVersion: Chapter.Version, latest: List<String>): Observable<Chapter?>? {
    val allLinks = parent.select("a[class=ml-1 visited ch]")
    val allTime = parent.select("span[class=time]").text().split("ago".toRegex()).toTypedArray()

    val chapLengths = parent.select("em").text().split("1 3 6 10 all of ".toRegex()).toTypedArray()
    val li = parent.select("li[class*=d-flex py-1 item]")

    return Observable.range(0, allLinks.size).map {

        Chapter(manga.id).apply {
            val chapNum = allLinks[it].text()
            val chapLink = allLinks[it].attr("href")

            title = fixTitle(correctTitle(li[it].select("div[class=d-none d-md-flex align-items-center ml-0 ml-md-1  txt]").text(), chapNum))
            link = chapLink
            version = chapVersion
            number = extractChapterNumber(chapNum)
            releaseDate = allTime[it].translateTime()

            isNewChap = latest.contains(chapLink)
            length = chapLengths[it + 1].trim().toInt()
            position = allLinks.size - 1 - it
        }

    }.doOnNext { manga.addChapter(it) }
}

private fun extractChapterNumber(text: String): Float {
    val numberPattern = Pattern.compile("(ch|Chapter)\\.?\\s?([\\d\\.]+)")
    val numberMatcher = numberPattern.matcher(text)

    return if (numberMatcher.find()) {
        val groupCount = numberMatcher.groupCount()
        numberMatcher.group(groupCount).toFloat()
    } else 0f
}

private fun correctTitle(wrongTitle: String, chapterNumber: String): String {

    val titleSplit: Array<String> = wrongTitle.split("(ch|Chapter)\\.?\\s*[\\d\\.]+:?".toRegex()).toTypedArray()

    return if (titleSplit.size <= 1) {
        return if (wrongTitle.isEmpty()) {
            val titles = chapterNumber.split("(ch|Chapter)\\.?\\s*[\\d\\.]+:?".toRegex()).toTypedArray()
            Timber.d("Chapter number is $chapterNumber")
            return if (titles.size > 1) titles[1]
            else {
                val splt = chapterNumber.split("(Vol|vol|Volume|volume)\\.?\\d+".toRegex())
                Timber.d("Split volume is $splt")
                if (splt.size > 1) splt[1] else chapterNumber
            }
        } else wrongTitle//use title itself otherwise
    } else titleSplit[1]
}

