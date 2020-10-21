package com.caowj.lib_logs.helper;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.caowj.lib_logs.LegoLog;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class LogRecord {

    static final String TAG = "LogRecord";
    static final String NET_TAG = "LegoHttpLog";

    LinkedBlockingQueue<LogInfo> logLinkedBlockingQueue;

    static LogRecord INSTANCE;
    WeakReference<OnPrintLogListener> mOnPrintLogListener;
    WriteLogThread nWriteLogThread;
    //    LogRecordConfig nLogRecordConfig;
    Map<Integer, LogFile> nLogFileMap = new HashMap<>();
    private static int PID = android.os.Process.myPid();
    private static String PName;
    LogcatRecord.LogcatConfig mLogRecordConfig;
    boolean mIsStop = false;
    Handler mHandler = new Handler(Looper.getMainLooper());

    void startRecord(LogcatRecord.LogcatConfig config) {
        if(mLogRecordConfig == null||mIsStop){// 配置以第一次为主，防止插件覆盖宿主
            mLogRecordConfig = config;
            LogFile.LOG_FILE_MAX_SIZE = config.maxFileSize;
        }

        start();
    }

    public static LogRecord init(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LogRecord(context.getApplicationContext());
        }
        return INSTANCE;
    }

    public static LogRecord getINSTANCE() {
        return INSTANCE;
    }

    private LogRecord(Context context) {
        Constance.initLogFolder(context);
        PName = Constance.getProcessName(context);
        logLinkedBlockingQueue = new LinkedBlockingQueue<LogInfo>();
//        nLogFileMap.put(LogTypeEnum.Crash.ordinal(), new LogFile(context, LogTypeEnum.Crash));
        nLogFileMap.put(LogTypeEnum.Net.ordinal(), new LogFile(context, LogTypeEnum.Net));
//        nLogFileMap.put(LogTypeEnum.Logcat.ordinal(), new LogFile(context, LogTypeEnum.Logcat));
        nLogFileMap.put(LogTypeEnum.Bussiness.ordinal(), new LogFile(context, LogTypeEnum.Bussiness));
    }

    public void setOnPrintLogListener(OnPrintLogListener onPrintLogListener) {

        if (mOnPrintLogListener != null) {
            mOnPrintLogListener.clear();
        }
        if (onPrintLogListener != null) {
            mOnPrintLogListener = new WeakReference<OnPrintLogListener>(onPrintLogListener);
        }

    }

    private void start() {
        // 防止多次启动
        if (nWriteLogThread == null) {
            mIsStop = false;
            nWriteLogThread = new WriteLogThread();
            nWriteLogThread.start();
        }
    }

    private void reStart() {
        if(!mIsStop){
            nWriteLogThread = new WriteLogThread();
            nWriteLogThread.start();
        }

    }

    public void stop() {
        if (nWriteLogThread != null) {
            mIsStop = true;
            nWriteLogThread.interrupt();
            nWriteLogThread = null;
        }
    }

    public boolean isRunning() {
        if (nWriteLogThread == null) {
            return false;
        }
        return !nWriteLogThread.isInterrupted();
    }

    /**
     * 写入log
     *
     * @param logPriority 日志级别
     * @param logType     日志类型
     * @param tag
     * @param logContent  日志内容
     * @param isRecord    true-强制记录，不受配置的筛选条件控制;false-符合筛选条件就记录
     */
    public static void writeLog(LogPriority logPriority, LogTypeEnum logType, String tag, String logContent, boolean isRecord) {
        if (INSTANCE != null && INSTANCE.isRunning()) {
            INSTANCE.write(logPriority, logType, tag, logContent, isRecord);
        }

    }

    //    public static void writeLog(LogTypeEnum logType, LogPriority logLevel, String tag, String logContent) {
//        if (INSTANCE != null) {
//            INSTANCE.write(logType, tag, logContent);
//        }
//
//    }

    private void write(LogPriority logPriority, LogTypeEnum logType, String tag, String logContent, boolean isRecord) {
        StringBuilder stringBuilder  = new StringBuilder();
        stringBuilder.append(Constance.LOG_DATA_TIME_FORMAT.format(new Date()))
                .append(PID).append("-").append(PName).append("/? ").append(logPriority).append("/")
                .append(tag).append(logContent).append('\n');
        if (logPriority.priority() >= mLogRecordConfig.priority.priority() || isRecord) {
//            logContent = String.format(Locale.CHINESE, "%s %d-%s/? %s/%s: %s",
//                    Constance.LOG_DATA_TIME_FORMAT.format(new Date()), PID, PName, logPriority, tag, logContent) + '\n';

            if (tag.contains(NET_TAG)) {
                logType = LogTypeEnum.Net;
            }
            logLinkedBlockingQueue.offer(new LogInfo(logType, stringBuilder.toString()));

        }

    }

    class WriteLogThread extends Thread {

        public WriteLogThread() {
            setName("WriteLogThread");
        }

        @Override
        public void run() {
            LegoLog.d(TAG, "WriteLogThread start" + WriteLogThread.this.toString(), true);
            // 清除之前的LOG
            clearLog();
//            MappedByteBuffer buffer = null;
//            RandomAccessFile randomAccessFile = null;
            FileOutputStream fileOutputStream = null;
            LogInfo logInfo = null;
            LogFile logFile = null;
            while (!isInterrupted()) {
                try {
                    logInfo = logLinkedBlockingQueue.take();
                } catch (Exception e) {
                   continue;
                }

                if (logInfo != null && logInfo.log != null) {

                    if (mOnPrintLogListener != null && mOnPrintLogListener.get() != null) {
                        mOnPrintLogListener.get().onPrintLog(logInfo.logType,logInfo.log);
                    }

                    logFile = nLogFileMap.get(logInfo.logType.ordinal());

                    byte[] logBytes =  logInfo.log.getBytes();
                    try {
                        fileOutputStream = logFile.getFileOutputStream(logBytes.length );
//                        randomAccessFile = logFile.getRandomAccessFile(logBytes.length);

//                        buffer = randomAccessFile.getChannel()
//                                .map(FileChannel.MapMode.READ_WRITE, randomAccessFile.length(), logBytes.length);
//                        buffer.put(logBytes);
                        fileOutputStream.write(logBytes);
                    } catch (IOException e) {
                        // 这里不能使用LegoLog,因为如果没有SD读写权限，会一直循环执行
                        Log.e(TAG, null, e);
                    }

                }

            }

            closeFile();
            if(!mIsStop){
                // 重新拉起线程
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        INSTANCE.reStart();
                    }
                });
            }

            LegoLog.d(TAG, "WriteLogThread end", true);
        }


    }

    private void clearLog() {
        Collection<LogFile> logFiles = nLogFileMap.values();
        for (LogFile logFile : logFiles) {
            Constance.autoClearLog(mLogRecordConfig.retainDay, logFile.getFolderPath(), TAG);
        }
    }

    private void closeFile() {
        Collection<LogFile> logFiles = nLogFileMap.values();
        for (LogFile logFile : logFiles) {
            logFile.close();
        }
    }

    public static class LogInfo {

        String log;
        LogTypeEnum logType;

        public LogInfo(LogTypeEnum logType, String log) {
            this.logType = logType;
            this.log = log;
        }

        public String getLog() {
            return log;
        }

        public void setLog(String log) {
            this.log = log;
        }

        public LogTypeEnum getLogType() {
            return logType;
        }

        public void setLogType(LogTypeEnum logType) {
            this.logType = logType;
        }
    }



}

