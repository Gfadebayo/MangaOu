package com.exzell.mangaplayground.io.database;

import android.util.Log;

import androidx.core.util.PatternsCompat;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.exzell.mangaplayground.advancedsearch.Genre;
import com.exzell.mangaplayground.advancedsearch.Type;
import com.exzell.mangaplayground.download.Download;
import com.exzell.mangaplayground.models.Chapter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MangaTypeConverter {
    private static final String TAG = "MangaTypeConverter";

    @TypeConverter
    public static String genreToString(List<Genre> genre){
        Stream<Genre> genreStream = Stream.of(genre.toArray(new Genre[genre.size()]));

        return genreStream.map(genre1 -> genre1.name()).collect(Collectors.joining("|"));
    }

    @TypeConverter
    public static List<Genre> stringToGenre(String string){

        String[] split = string.split(Pattern.quote("|"));
        return Stream.of(split).map(Genre::valueOf).collect(Collectors.toList());
    }

    @TypeConverter
    public static String typeToString(Type type){
        return type.name();
    }

    @TypeConverter
    public static Type stringToType(String string){
        return Type.valueOf(string);
    }

    @TypeConverter
    public static String versionToString(Chapter.Version version){
        return version.name();
    }

    @TypeConverter
    public static Chapter.Version stringToVersion(String string){
        return Chapter.Version.valueOf(string);
    }

    @TypeConverter
    public static String stateToString(Download.State state){
        return state.name();
    }

    @TypeConverter
    public static Download.State stringToState(String string){
        return Download.State.valueOf(string);
    }
}
