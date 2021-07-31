package com.exzell.mangaplayground.download

import com.exzell.mangaplayground.models.Download

interface DownloadChangeListener {

    companion object {
        @JvmField
        val FLAG_PROGRESS: String = "progress"
        @JvmField
        val FLAG_STATE: String = "state"
        @JvmField
        val FLAG_NEW: String = "new";
    }

    fun onDownloadChange(downs: MutableList<Download>, flag: String)
}