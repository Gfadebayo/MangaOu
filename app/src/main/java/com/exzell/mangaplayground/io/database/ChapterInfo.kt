package com.exzell.mangaplayground.io.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class ChapterTimeUpdate(@PrimaryKey val id: Long, @ColumnInfo(name = "last_read_time") val lastReadTime: Long)
