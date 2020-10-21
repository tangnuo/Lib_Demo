package com.caowj.lib_utils

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * 时间处理工具类
 */
object DatetimeUtil {

    /**
     *  格式：年－月－日 小时：分钟：秒
     */
    @JvmField
    val FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND = "yyyy-MM-dd HH:mm:ss"

    /**
     * 格式：年－月－日 小时：分钟
     */
    @JvmField
    val FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE = "yyyy-MM-dd HH:mm"

    /**
     * 格式：年－月－日
     */
    @JvmField
    val FORMAT_YEAR_MONTH_DAY = "yyyy-MM-dd"

    /**
     * 格式：月－日
     */
    @JvmField
    val FORMAT_MONTH_DAY = "MM-dd"

    /**
     *  格式：小时：分钟：秒
     */
    @JvmField
    val FORMAT_HOUR_MINUTE_SECOND = "HH:mm:ss"

    /**
     * 格式：小时 ：分钟
     */
    @JvmField
    val FORMAT_HOUR_MINUTE = "HH:mm"

    /**
     * 年的加减
     */
    @JvmField
    val SUB_YEAR = if (SDK_INT < 24) Calendar.YEAR else android.icu.util.Calendar.YEAR

    /**
     * 月加减
     */
    @JvmField
    val SUB_MONTH = if (SDK_INT < 24) Calendar.MONTH else android.icu.util.Calendar.MONTH

    /**
     * 天的加减
     */
    @JvmField
    val SUB_DAY = if (SDK_INT < 24) Calendar.DATE else android.icu.util.Calendar.DATE

    /**
     * 小时的加减
     */
    @JvmField
    val SUB_HOUR = if (SDK_INT < 24) Calendar.HOUR else android.icu.util.Calendar.HOUR

    /**
     * 分钟的加减
     */
    @JvmField
    val SUB_MINUTE = if (SDK_INT < 24) Calendar.MINUTE else android.icu.util.Calendar.MINUTE

    /**
     * 秒的加减
     */
    @JvmField
    val SUB_SECOND = if (SDK_INT < 24) Calendar.SECOND else android.icu.util.Calendar.SECOND

    @JvmField
    val dayNames = arrayOf("", "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")

    /**
     * 时间格式
     */
    @JvmStatic
    private val mTimeFormat = if (SDK_INT < 24)
        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    else
        android.icu.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    /**
     * 把符合日期格式的String转换为Date对象
     *
     * @param dateStr 日期字符串
     * @param format 格式
     * @return 格式
     */
    @JvmStatic
    fun stringToDate(dateStr: String, format: String): Date? {
        return try {
            if (SDK_INT < 24) {
                val formatter = java.text.SimpleDateFormat(format, Locale.getDefault())
                formatter.calendar.isLenient = false
                formatter.parse(dateStr)
            } else {
                val formatter = android.icu.text.SimpleDateFormat(format, Locale.getDefault())
                formatter.isLenient = false
                formatter.parse(dateStr)
            }
        } catch (e: Exception) {
            // log.error(e);
            null
        }
    }

    /**
     * 把符合日期格式的字符串转换为日期类型，从pos位置开始解析
     */
    @JvmStatic
    fun stringToDate(dateStr: String, format: String, pos: ParsePosition): Date? {
        return if (SDK_INT < 24) {
            val formatter = java.text.SimpleDateFormat(format, Locale.getDefault())
            formatter.calendar.isLenient = false
            formatter.parse(dateStr, pos)
        } else {
            val formatter = android.icu.text.SimpleDateFormat(format)
            formatter.isLenient = false
            formatter.parse(dateStr, pos)
        }
    }

    /**
     * 把日期转换为字符串
     *
     * @param date
     * @return
     */
    @JvmStatic
    fun dateToString(date: Date, format: String): String {
        var result = ""
        val formatter = if (SDK_INT < 24)
            java.text.SimpleDateFormat(format, Locale.getDefault())
        else
            android.icu.text.SimpleDateFormat(format)
        try {
            result = formatter.format(date)
        } catch (e: Exception) {
            // log.error(e);
        }

        return result
    }

