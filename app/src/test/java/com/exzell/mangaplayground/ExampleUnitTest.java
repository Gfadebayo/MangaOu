package com.exzell.mangaplayground;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        System.out.println(1d/2);
    }

    @Test
    public void testDate(){
        Calendar dar = Calendar.getInstance();
        dar.setTimeInMillis(System.currentTimeMillis());

        Calendar testDar = randomDay();
        testDar.set(2020, 9, 13);

        System.out.println("The date used is " + testDar.getTime().toString() + " current date is " + dar.getTime().toString());


        System.out.print("The difference in time is: ");
        System.out.println(formatDay((int) (Math.abs(dar.getTime().getTime() - testDar.getTime().getTime()) / (1000*60*60*24))));
    }

    public Calendar randomDay(){
        int day = (int) (Math.random() * 31);
        int month = (int) (Math.random() * 12);

        Calendar dar = Calendar.getInstance();
        dar.set(Calendar.DAY_OF_MONTH, day);
        dar.set(Calendar.MONTH, month);
        return dar;
    }

    @Test
    public void testTime(){
        Calendar in = Calendar.getInstance();
        in.set(Calendar.MINUTE, 0);
        in.set(Calendar.HOUR, 0);
        in.set(Calendar.SECOND, 0);
        in.set(Calendar.MILLISECOND, 0);

        System.out.println("Calendar " + in.getTime());
    }

    private String formatDay(int day){
        if(day == 0) return "Today";
        else if(day == 1) return "Yesterday";
        else{
            return day + "days ago";
        }
    }

    @Test
    public void nextLink(){
        StringBuilder newLink = new StringBuilder("/genre/action");
        int index = newLink.lastIndexOf("/");
        String num = newLink.substring(index+1);
        int newNum = num.isEmpty() || !Character.isDigit(num.charAt(0)) ? 2 : Integer.parseInt(num)+1;
        newLink.replace(index+1, newLink.length(), String.valueOf(newNum));

//        return newLink.toString();
    }

    @Test
    public void testCalendar(){
        Calendar in = Calendar.getInstance();
        in.set(Calendar.MINUTE, 0);
        in.set(Calendar.HOUR, 0);
        in.set(Calendar.SECOND, 0);
        in.set(Calendar.MILLISECOND, 0);

        System.out.println(in.getTimeInMillis());
    }

    @Test
    public void testStringLocale(){
        Locale loc = Locale.JAPAN;

        String i = String.format(loc, "%s", "I");

        System.out.println("Translation: " + i);
    }

    @Test
    public void testTest(){
        //Format: xth, it has xx monthly views

        String popu = "6th, it has 112.82k monthly views.";

        String[] split = popu.split(",");

        String popular = split[0];

        String views = Stream.of(split[1].trim().split("\\s+")).filter(p -> Character.isDigit(p.charAt(0))).collect(Collectors.joining());

        System.out.printf("%s popularity with %s monthly views", popular, views);
    }
}