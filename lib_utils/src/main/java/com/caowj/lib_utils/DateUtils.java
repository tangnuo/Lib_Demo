package com.caowj.lib_utils;

import android.util.Log;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by wangqian on 2018/6/1.
 */

public class DateUtils {
    private DateUtils(){

    }
    /**
     * 转换日期格式,如"1694-1-1 10:23:4" 转成"11694-01-01 10:23:04"
     * @param strTime
     * @return
     */
    public static String formatDateString(String strTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            Date date = formatter.parse(strTime);
            return formatter.format(date);
        }catch (ParseException e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 转换Date对象为yyyy-MM-dd HH:mm:ss格式的字符串
     * @param date
     * @return
     */
    public static String dateToLongStr(java.util.Date date) {
        if(date == null){
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }

    /**
     * 转换Date对象为yyyy-MM-dd"格式的字符串
     * @param date
     * @return
     */
    public static String dateToDayStr(java.util.Date date) {
        if(date == null){
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(date);
        return dateString;
    }

    /**
     * 转换日期字符串为Date对象
     * @param strDate 1986-01-15或者2018-05-15 14:20:56格式的字符串
     * @return
     */
    public static Date strToDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        if(strtodate == null){
             formatter = new SimpleDateFormat("yyyy-MM-dd");
            strtodate = formatter.parse(strDate, pos);
        }
        return strtodate;
    }
    /**
     * 时间戳（毫秒数）转成yyyy-MM-dd HH:mm:ss格式的日期字符串
     * @param value
     * @return
     */
    public static String timestampToLongDateStr(Long value) {
        Date date =  value == null ? null : new Date(value);
        return DateUtils.dateToLongStr(date);
    }

    /**
     * 时间戳（毫秒数）转成yyyy-MM-dd格式的日期字符串
     * @param value
     * @return
     */
    public static String timestampToDateStr(Long value) {
        Date date =  value == null ? null : new Date(value);
        return DateUtils.dateToDayStr(date);
    }

    /**
     * yyyy-MM-dd HH:mm:ss或yyyy-MM-dd 格式的时间字符串转成时间戳（毫秒数）
     * @param strDate
     * @return
     */
    public static Long dateStrToTimestamp(String strDate) {
        Date date = DateUtils.strToDate(strDate);
        return date == null ? null : date.getTime();
    }


    /**
     * 1: 表示现在的时间大于被比较的时间
     * 0：表示现在的时间小于被比较的时间
     * -1: 报错
     * 注：yyyy-MM-dd HH:mm:ss 其中 HH 表示24小时制， hh 表示12小时制
     */
    public static int compareNowDate(String compareDateString, String format) {
        // 格式化时间
        SimpleDateFormat CurrentTime = new SimpleDateFormat(format, Locale.CHINA);
        try {
            Date compareDate = CurrentTime.parse(compareDateString);
            if ((System.currentTimeMillis() - compareDate.getTime()) > 0) {
                return 1;
            } else {
                return 0;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 1: 表示第一个时间大于第二个的时间
     * 0：表示第一个时间小于第e二个的时间
     * -1: 报错
     */
    public static int compareTwoDate(String firstTime, String twoTime, String format) {
        // 格式化时间
        SimpleDateFormat CurrentTime = new SimpleDateFormat(format, Locale.CHINA);
        try {
            Date firstDate = CurrentTime.parse(firstTime);
            Date twoDate = CurrentTime.parse(twoTime);
            if ((firstDate.getTime() - twoDate.getTime()) > 0) {
                return 1;
            } else {
                return 0;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 调此方法输入所要转换的时间，输入例如（"2014年06月14日16时09分00秒"）返回时间戳(毫秒)
     */
    public static long getTimestampMilli(String time, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.CHINA);
        try {
            Date date = dateFormat.parse(time);
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * @param timestamp 某一时间戳 单位：毫秒
     * @return <0:过去 0：今天 1：明天 2：后天 >3：大于后天
     */
    public static int getDateType(long timestamp) {
        // 时间差
        long deltaT = dataFormat(timestamp) - dataFormat(System.currentTimeMillis());
        return (int) (deltaT / ONE_DAY_MS);
    }

    /**
     * @param dateStr 某一时间 例如 2019-09-09 13:20:11
     * @return <0:过去 0：今天 1：明天 2：后天 >3：大于后天
     */
    public static int getDateType(String dateStr, String format) {
        // 时间差
        long deltaT = dataFormat(getTimestampMilli(dateStr, format)) - dataFormat(System.currentTimeMillis());
        return (int) (deltaT / ONE_DAY_MS);
    }

    /**
     * 时间戳取整（例：将2018/01/09 11:22:33 的时间戳格式化为2018/01/09 00:00:00的时间戳）
     */
    private static long dataFormat(long timestamp) {
        Calendar calendar = getCalendar(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime().getTime();
    }

    /**
     * 输入Date 输出（"2014年06月14日16时09分00秒"）
     */
    public static String formatDate(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.CHINA);
        return dateFormat.format(date);
    }

    /**
     * 输入"2014年06月14日16时09分00秒" 输出 Date
     */
    public static Date toDate(String dateStr, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.CHINA);
        Date date = null;
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 输入时间戳（1402733340）转化为 Date
     */
    public static Date toDate(long dateStamp) {
        Calendar calendar = getCalendar(dateStamp);
        return calendar.getTime();
    }

    /**
     * 调用此方法输入所要转换的时间戳输入例如（1402733340）输出（"2014年06月14日16时09分00秒"）
     *
     * @param timestamp 时间戳，单位：毫秒
     */
    public static String formatDate(long timestamp, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.CHINA);
        return dateFormat.format(new Date(timestamp));
    }

    /**
     * 打招呼
     *
     * @param timestamp 时间戳，单位：毫秒
     */
    public static String hello(long timestamp) {
        SimpleDateFormat sdr = new SimpleDateFormat("HH", Locale.CHINA);
        String times = sdr.format(new Date(timestamp));
        int hour = Integer.valueOf(times);
        if (hour < 6) {
            return "凌晨好！";
        }
        if (hour >= 6 && hour < 12) {
            return "上午好！ ";
        }
        if (hour >= 12 && hour < 18) {
            return "下午好！ ";
        }
        if (hour >= 18) {
            return "晚上好！ ";
        }
        return "";
    }

    /**
     * 返回日("1516261426000" -> 18)
     * 注意：timestamp 单位：毫秒
     */
    public static String getDay(long timestamp) {
        int day = 0;
        try {
            day = getCalendar(timestamp).get(Calendar.DAY_OF_MONTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(day);
    }

    /**
     * 返回日 (Date -> day)
     */
    public static String getDay(Date date) {
        int day = 0;
        try {
            day = getCalendar(date).get(Calendar.DAY_OF_MONTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(day);
    }

    /**
     * 输入时间戳变星期
     *
     * @param timestamp 单位：毫秒
     */
    public static String getWeek(long timestamp) {
        int week = 0;
        try {
            week = getCalendar(timestamp).get(Calendar.DAY_OF_WEEK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getWeek(week);
    }

    /**
     * 输入日期如（2014年06月14日16时09分00秒）返回（星期数）
     */
    public static String getWeek(String time, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.CHINA);
        int week = 0;
        try {
            Date date = dateFormat.parse(time);
            week = getCalendar(date).get(Calendar.DAY_OF_WEEK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getWeek(week);
    }

    /**
     * 输入Date 返回（星期数）
     */
    public static String getWeek(Date date) {
        int week = 0;
        try {
            week = getCalendar(date).get(Calendar.DAY_OF_WEEK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getWeek(week);
    }

    private static String getWeek(int week) {
        switch (week) {
            case 1:
                return "日";
            case 2:
                return "一";
            case 3:
                return "二";
            case 4:
                return "三";
            case 5:
                return "四";
            case 6:
                return "五";
            case 7:
                return "六";
            default:
                return "";
        }
    }

    /**
     * 获取当前星期
     */
    public static int getCurrentWeek() {
        int week = 0;
        try {
            week = getCalendar().get(Calendar.DAY_OF_WEEK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getIntWeek(week);
    }

    private static int getIntWeek(int week) {
        switch (week) {
            case 1: // 周日
                return 7;
            case 2: // 周一
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            case 5:
                return 4;
            case 6:
                return 5;
            case 7:
                return 6;
            default:
                return 0;
        }
    }

    /**
     * 获取本周所有日期
     */
    public static List<String> getWeekDays() {
        List<String> list = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        // 获取本周的第一天
        int firstDayOfWeek = calendar.getFirstDayOfWeek();
        for (int i = 0; i < 7; i++) {
            calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek + i);
            // 获取星期的显示名称，例如：周一、星期一、Monday等等
//           String week = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH);
            String day = new SimpleDateFormat("dd").format(calendar.getTime());
            // 去掉前面的0，ex: 05 -> 5
            if (day.startsWith("0")) {
                day = day.substring(1);
            }
            list.add(day);
        }
        return list;
    }

    /**
     * 获取本周所有日期
     */
    public static List<String> getWeekDays(String format) {
        List<String> list = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        // 获取本周的第一天
        int firstDayOfWeek = calendar.getFirstDayOfWeek();
        for (int i = 0; i < 7; i++) {
            calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek + i);
            // 获取星期的显示名称，例如：周一、星期一、Monday等等
//           String week = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH);
            String day = new SimpleDateFormat(format).format(calendar.getTime());
            // 去掉前面的0，ex: 05 -> 5
            if (day.startsWith("0")) {
                day = day.substring(1);
            }
            list.add(day);
        }
        return list;
    }

    /**
     * 获取指定时间戳所在那一周的所有日期
     */
    public static List<String> getWeekDays(long timestamp, String format) {
        List<String> list = new ArrayList<>();
        Calendar calendar = getCalendar(timestamp);
        // 获取本周的第一天
        int firstDayOfWeek = calendar.getFirstDayOfWeek();
        for (int i = 0; i < 7; i++) {
            calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek + i);
            // 获取星期的显示名称，例如：周一、星期一、Monday等等
//           String week = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH);
            String date = new SimpleDateFormat(format).format(calendar.getTime());
            list.add(date);
        }
        return list;
    }

    private static final long ONE_DAY_MS = 24 * 60 * 60 * 1000;      // 一天的时间（毫秒）
    public static final long ONE_WEEK_MS = 7 * 24 * 60 * 60 * 1000;  // 一周的时间（毫秒）

    /**
     * 计算两个日期之间的日期
     *
     * @param startTimeStamp 开始时间戳
     * @param endTimeStamp   开始结束
     */
    public static List<String> betweenDays(long startTimeStamp, long endTimeStamp) {
        List<String> days = new ArrayList<>();
        Date date_start = new Date(startTimeStamp);
        Date date_end = new Date(endTimeStamp);

        //计算日期从开始时间于结束时间的0时计算
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(date_start);
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);

        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(date_end);
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);

        int s = (int) ((toCalendar.getTimeInMillis() - fromCalendar.getTimeInMillis()) / (ONE_DAY_MS));
        if (s >= 0) {
            for (int i = 0; i <= s; i++) {
                long dayStamp = fromCalendar.getTimeInMillis() + i * ONE_DAY_MS;
                days.add(formatDate(dayStamp, "yyyy-MM-dd"));
            }
        }
        return days;
    }

    /**
     * 计算两个日期之间的日期
     *
     * @param startTimeStr 开始时间
     * @param endTimeStr   开始结束
     * @param format       格式
     */
    public static List<String> betweenDays(String startTimeStr, String endTimeStr, String format) {
        List<String> days = new ArrayList<>();
        Date date_start = toDate(startTimeStr, format);
        Date date_end = toDate(endTimeStr, format);

        //计算日期从开始时间于结束时间的0时计算
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(date_start);
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);

        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(date_end);
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);

        int s = (int) ((toCalendar.getTimeInMillis() - fromCalendar.getTimeInMillis()) / (ONE_DAY_MS));
        if (s >= 0) {
            for (int i = 0; i <= s; i++) {
                long dayStamp = fromCalendar.getTimeInMillis() + i * ONE_DAY_MS;
                days.add(formatDate(dayStamp, "yyyy-MM-dd"));
            }
        }
        return days;
    }


    /**
     * 秒 -> 分钟 ：秒
     * 秒 -> 小时：分钟 ：秒
     */
    public static String second2Min(int time) {
        String timeStr;
        int hour;
        int minute;
        int second;
        if (time <= 0) {
            return "00:00";
        } else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    /**
     * 秒 -> 分钟 ：秒
     * 秒 -> 小时：分钟 ：秒
     */
    public static String second2Min2(int time) {
        String timeStr;
        int hour;
        int minute;
        int second;
        if (time <= 0) {
            return "00:00";
        } else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    private static String unitFormat(int i) {
        String retStr;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }

    public static String getPostDays(String postTime) {
        long l = 1;
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(postTime);
            l = (new Date().getTime() - date.getTime()) / (1000);

            if (l > 30 * 24 * 60 * 60) {
                return new SimpleDateFormat("yyyy-MM-dd").format(date);
            } else if (l > 24 * 60 * 60) {
                l = l / (24 * 60 * 60);
                return String.valueOf(l) + "天前";
            } else if (l > 60 * 60) {
                l = l / (60 * 60);
                return String.valueOf(l) + "小时前";
            } else if (l > 60) {
                l = l / (60);
                return String.valueOf(l) + "分钟前";
            }
            if (l < 1) {
                return "刚刚";
            }
        } catch (Exception ex) {
            Log.d("DateUtils::getPostDays", ex.toString());
        }
        return String.valueOf(l) + "秒前";
    }

    /**
     * 上个月的Calendar
     */
    public static Calendar getLastMonthCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -1);
        return calendar;
    }

    public static Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar;
    }

    public static Calendar getCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    /**
     * @param timestamp 时间戳，单位：毫秒
     */
    public static Calendar getCalendar(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar;
    }

    /**
     * @param time   ex:1991-09-09
     * @param format ex:"yyyy-MM-dd"
     */
    public static Calendar getCalendar(String time, String format) {
        return getCalendar(getTimestampMilli(time, format));
    }

    /**
     * 根据提供的年月获取该月份的最后一天
     */
    public static int getLastDayOfMonth(int year, int monthOfYear) {
        Calendar cal = Calendar.getInstance();
        // 不加下面2行，就是取当前时间前一个月的第一天及最后一天
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        SimpleDateFormat format = new SimpleDateFormat("dd", Locale.CHINA);
        return Integer.valueOf(format.format(cal.getTime()));
    }

    /**
     * 一个月前的日期时间戳
     */
    public static long lastMonthDayStamp() {
        Calendar calendar = getCalendar();
        calendar.add(Calendar.MONTH, -1);
        return calendar.getTimeInMillis();
    }

    /**
     * 一个月前的日期时间戳
     */
    public static long lastMonthDayStamp(String time, String format) {
        Calendar calendar = getCalendar(time, format);
        calendar.add(Calendar.MONTH, -1);
        return calendar.getTimeInMillis();
    }

    /**
     * 近7天前的日期时间戳
     *
     * @return
     */
    public static long recently7Days() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) - 7, 0, 0, 0);
        return calendar.getTimeInMillis();
    }
}
