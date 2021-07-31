package com.exzell.mangaplayground.download.model;

import com.exzell.mangaplayground.models.Download;
import com.exzell.mangaplayground.models.Manga;

import org.jetbrains.annotations.NotNull;

public class DownloadManga extends Manga {

    private int totalProgress;

    private int totalLength;

    @NotNull
    private Download.State state = Download.State.QUEUED;

    public int getTotalProgress() {
        return totalProgress;
    }

    public void setTotalProgress(int progress) {
        this.totalProgress = progress;
    }

    public Download.@NotNull State getState() {
        return state;
    }

    public void setState(Download.@NotNull State state) {
        this.state = state;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }
}
