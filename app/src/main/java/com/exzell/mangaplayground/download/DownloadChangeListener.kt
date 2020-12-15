package com.exzell.mangaplayground.download

interface DownloadChangeListener {

    companion object{
        @JvmField val FLAG_PROGRESS: String = "progress"
        @JvmField val FLAG_STATE: String = "state"
        @JvmField val FLAG_NEW: String = "new";
    }

    fun onDownloadChange(down: Download, flag: String)
}