package com.caowj.lib_logs;

import android.text.TextUtils;
import android.util.Log;

import com.caowj.lib_logs.helper.LogPriority;
import com.caowj.lib_logs.helper.LogRecord;
import com.caowj.lib_logs.helper.LogTypeEnum;

import java.util.List;


/**
 * 日志统一处理工具类
 *
 * @author : huangyin,袁兵兵
 * @version : 0.5.7
 * @since : 2018-05-24
 */
public class LegoLog {
    private static final int LOG_MAX_LENGTH = 4000;
    private static boolean open = true;
    private static String tag = LegoLog.class.getSimpleName();

    private enum logType {V, D, I, W, E}

    private LegoLog() {
    }

    /**
     * 打开控制台日志输出功能
     */
    public static void logOn() {
        LegoLog.open = true;
    }

    /**
     * 关闭控制台日志输出功能
     */
    public static void logOff() {
        LegoLog.open = false;
    }

    /**
     * 是否开启日志记录
     *
     * @return true:开启，false关闭
     */
    public static boolean isLogging() {
        return LegoLog.open;
    }

    /**
     * 设置默认的日志标签
     *
     * @param tag
     */
    public static void setLogDefaultTag(String tag) {
        LegoLog.tag = tag;
    }

    /**
     * 封装了android.util.Log.v
     */
    public static void v(Object msgObject) {
        handleLog(tag, msgObject, logType.V, false);
    }

    /**
     * 封装了{@link Log#v(String, String)}
     *
     * @param msgObject 日志内容
     * @param recordLog 是否强制记录日志至文件中
     * @since 0.5.7
     */
    public static void v(Object msgObject, boolean recordLog) {
        handleLog(tag, msgObject, logType.V, recordLog);
    }

    /**
     * 封装了android.util.Log.v
     */
    public static void v(String tag, Object msgObject) {
        handleLog(tag, msgObject, logType.V, false);
    }

    /**
     * 封装了{@link Log#v(String, String)}
     *
     * @param tag       tag标签
     * @param msgObject 日志内容
     * @param recordLog 是否强制记录日志至文件中
     * @since 0.5.7
     */
    public static void v(String tag, Object msgObject, boolean recordLog) {
        handleLog(tag, msgObject, logType.V, recordLog);
    }

    /**
     * 封装了android.util.Log.d
     */
    public static void d(Object msgObject) {
        handleLog(tag, msgObject, logType.D, false);
    }

    /**
     * 封装了{@link Log#d(String, String)}
     *
     * @param msgObject 日志内容
     * @param recordLog 是否强制记录日志至文件中
     * @since 0.5.7
     */
    public static void d(Object msgObject, boolean recordLog) {
        handleLog(tag, msgObject, logType.D, recordLog);
    }

    /**
     * 封装了android.util.Log.d
     */
    public static void d(String tag, Object msgObject) {
        handleLog(tag, msgObject, logType.D, false);
    }

    /**
     * 封装了{@link Log#d(String, String)}
     *
     * @param tag       tag标签
     * @param msgObject 日志内容
     * @param recordLog 是否强制记录日志至文件中
     * @since 0.5.7
     */
    public static void d(String tag, Object msgObject, boolean recordLog) {
        handleLog(tag, msgObject, logType.D, recordLog);
    }

    /**
     * 封装了android.util.Log.i
     */
    public static void i(Object msgObject) {
        handleLog(tag, msgObject, logType.I, false);
    }


    /**
     * 封装了{@link Log#i(String, String)}
     *
     * @param msgObject 日志内容
     * @param recordLog 是否强制记录日志至文件中
     * @since : 0.5.7
     */
    public static void i(Object msgObject, boolean recordLog) {
        handleLog(tag, msgObject, logType.I, recordLog);
    }


    /**
     * 封装了android.util.Log.i
     */
    public static void i(String tag, Object msgObject) {
        handleLog(tag, msgObject, logType.I, false);
    }

    /**
     * 封装了{@link Log#i(String, String)}
     *
     * @param tag       tag标签
     * @param msgObject 日志内容
     * @param recordLog 是否强制记录日志至文件中
     * @since : 0.5.7
     */
    public static void i(String tag, Object msgObject, boolean recordLog) {
        handleLog(tag, msgObject, logType.I, recordLog);
    }