    /**
     * 获取当前时间的指定格式
     *
     * @param format 格式字符串， 例如："yyyy-MM-dd HH:mm:ss"
     * @return format格式的字符串
     */
    @JvmStatic
    fun getCurrDate(format: String): String {
        return dateToString(Date(), format)
    }

    /**
     * 依据Calendar规则对日期进行增减操作。
     * 此方法默认按照"yyyy-MM-dd HH:mm:ss"转换为Date类型，进行日期的增减后返回String格式。
     *
     * @param dateKind 日期类型，例如：Calendar.DAY_OF_MONTH
     * @param dateStr 日期字符串格式
     * @param amount 增减的量值
     * @return
     */
    @JvmStatic
    fun dateSub(dateKind: Int, dateStr: String, amount: Int): String {
        val date = stringToDate(dateStr, FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        return if (SDK_INT < 24) {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(dateKind, amount)
            dateToString(calendar.time, FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        } else {
            val calendar = android.icu.util.Calendar.getInstance()
            calendar.time = date
            calendar.add(dateKind, amount)
            dateToString(calendar.time, FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        }
    }

    /**
     * 两个日期相减
     *
     * @param firstTime 第一个日期
     * @param secTime 第二个日期
     * @return 相减得到的秒数
     */
    @JvmStatic
    fun timeSubtraction(firstTime: String, secTime: String): Long {
        val first = stringToDate(firstTime, FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)!!.time
        val second = stringToDate(secTime, FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)!!.time
        return (second - first) / 1000
    }

    /**
     * 获得某月的天数
     *
     * @param year 年
     *
     * @param month 月份
     *
     * @return int
     */
    @JvmStatic
    fun getDaysOfMonth(year: String, month: String): Int {
        var days = 0
        if (month == "1" || month == "3" || month == "5"
                || month == "7" || month == "8" || month == "10"
                || month == "12") {
            days = 31
        } else if (month == "4" || month == "6" || month == "9"
                || month == "11") {
            days = 30
        } else {
            if (Integer.parseInt(year) % 4 == 0 && Integer.parseInt(year) % 100 != 0 || Integer.parseInt(year) % 400 == 0) {
                days = 29
            } else {
                days = 28
            }
        }

        return days
    }

    /**
     * 获取某年某月的天数
     *
     * @param year
     * int
     * @param month
     * int 月份[1-12]
     * @return int
     */
    @JvmStatic
    fun getDaysOfMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /**
     * 获得当前月份的第几天，例如：11/5 => 5。
     * 每月第一天值为1。
     *
     * @return int
     */
    @JvmStatic
    fun getToday(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.DATE)
    }

    /**
     * 获得当前月份
     *
     * @return int
     */
    @JvmStatic
    fun getMonth(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.MONTH) + 1
    }

    /**
     * 获得当前年份
     *
     * @return int
     */
    @JvmStatic
    fun getYear(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR)
    }

