package com.exzell.mangaplayground.utils

import java.text.SimpleDateFormat
import java.util.*

fun Calendar.resetCalendar(millis: Long? = null): Calendar{
    millis?.let { setTimeInMillis(it)}
    set(Calendar.MINUTE, 0)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return this
}

    fun String.translateTime(): Long {
        val timeBuild = StringBuilder(this)
        var time = System.currentTimeMillis()
        while (!timeBuild.toString().isEmpty() && timeBuild.toString().trim { it <= ' ' } != "ago") {
            time = parseDate(timeBuild, time)
        }
        return time
    }

    private fun parseDate(time: StringBuilder, oldTime: Long): Long {
        var oldTime = oldTime
        var endIndex = 0
        var startIndex: Int
        var multiplierToSecond: Long = 1
        if (time.toString().contains("second")) {
            endIndex = time.indexOf("second")
        } else if (time.toString().contains("minute")) {
            endIndex = time.indexOf("minute")
            multiplierToSecond = 60
        } else if (time.toString().contains("hour")) {
            endIndex = time.indexOf("hour")
            multiplierToSecond = 3600
        } else if (time.toString().contains("day")) {
            endIndex = time.indexOf("day")
            multiplierToSecond = (3600 * 24).toLong()
        } else if (time.toString().contains("week")) {
            endIndex = time.indexOf("week")
            multiplierToSecond = (7 * 3600 * 24).toLong()
        } else if (time.toString().contains("month")) {
            endIndex = time.indexOf("month")
            multiplierToSecond = (30 * 3600 * 24).toLong()
        } else if (time.toString().contains("year")) {
            endIndex = time.indexOf("year")
            multiplierToSecond = (365 * 24 * 3600).toLong()
        }
        startIndex = time.lastIndexOf(" ", endIndex - 2)
        startIndex = if (startIndex == -1) 0 else startIndex
        val timeStr = time.substring(startIndex, endIndex).trim { it <= ' ' }
        val timeInSec = if (Character.isDigit(timeStr[0])) timeStr.toInt() else 1
        oldTime -= timeInSec * multiplierToSecond * 1000
        val nextSpaceIndex = time.indexOf(" ", endIndex)
        if (nextSpaceIndex != -1) time.delete(startIndex, nextSpaceIndex + 1) else time.delete(startIndex, time.length)
        return oldTime
    }