    /**
     * 封装了android.util.Log.w
     */
    public static void w(Object msgObject) {
        handleLog(tag, msgObject, logType.W, false);
    }

    /**
     * 封装了{@link Log#w(String, String)}
     *
     * @param msgObject 日志内容
     * @param recordLog 是否强制记录日志至文件中
     * @since : 0.5.7
     */
    public static void w(Object msgObject, boolean recordLog) {
        handleLog(tag, msgObject, logType.W, recordLog);
    }

    /**
     * 封装了android.util.Log.w
     */
    public static void w(String tag, Object msgObject) {
        handleLog(tag, msgObject, logType.W, false);
    }

    /**
     * 封装了{@link Log#w(String, String)}
     *
     * @param tag       tag标签
     * @param msgObject 日志内容
     * @param recordLog 是否强制记录日志至文件中
     * @since : 0.5.7
     */
    public static void w(String tag, Object msgObject, boolean recordLog) {
        handleLog(tag, msgObject, logType.W, recordLog);
    }

    /**
     * 封装了android.util.Log.e
     *
     * @param msgObject 日志内容
     * @param throwable 异常
     */
    public static void e(Object msgObject, Throwable throwable) {
        handleLog(tag, msgObject, logType.E, false, throwable);
    }

    /**
     * 封装了android.util.Log.e
     *
     * @param throwable 异常
     */
    public static void e(Throwable throwable) {
        handleLog(tag, null, logType.E, false, throwable);
    }

    /**
     * 封装了android.util.Log.e
     *
     * @param throwable 异常
     * @param recordLog 是否记录
     */
    public static void e(Throwable throwable, boolean recordLog) {
        handleLog(tag, null, logType.E, recordLog, throwable);
    }

    /**
     * 封装了{@link Log#e(String, String, Throwable)}
     *
     * @param msgObject 日志内容
     * @param throwable 异常
     * @param recordLog 是否强制记录日志至文件中
     * @since : 0.5.7
     */
    public static void e(Object msgObject, Throwable throwable, boolean recordLog) {
        handleLog(tag, msgObject, logType.E, recordLog, throwable);
    }

    /**
     * 封装了android.util.Log.e
     *
     * @param tag
     * @param msgObject
     * @param throwable
     */
    public static void e(String tag, Object msgObject, Throwable throwable) {
        handleLog(tag, msgObject, logType.E, false, throwable);
    }

    public static void e(String tag, String msgObject, boolean recordLog) {
        handleLog(tag, msgObject, logType.E, recordLog, null);
    }

    public static void e(String tag, String msgObject) {
        handleLog(tag, msgObject, logType.E, false, null);
    }

    /**
     * 封装了{@link Log#e(String, String, Throwable)}
     *
     * @param tag       tag标签
     * @param msgObject 日志内容
     * @param throwable 异常
     * @param recordLog 是否强制记录日志至文件中
     * @since : 0.5.7
     */
    public static void e(String tag, Object msgObject, Throwable throwable, boolean recordLog) {
        handleLog(tag, msgObject, logType.E, recordLog, throwable);
    }

    /**
     * 根据需要打印的对象不同来进行不同的处理
     * 程序已经支持的会自动拼接的类型：数组、List
     */
    private static void handleLog(String tag, Object msgObject, logType logType, boolean recordLog) {
        handleLog(tag, msgObject, logType, recordLog, null);
    }