    /**
     * 给定日期，获取日期中Day值。功能类似getToday()方法。
     *
     * @param date
     * Date
     * @return int
     */
    @JvmStatic
    fun getDay(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.DATE)
    }

    /**
     * 返回日期的年
     *
     * @param date
     * Date
     * @return int
     */
    @JvmStatic
    fun getYear(date: Date?): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.YEAR)
    }

    /**
     * 返回日期的月份，1-12
     *
     * @param date
     * Date
     * @return int
     */
    @JvmStatic
    fun getMonth(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.MONTH) + 1
    }

    /**
     * 计算两个日期相差的天数，如果date2 > date1 返回正数，否则返回负数
     *
     * @param date1
     * Date
     * @param date2
     * Date
     * @return long
     */
    @JvmStatic
    fun dayDiff(date1: Date, date2: Date): Long {
        return (date2.time - date1.time) / 86400000
    }

    /**
     * 比较两个日期的年差
     *
     * @param befor
     * @param after
     * @return
     */
    @JvmStatic
    fun yearDiff(before: String, after: String): Int {
        val beforeDay = stringToDate(before, FORMAT_YEAR_MONTH_DAY)
        val afterDay = stringToDate(after, FORMAT_YEAR_MONTH_DAY)
        return getYear(afterDay) - getYear(beforeDay)
    }

    /**
     * 比较指定日期与当前日期的差
     *
     * @param befor
     * @param after
     * @return
     */
    @JvmStatic
    fun yearDiffCurr(after: String): Int {
        val beforeDay = Date()
        val afterDay = stringToDate(after, FORMAT_YEAR_MONTH_DAY)
        return getYear(beforeDay) - getYear(afterDay)
    }

    /**
     * 比较指定日期与当前日期的差
     * @param before
     * @return
     */
    @JvmStatic
    fun dayDiffCurr(before: String): Long? {
        val currDate = stringToDate(currDay(), FORMAT_YEAR_MONTH_DAY)
        val beforeDate = stringToDate(before, FORMAT_YEAR_MONTH_DAY)
        return currDate?.time?.minus(beforeDate!!.time)?.div(86400000)

    }

    /**
     * 获取某月第一天的dayOfWeek值，星期天(1) ... 星期六(7)。
     *
     * @param year
     * @param month
     * @return
     */
    @JvmStatic
    fun getFirstWeekdayOfMonth(year: Int, month: Int): Int {
        val c = Calendar.getInstance()
        c.firstDayOfWeek = Calendar.SUNDAY // 星期天为第一天
        c.set(year, month - 1, 1)
        return c.get(Calendar.DAY_OF_WEEK)
    }

    /**
     * 获取某每月的最后一天的dayOfWeek值，星期天(1) ... 星期六(7)。
     *
     * @param year
     * @param month
     * @return
     */
    @JvmStatic
    fun getLastWeekdayOfMonth(year: Int, month: Int): Int {
        val c = Calendar.getInstance()
        c.firstDayOfWeek = Calendar.SATURDAY // 星期天为第一天
        c.set(year, month - 1, getDaysOfMonth(year, month))
        return c.get(Calendar.DAY_OF_WEEK)
    }

    /**
     * 获得当前日期字符串，格式"yyyy_MM_dd_HH_mm_ss"，单位数补0处理。例如：2019_11_05_18_49_52
     *
     * @return
     */
    @JvmStatic
    fun getCurrent(): String {
        val cal = Calendar.getInstance()
        cal.time = Date()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)
        val sb = StringBuffer()
        sb.append(year).append("_").append(addzero(month, 2))
                .append("_").append(addzero(day, 2)).append("_")
                .append(addzero(hour, 2)).append("_").append(
                        addzero(minute, 2)).append("_").append(
                        addzero(second, 2))
        return sb.toString()
    }

    /**
     * 获得当前日期字符串，格式"yyyy-MM-dd HH:mm:ss"。
     *
     * @return
     */
    @JvmStatic
    fun getNow(): String {
        val today = Calendar.getInstance()
        return dateToString(today.time, FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    }

    /**
     * 判断日期是否有效,包括闰年的情况
     *
     * @param date
     * YYYY-mm-dd
     * @return
     */
    @JvmStatic
    fun isDate(date: String): Boolean {
        val reg = StringBuffer(
                "^((\\d{2}(([02468][048])|([13579][26]))-?((((0?")
        reg.append("[13578])|(1[02]))-?((0?[1-9])|([1-2][0-9])|(3[01])))")
        reg.append("|(((0?[469])|(11))-?((0?[1-9])|([1-2][0-9])|(30)))|")
        reg.append("(0?2-?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][12")
        reg.append("35679])|([13579][01345789]))-?((((0?[13578])|(1[02]))")
        reg.append("-?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))")
        reg.append("-?((0?[1-9])|([1-2][0-9])|(30)))|(0?2-?((0?[")
        reg.append("1-9])|(1[0-9])|(2[0-8]))))))")
        val p = Pattern.compile(reg.toString())
        return p.matcher(date).matches()
    }

    /**
     * 取得指定日期过 months 月后的日期 (当 months 为负数表示指定月之前);
     *
     * @param date
     * 日期 为null时表示当天
     * @param month
     * 相加(相减)的月数
     */
    @JvmStatic
    fun nextMonth(date: Date?, months: Int): Date {
        val cal = Calendar.getInstance()
        if (date != null) {
            cal.time = date
        }
        cal.add(Calendar.MONTH, months)
        return cal.time
    }

    /**
     * 取得指定日期过 day 天后的日期 (当 day 为负数表示指日期之前);
     *
     * @param date 日期 为null时表示当天
     * @param day 相加(相减)的月数
     */
    @JvmStatic
    fun nextDay(date: Date?, day: Int): Date {
        val cal = Calendar.getInstance()
        if (date != null) {
            cal.time = date
        }
        cal.add(Calendar.DAY_OF_YEAR, day)
        return cal.time
    }

    /**
     * 取得距离今天 day 日的日期
     * @param day
     * @param format
     * @return
     */
    @JvmStatic
    fun nextDay(day: Int, format: String): String {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, day)
        return dateToString(cal.time, format)
    }

    /**
     * 取得指定日期过 week 周后的日期 (当 week 为负数表示指定月之前)
     *
     * @param date 日期 为null时表示当天
     * @param week week周
     */
    @JvmStatic
    fun nextWeek(date: Date?, week: Int): Date {
        val cal = Calendar.getInstance()
        if (date != null) {
            cal.time = date
        }
        cal.add(Calendar.WEEK_OF_MONTH, week)
        return cal.time
    }

    /**
     * 获取当前的日期(yyyy-MM-dd)
     */
    @JvmStatic
    fun currDay(): String {
        return dateToString(Date(), FORMAT_YEAR_MONTH_DAY)
    }

    /**
     * 获取昨天的日期
     *
     * @return
     */
    @JvmStatic
    fun yesterday(): String {
        return yesterday(FORMAT_YEAR_MONTH_DAY)
    }

    /**
     * 根据时间类型获取昨天的日期
     * @param format
     * @return
     */
    @JvmStatic
    fun yesterday(format: String): String {
        return dateToString(nextDay(Date(), -1), format)
    }

    /**
     * 获取明天的日期
     */
    @JvmStatic
    fun tomorrow(): String {
        return dateToString(nextDay(Date(), 1), FORMAT_YEAR_MONTH_DAY)
    }

    /**
     * 取得当前时间距离1900/1/1的天数
     *
     * @return
     */
    @JvmStatic
    fun getDayNum(): Int {
        var daynum = 0
        val gd = GregorianCalendar()
        val dt = gd.time
        val gd1 = GregorianCalendar(1900, 1, 1)
        val dt1 = gd1.time
        daynum = ((dt.time - dt1.time) / (24 * 60 * 60 * 1000)).toInt()
        return daynum
    }

    /**
     * getDayNum的逆方法(用于处理Excel取出的日期格式数据等)
     *
     * @param day
     * @return
     */
    @JvmStatic
    fun getDateByNum(day: Int): Date {
        val gd = GregorianCalendar(1900, 1, 1)
        var date = gd.time
        date = nextDay(date, day)
        return date
    }

    /**
     *  针对yyyy-MM-dd HH:mm:ss格式,显示yyyymmdd
     */
    @JvmStatic
    fun getYmdDateCN(datestr: String?): String {
        if (datestr == null)
            return ""
        if (datestr.length < 10)
            return ""
        val buf = StringBuffer()
        buf.append(datestr.substring(0, 4)).append(datestr.substring(5, 7))
                .append(datestr.substring(8, 10))
        return buf.toString()
    }

    /**
     * 获取本月第一天
     *
     * @param format
     * @return
     */
    @JvmStatic
    fun getFirstDayOfMonth(format: String): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DATE, 1)
        return dateToString(cal.time, format)
    }

    /**
     * 获取本月最后一天
     *
     * @param format
     * @return
     */
    @JvmStatic
    fun getLastDayOfMonth(format: String): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DATE, 1)
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.DATE, -1)
        return dateToString(cal.time, format)
    }

    /**
     * 将元数据前补零，补后的总长度为指定的长度，以字符串的形式返回
     * @param sourceDate
     * @param formatLength
     * @return 重组后的数据
     */
    @JvmStatic
    fun addzero(sourceDate: Int, formatLength: Int): String {
        /*
      * 0 指前面补充零
      * formatLength 字符总长度为 formatLength
      * d 代表为正数。
      */
        return String.format("%0" + formatLength + "d", sourceDate)
    }


    /**
     *
     * 获取当前是星期几
     *
     * @return 星期几
     */
    @JvmStatic
    fun getWeek(): String {
        val cal = Calendar.getInstance()
        return dayNames[cal.get(Calendar.DAY_OF_WEEK)]
    }

    /**
     *
     * 根据十二小时制或者二十四小时制得到时间
     *
     * @return 时间
     */
    @JvmStatic
    fun getCurrentTimeBy12_24(context: Context): String {
        if (android.text.format.DateFormat.is24HourFormat(context)) {
            return getCurrDate(FORMAT_HOUR_MINUTE)
        } else {
            val sb = StringBuilder()
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val minute = Calendar.getInstance().get(Calendar.MINUTE)
            if (hour >= 12) {
                sb.append(hour - 12).append(":").append(minute).append(" PM")
            } else {
                sb.append(hour).append(":").append(minute).append(" AM")
            }
            return sb.toString()
        }
    }
