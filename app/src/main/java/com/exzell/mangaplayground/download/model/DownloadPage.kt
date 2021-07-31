package com.exzell.mangaplayground.download.model

import com.exzell.mangaplayground.models.Download

data class DownloadPage(
        val number: String,
        val path: String,
        val url: String?,
        val parent: Download,
        var state: State = State.PENDING) {

    enum class State {
        DOWNLOADED,
        ERROR,
        PENDING
    }
}