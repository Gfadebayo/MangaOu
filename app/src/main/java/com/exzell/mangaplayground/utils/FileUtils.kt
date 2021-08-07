package com.exzell.mangaplayground.utils

import android.content.Context
import com.exzell.mangaplayground.models.Chapter

import com.exzell.mangaplayground.models.Download
import com.exzell.mangaplayground.models.Manga

import java.io.File

//file format is manga -> version -> chapter

/**
 * Creates the dir where the chapter will be stored
 * @param parentName The parent dirs for the chapter, This should be the manga then the version
 * with / separating both
 */
fun createDownloadFolder(context: Context, manga: Manga, chapter: Chapter): String {

    val fullDir = File(getDownloadFolder(context, manga, chapter))
    if (!fullDir.exists()) fullDir.mkdirs()


    return fullDir.path
}

fun getDownloadFolder(context: Context, manga: Manga, chapter: Chapter): String {
    val downloads = context.getExternalFilesDir("downloads")

    val fullPath = "${manga.title}/${chapter.version}/${chapter.getChapterNumericValue()}"
    val fullDir = File(downloads, fullPath)

    return fullDir.path
}

fun getImagePath(parentPath: String, number: Int): String {
    val strNumber = if (number < 10) "0$number" else number.toString()

    val file = File(parentPath)

    val fileDir = file.listFiles { dir, name ->
        val dotIndex = name.lastIndexOf(".")
        val fileNumber = name.substring(0, dotIndex)
        strNumber == fileNumber
    }

    return if (fileDir != null) fileDir[0].path else ""
}

/**
 * Checks through the list of sent downloads and ensures they exist and have not been removed by the user
 *
 * @return The list of invalid downloads
 */
fun ensureDownloadExist(downloads: List<Download>): List<Download> {
    return downloads.filter {
        !File(it.path).exists()
    }
}
