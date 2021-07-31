package com.exzell.mangaplayground;

import com.exzell.mangaplayground.utils.DateUtilsKt;

import org.junit.Test;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public void testDateFormat(){

        Calendar todayExact = DateUtilsKt.reset(Calendar.getInstance(), null);
        System.out.printf("The date is %s \n", todayExact.getTime().toString());

        todayExact.add(Calendar.DAY_OF_MONTH, -4);
        System.out.printf("The date is %s \n", todayExact.getTime().toString());

        System.out.println(SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(todayExact.getTime()));
    }

    @Test
    public void testDate(){

        Calendar todayCalen = Calendar.getInstance();
        todayCalen.setTimeInMillis(System.currentTimeMillis());
        todayCalen.set(Calendar.MINUTE, 0);
        todayCalen.set(Calendar.HOUR, 0);
        todayCalen.set(Calendar.SECOND, 0);
        todayCalen.set(Calendar.MILLISECOND, 0);

        long today = todayCalen.getTimeInMillis();

        Calendar yester = Calendar.getInstance();
        yester.setTime(Date.valueOf("2021-01-30"));
        long yesterday = yester.getTimeInMillis();

        long todayInDays = Math.floorDiv(today , (1000*60*60*24));
        long yesterdayInDays = Math.floorDiv(yesterday, (1000*60*60*24));

        long diff = todayInDays - yesterdayInDays;

        Calendar in = Calendar.getInstance();
        System.out.printf("The current calendar is %s \n", in.getTime().toString());
        in.setTimeInMillis(System.currentTimeMillis());


        in.set(Calendar.MINUTE, 0);
        in.set(Calendar.HOUR, 0);
        in.set(Calendar.SECOND, 0);
        in.set(Calendar.MILLISECOND, 0);

        System.out.printf("The current calendar after zeroing everything is %s", in.getTime().toString());

        Calendar testDar = randomDay();
        testDar.set(2020, 9, 13);

        System.out.println("The date used is " + testDar.getTime().toString() + " current date is " + in.getTime().toString());


        System.out.print("The difference in time is: ");
        System.out.println(formatDay((int) (Math.abs(in.getTime().getTime() - testDar.getTime().getTime()) / (1000*60*60*24))));
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

    @Test
    public void testParse() {
        String weirdLink = "https:\\/\\/xcdn-210.bato.to\\/10061\\/93\\/5d\\/5d45796baaec5529bf5ed539\\/01_290375_975_1400.jpeg?acc=1J41Wy82e5B6w-P4iBjHyQ&exp=1624800401";
        String collect = String.join("", weirdLink.split("\\\\"));
        System.out.println(collect);
    }
}