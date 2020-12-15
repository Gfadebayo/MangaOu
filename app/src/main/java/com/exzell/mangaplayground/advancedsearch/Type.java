package com.exzell.mangaplayground.advancedsearch;

import androidx.annotation.NonNull;

public enum Type {

    JAPANESE("manga", "Japanese Manga", true),
    JAPANESE_W("webtoon", "Japanese Webtoon", false),
    KOREAN("manhwa", "Korean Manhwa", true),
    CHINESE("manhua", "Chinese Manhwa", true),
    CHINESE_V2("manhua", "Chinese Manhua", false),
    UNKNOWN("unknown", "Unknown", true);

    Type(String value, String dispName, boolean isSearchable){
        this.value = value;
        this.dispName = dispName;
        this.isSearchable = isSearchable;
    }

    public String value;
    public String dispName;
    public boolean isSearchable;


    @NonNull
    @Override
    public String toString() {
        return dispName;
    }
}
