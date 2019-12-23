package com.caowj.lib_utils;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

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


}
