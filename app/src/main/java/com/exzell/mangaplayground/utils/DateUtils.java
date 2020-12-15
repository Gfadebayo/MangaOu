package com.exzell.mangaplayground.utils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

    public static long translateTime(String weirdTime){

        StringBuilder timeBuild = new StringBuilder(weirdTime);
        long time = (System.currentTimeMillis());

        while (!timeBuild.toString().isEmpty() && !timeBuild.toString().trim().equals("ago")){
            time = parseDate(timeBuild, time);
        }


        String date = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(new Date(time));

        return time;
    }


    private static long parseDate(StringBuilder time, long oldTime){

        int endIndex = 0;
        int startIndex;
        long multiplierToSecond = 1;

        if(time.toString().contains("second")){
            endIndex = time.indexOf("second");

        }else if(time.toString().contains("minute")){
            endIndex = time.indexOf("minute");
            multiplierToSecond = 60;

        }else if(time.toString().contains("hour")){
            endIndex = time.indexOf("hour");
            multiplierToSecond = 3600;

        }else if(time.toString().contains("day")){
            endIndex = time.indexOf("day");
            multiplierToSecond = 3600 * 24;

        }else if(time.toString().contains("week")){
            endIndex = time.indexOf("week");
            multiplierToSecond = 7 * 3600 * 24;

        }else if(time.toString().contains("month")){
            endIndex = time.indexOf("month");
            multiplierToSecond = 30 * 3600 * 24;

        }else if(time.toString().contains("year")){
            endIndex = time.indexOf("year");
            multiplierToSecond = 365 * 24 * 3600;
        }



        startIndex = time.lastIndexOf(" ", endIndex-2);
        startIndex = startIndex == -1 ? 0 : startIndex;
        String timeStr = time.substring(startIndex, endIndex).trim();
        int timeInSec = Character.isDigit(timeStr.charAt(0)) ? Integer.parseInt(timeStr) : 1;
        oldTime = oldTime - ((timeInSec * multiplierToSecond) * 1000);


        int nextSpaceIndex = time.indexOf(" ", endIndex);
        if(nextSpaceIndex != -1) time.delete(startIndex, nextSpaceIndex+1);
        else time.delete(startIndex, time.length());

        return oldTime;
    }
}