/*******************以下是合并来的*********************************/
    /**
     * 转换日期格式,如"1694-1-1 10:23:4" 转成"11694-01-01 10:23:04"
     * @param strTime
     * @return
     */
    @JvmStatic
    fun formatDateString(strTime: String): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            val date = formatter.parse(strTime)
            return formatter.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return ""
    }

    /**
     * 转换Date对象为yyyy-MM-dd HH:mm:ss格式的字符串
     * @param date
     * @return
     */
    @JvmStatic
    fun dateToLongStr(date: java.util.Date?): String? {
        if (date == null) {
            return null
        }
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return formatter.format(date)
    }

    /**
     * 转换Date对象为yyyy-MM-dd"格式的字符串
     * @param date
     * @return
     */
    @JvmStatic
    fun dateToDayStr(date: java.util.Date?): String? {
        if (date == null) {
            return null
        }
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        return formatter.format(date)
    }

    /**
     * 转换日期字符串为Date对象
     * @param strDate 1986-01-15或者2018-05-15 14:20:56格式的字符串
     * @return
     */
    @JvmStatic
    fun strToDate(strDate: String): Date? {
        var formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val pos = ParsePosition(0)
        var strtodate: Date? = formatter.parse(strDate, pos)
        if (strtodate == null) {
            formatter = SimpleDateFormat("yyyy-MM-dd")
            strtodate = formatter.parse(strDate, pos)
        }
        return strtodate
    }

    /**
     * 时间戳（毫秒数）转成yyyy-MM-dd HH:mm:ss格式的日期字符串
     * @param value
     * @return
     */
    @JvmStatic
    fun timestampToLongDateStr(value: Long?): String? {
        val date = if (value == null) null else Date(value)
        return dateToLongStr(date)
    }

    /**
     * 时间戳（毫秒数）转成yyyy-MM-dd格式的日期字符串
     * @param value
     * @return
     */
    @JvmStatic
    fun timestampToDateStr(value: Long?): String? {
        val date = if (value == null) null else Date(value)
        return dateToDayStr(date)
    }

    /**
     * yyyy-MM-dd HH:mm:ss或yyyy-MM-dd 格式的时间字符串转成时间戳（毫秒数）
     * @param strDate
     * @return
     */
    @JvmStatic
    fun dateStrToTimestamp(strDate: String): Long? {
        val date = strToDate(strDate)
        return if (date == null) null else date!!.getTime()
    }


}