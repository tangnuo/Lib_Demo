package com.caowj.lib_logs.helper;

import android.content.Context;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 日志配置构建
 *
 * @author : 袁兵兵
 * @version : 0.5.7
 * @since : 2019-10-10
 */
public class LogBuilder {

    static final int LOG_FILE_MAX_SIZE = Constance.LOG_FILE_MAX_SIZE; //10M
    static final int LOG_FILE_MIN_SIZE = 1024 ; //1KB
    static final int LOG_FILE_DEFAULT_SIZE = Constance.LOG_FILE_MAX_SIZE ; //10M
    private boolean recordLog;
    private boolean recordJavaCrash;
    private boolean showJavaCrashDialog = true;
    private LogPriority logPriority = LogPriority.Error;
    private int day = 7;
    private CrashCallback crashCallback;
    private CrashCallback defaultCrashCallback;
    private String expr;
    private int maxFileSize = LOG_FILE_DEFAULT_SIZE;
    private int logSource = LogSource.LegoLog;

    private LogBuilder() {

    }

    public static LogBuilder builder() {
        return new LogBuilder();
    }


    /**
     * 是否将logcat的日志记录到本地文件中
     * <p>
     * 日志路径: /sdcard/kedacom/[packageName]/log/
     *
     * @param value 开启与否
     * @return LogBuilder
     */
    public LogBuilder recordLog(boolean value) {
        this.recordLog = value;
        return this;
    }


    /**
     * 是否记录app奔溃日志到本地文件中
     * <p>
     * 日志路径: /sdcard/kedacom/[packageName]/crash/
     *
     * @param value 开启与否
     * @return LogBuilder
     */
    public LogBuilder recordJavaCrash(boolean value) {
        this.recordJavaCrash = value;
        return this;
    }

    /**
     * 是否在crash时，弹日志dialog
     * @param value true -弹
     * @return
     */
    public LogBuilder showJavaCrashDialog(boolean value) {
        this.showJavaCrashDialog = value;
        return this;
    }

    /**
     * App Crash 回调函数
     *
     * @param callback 回调函数
     * @return LogBuilder
     */
    public LogBuilder crashCallback(CrashCallback callback) {
        this.crashCallback = callback;
        return this;
    }
    /**
     * App 默认Crash 回调函数
     *
     * @author wangqian
     * @param callback 回调函数
     * @return LogBuilder
     */
    public LogBuilder defaultCrashCallback(CrashCallback callback) {
        this.defaultCrashCallback = callback;
        return this;
    }



    /**
     * logcat日志记录级别(默认记录Error及以上级别的日志)
     *
     * @param value LogPriority
     * @return LogBuilder
     */
    public LogBuilder logPriority(LogPriority value) {
        this.logPriority = value;
        return this;
    }

    /**
     * 日志保留指定天数内的日志(默认7天之内)
     *
     * @param day >=0
     * @return LogBuilder
     */
    public LogBuilder retainLog(int day) {
        this.day = Math.abs(day);
        return this;
    }

    /**
     * 日志文件最大尺寸，默认10M  (1024*1024*10)
     *
     * @param size >=0
     * @return LogBuilder
     */
    public LogBuilder maxFileSize(int size) {
        if(size<LOG_FILE_MIN_SIZE){
            size = LOG_FILE_MIN_SIZE;
        }
        if(size>LOG_FILE_MAX_SIZE){
            size = LOG_FILE_MAX_SIZE;
        }
        this.maxFileSize = size;
        return this;
    }

    /**
     * 日志过滤正则表达式
     *
     * @param value 过滤正则表达式
     * @return LogBuilder
     */
    public LogBuilder expr(String value) {
        this.expr = value;
        return this;
    }

    /**
     * 日志记录的数据来源
     * @param value 日志来源类型
     * @return LogBuilder
     */
    public LogBuilder logSource(@LogSource int value){
        this.logSource = value;
        return this;
    }


    /**
     * 设置参数，开启日志记录
     *
     * @param context Android Content
     */
    public void build(Context context) {
        if (recordJavaCrash) {
            LegoCrashHandler handler = LegoCrashHandler.getInstance().init(context, day, this.showJavaCrashDialog);
            if (crashCallback != null) {
                handler.addCrashCallback(crashCallback);
            }
            if(defaultCrashCallback != null){
                handler.setDefaultCrashCallback(defaultCrashCallback);
            }
        }
        if (recordLog) {
            LogcatRecord.LogcatConfig logcatConfig = new LogcatRecord.LogcatConfig();
            logcatConfig.priority = logPriority;
            logcatConfig.retainDay = day;
            logcatConfig.expr = expr;
            logcatConfig.maxFileSize = maxFileSize;
            logcatConfig.logSource = logSource;
            new LogcatRecord(context).startRecord(logcatConfig);


            if (logSource == LogSource.LegoLog || logSource == LogSource.ALL) {
                LogRecord.init(context).startRecord(logcatConfig);
            }
        }
    }

    @IntDef({LogSource.ALL, LogSource.Logcat, LogSource.LegoLog})
    @Retention(RetentionPolicy.SOURCE)
    public  @interface LogSource {
        /**
         * 同时从Logcat以及LegoLog记录日志
         */
        int ALL=0;
        /**
         * 只从Logcat记录日志
         */
        int Logcat = 1;
        /**
         * 只从LegoLog记录日志
         */
        int LegoLog =2;
    }

}