    /**
     * 根据需要打印的对象不同来进行不同的处理
     * 程序已经支持的会自动拼接的类型：数组、List
     */
    private static void handleLog(String tag, Object msgObject, logType logType, boolean recordLog, Throwable throwable) {
        if (open || recordLog) {
            if (msgObject != null) {
                if (msgObject instanceof String) {
                    handleStringLog(tag, (String) msgObject, logType, recordLog,throwable);
                } else if (msgObject.getClass().isArray()) {
                    Object[] msgArray = (Object[]) msgObject;
                    StringBuffer msgBuffer = new StringBuffer();
                    msgBuffer.append('[');
                    for (int i = 0; i < msgArray.length; i++) {
                        msgBuffer.append(msgArray[i] == null ? "null" : msgArray[i].toString());
                        if (i != msgArray.length - 1) {
                            msgBuffer.append(", ");
                        }
                    }
                    msgBuffer.append("]");
                    handleStringLog(tag, msgBuffer.toString(), logType, recordLog,throwable);
                } else if (msgObject instanceof List) {
                    List msgList = (List) msgObject;
                    StringBuffer msgBuffer = new StringBuffer();
                    msgBuffer.append('[');
                    for (int i = 0; i < msgList.size(); i++) {
                        Object listObject = msgList.get(i);
                        msgBuffer.append(listObject == null ? "null" : listObject.toString());
                        if (i != msgList.size() - 1) {
                            msgBuffer.append(", ");
                        }
                    }
                    msgBuffer.append("]");
                    handleStringLog(tag, msgBuffer.toString(), logType, recordLog,throwable);
                } else {
                    handleStringLog(tag, msgObject.toString(), logType, recordLog, throwable);
                }
            } else {
                log(tag, "null", logType, recordLog, throwable);
            }
        }
    }

    /**
     * 根据字符串的长度是否超过定义的最大长度来做决定是否做分段打印
     */
//    private static void handleStringLog(String tag, String msg, logType logType, boolean recordLog,Throwable throwable) {
//        handleStringLog(tag, msg, logType, recordLog, null);
//    }

    /**
     * 根据字符串的长度是否超过定义的最大长度来做决定是否做分段打印
     */
    private static void handleStringLog(String tag, String msg, logType logType, boolean recordLog, Throwable throwable) {
        // 日志长度超过最大长度分段打印
        if (msg.length() > LOG_MAX_LENGTH) {
            for (int index = 0; index < msg.length(); index += LOG_MAX_LENGTH) {
                String subMsg;
                if (msg.length() <= index + LOG_MAX_LENGTH) {
                    subMsg = msg.substring(index);
                } else {
                    subMsg = msg.substring(index, index + LOG_MAX_LENGTH);
                }
                log(tag, subMsg, logType, recordLog, throwable);
            }
        } else {
            log(tag, msg, logType, recordLog, throwable);
        }
    }

    /**
     * 调用Android系统Log类进行日志打印
     */
    private static void log(String tag, String msg, logType logType, boolean recordLog, Throwable throwable) {

//        if(!tag.contains("-LegoHttpLog-")){
//            StackTraceElement element = findCurrentStackTraceElement();
//            msg = msg + "    " + element;
//        }

        if (recordLog) {
            logType = LegoLog.logType.E;
        }

        LogPriority logPriority = LogPriority.Debug;

        switch (logType) {
            case V:
                Log.v(tag, msg);
                logPriority = LogPriority.Verbose;
                break;
            case D:
                Log.d(tag, msg);
                logPriority = LogPriority.Debug;
                break;
            case I:
                Log.i(tag, msg);
                logPriority = LogPriority.Info;
                break;
            case W:
                Log.w(tag, msg);
                logPriority = LogPriority.Warn;
                break;
            case E:
                Log.e(tag, msg, throwable);
                logPriority = LogPriority.Error;
        }

        if (throwable != null) {
            String trace = Log.getStackTraceString(throwable);
            if (TextUtils.isEmpty(trace)) {
                trace = throwable.getMessage();
            }
            msg = msg + '\n' + trace;
        }
        LogRecord.writeLog(logPriority, LogTypeEnum.Bussiness, tag, msg, recordLog);
//        LogcatRecord.addLogCache(logType.name(), tag, msg);
    }

    /**
     * 找出调用LegoLog.xx()方法的相关堆栈信息
     *
     * @return 调用LegoLog.xx()方法的相关堆栈信息
     */

    private static StackTraceElement findCurrentStackTraceElement() {

        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        StackTraceElement element = null;
        boolean findAnyThisClassStackTrackInfo = false;
        for (int i = 0; i < elements.length; i++) {
            element = elements[i];
            if (element.getClassName().equals(LegoLog.class.getName())) {
                findAnyThisClassStackTrackInfo = true;
            } else {
                if (findAnyThisClassStackTrackInfo) {
                    return element;
                }
            }
        }
        return null;
    }

}
