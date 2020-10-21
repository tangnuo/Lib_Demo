package com.caowj.lib_logs.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Logcat 日志工具类
 *
 * @author : 袁兵兵
 * @version : 1.0.8
 * @since : 2019-10-09
 */
public class LogcatRecord {

    private static String TAG = "LogcatRecord";
//    private static final String NET_TAG = "LegoHttpLog";

//    private static final String LOG_FILE_NAME = "(\\d{4}-\\d{1,2}-\\d{1,2})_(\\d+)(_\\d{1,2}:\\d{1,2}:\\d{1,2})?(-\\d{1,2}:\\d{1,2}:\\d{1,2})?";
//    private static final Pattern LOG_FILE_NAME_PATTERN = Pattern.compile(LOG_FILE_NAME, Pattern.CASE_INSENSITIVE);
    static final int LOG_FILE_MAX_SIZE = 1024 * 1024 * 10; //10M
//    private static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
//    private static final SimpleDateFormat LOG_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
//    private static final Pattern LOG_CAT_CONTENT = Pattern.compile("\\d{0,4}-?\\d{1,2}-\\d{1,2}\\s*\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{0,4}\\s*\\d{1,8}[-\\s]*\\d{1,8}[\\s\\/][\\w|\\.|\\?]*\\s*([a-zA-Z])[\\s\\/](\\S*?)\\s*:[\\s\\S]*", Pattern.CASE_INSENSITIVE);
//    private static final SimpleDateFormat LOG_DATA_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//    private static Pattern nLogContentRegex;


//    private static List<String> nLogPriority;
    private static String nLogcatShell;
    private static int nLogSource = LogBuilder.LogSource.LegoLog;
//    private static LinkedBlockingQueue<String> LOG_CACHE = new LinkedBlockingQueue();
//    private static int PID = android.os.Process.myPid();
//    private static String PName;
    private static Map<Integer, LogFile> nLogFileMap = new HashMap<>();
    private static WeakReference<OnPrintLogListener> nOnPrintLogListener;

    LogcatConfig nLogcatConfig = null;

    public LogcatRecord(Context context) {

//        PName = Constance.getProcessName(context);
        Constance.initLogFolder(context);
        nLogFileMap.put(LogTypeEnum.Net.toValue(), new LogFile(context, LogTypeEnum.Net));
        nLogFileMap.put(LogTypeEnum.Logcat.toValue(), new LogFile(context, LogTypeEnum.Logcat));
        nLogFileMap.put(LogTypeEnum.Bussiness.toValue(), new LogFile(context, LogTypeEnum.Bussiness));
        startAlarmManager(context);
    }

    /**
     * 开启日志记录
     *
     * @param config
     */
    void startRecord(LogcatConfig config) {
        if (config == null) config = new LogcatConfig();
        // wangqian add start
        if (nLogcatConfig == null) {// 配置以第一次为主，防止插件覆盖宿主
            nLogcatConfig = config;
        } else {
            config = nLogcatConfig;
        }
        // wangqian add end
        LogFile.LOG_FILE_MAX_SIZE = config.maxFileSize;
//        nLogPriority = new ArrayList<>(LogPriority.Silent.priority() - config.priority.priority() + 1);
//        for (LogPriority value : LogPriority.values()) {
//            if (config.priority.priority() <= value.priority()) {
//                nLogPriority.add(value.toString());
//            }
//        }
//        nLogPriority = Collections.unmodifiableList(nLogPriority);

        nLogcatShell = getLogcatShell(config);
        clearLog(config.retainDay);

        nLogSource = config.logSource;

        startRecordThread();
    }

    private static void startRecordThread() {

        if (nLogSource == LogBuilder.LogSource.Logcat || nLogSource == LogBuilder.LogSource.ALL) {
            LogcatThread.startThread();
        }
    }


    public static void setOnPrintLogListener(OnPrintLogListener onPrintLogListener) {

        if (nOnPrintLogListener != null) {
            nOnPrintLogListener.clear();
        }
        if (onPrintLogListener != null) {
            nOnPrintLogListener = new WeakReference<>(onPrintLogListener);
        }
    }

    private static void printLog(String log) {
            nOnPrintLogListener.get().onPrintLog(LogTypeEnum.Logcat,log);
    }


    /**
     * 开启Log线程定时唤醒机制，以往APP后台睡眠时无法记录日志
     *
     * @param context Android Context
     */
    private void startAlarmManager(Context context) {
        context.registerReceiver(new AlarmReceiver(), new IntentFilter(AlarmReceiver.ACTION));

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(AlarmReceiver.ACTION);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1500000, pendingIntent);
    }


    /**
     * 自动启动指定day前的数据
     *
     * @param day 保留天数
     */
    private void clearLog(int day) {
        Collection<LogFile> logFiles = nLogFileMap.values();
        for (LogFile logFile : logFiles) {
            Constance.autoClearLog(day, logFile.getFolderPath(), TAG);
        }
    }

//    private void closeFile() {
//        Collection<LogFile> logFiles = nLogFileMap.values();
//        for (LogFile logFile : logFiles) {
//            logFile.close();
//        }
//    }


    /**
     * 根据配置创建logcat命令
     *
     * @param config
     * @return
     */
    private String getLogcatShell(LogcatConfig config) {
        StringBuilder sb = new StringBuilder();
        if (config.pid > 0) {
            sb.append(String.format(" --pid %s", String.valueOf(config.pid)));
        }
        sb.append(" *:" + config.priority.toString());
        if (config.expr != null && config.expr.length() > 0) {
            sb.append("|grep " + config.priority.toString());
//            nLogContentRegex = Pattern.compile(config.expr, Pattern.CASE_INSENSITIVE);
        }

        return "/system/bin/logcat" + sb.toString() + " -b all ";
    }


//    private static void clearLogCache() {
//        LOG_CACHE.clear();
//    }

    /**
     * 将日志添加缓存列表中
     *
     * @param priority   日志等级
     * @param tag        日志TAG
     * @param logContent 日志内容
     */
//    public static void addLogCache(String priority, String tag, String logContent) {
//        String log = String.format(Locale.CHINESE, "%s %d-%d/%s %s/%s: %s", LOG_DATA_TIME_FORMAT.format(new Date()), PID, PID, PName, priority, tag, logContent);
//        LOG_CACHE.offer(log);
//    }

    /**
     * Logcat 日志记录线程
     */
    private static class LogcatThread extends Thread {


        private static Thread THREAD_INSTANCE;
        byte[] newLineByteArray = "\n".getBytes();

        private static void stopThread() {
            if (THREAD_INSTANCE != null && (THREAD_INSTANCE.isAlive() || !THREAD_INSTANCE.isInterrupted())) {
                THREAD_INSTANCE.interrupt();
                THREAD_INSTANCE = null;
            }
        }

        private static void startThread() {
            if (THREAD_INSTANCE != null && (!THREAD_INSTANCE.isAlive() || THREAD_INSTANCE.isInterrupted())) {
                THREAD_INSTANCE = null;
            }

            if (THREAD_INSTANCE == null) {
                THREAD_INSTANCE = new LogcatThread();
                THREAD_INSTANCE.start();
            }
        }

        public LogcatThread() {
            super("LogcatThread");
        }

        @Override
        public void run() {
            super.run();

            Process logcatProcess = null;
            InputStream logcatInputStream;
            FileOutputStream fileOutputStream = null;
            byte[] logBytes = new byte[10240 * 2];
            String printLog = null;
            try {
                //LegoLog.d("日志日志+++++++ " + nLogcatShell);
                logcatProcess = Runtime.getRuntime().exec(nLogcatShell);
                logcatInputStream = logcatProcess.getInputStream();

                long lastUpdate = System.currentTimeMillis();
                LogFile logFile = nLogFileMap.get(LogTypeEnum.Logcat.toValue());
                int readLength;
                while (!isInterrupted()) {

                    try {
                        readLength = logcatInputStream.read(logBytes);
                        if (readLength == 0) {
                            sleep(50);
                            //超过60秒未读取到日志，重启logcat进程
                            if ((System.currentTimeMillis() - lastUpdate) > 60000) {
                                logcatProcess.destroy();
                                logcatProcess = Runtime.getRuntime().exec(nLogcatShell);
//                                logcatReader = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream()), 10240);
                                logcatInputStream = logcatProcess.getInputStream();
                            }
                            continue;
                        }
                        if (nOnPrintLogListener != null && nOnPrintLogListener.get() != null) {
                            printLog = new String(logBytes,0, readLength);
                            printLog(printLog);
                        }
                        fileOutputStream = logFile.getFileOutputStream(readLength + newLineByteArray.length);
//                            buffer = randomAccessFile.getChannel()
//                                    .map(FileChannel.MapMode.READ_WRITE, randomAccessFile.length(), logBytes.length + newLineByteArray.length);
                        fileOutputStream.write(logBytes, 0, readLength);
                        //添加换行符
                        fileOutputStream.write(newLineByteArray);
//
//                            buffer.put(newLineByteArray);

                        lastUpdate = System.currentTimeMillis();
//                        if (nLogSource == LogBuilder.LogSource.Logcat) {
//                            clearLogCache();
//                        }

                    } catch (InterruptedException e) {
                        Log.e(TAG, "LogcatThread 异常", e);
                        interrupt();
                    } catch (Exception e) {
                        // 不打印日志，防止没有SD读写权限，会一直循环打印
//                       String message =  e.getMessage();
//                       if(!(message!=null&&message.contains("Permission denied"))){
//                           Log.e(TAG, "LogcatThread 异常", e);
//                       }

                    }

                }

            } catch (Exception e) {
                Log.e(TAG, "LogcatThread 异常", e);
            } finally {

                if (logcatProcess != null) {
                    logcatProcess.destroy();
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Exception e) {

                    }

                }

            }

        }


    }


//    /**
//     * Log缓存队列 持久化线程(对应LOGCAT无法读取日志的情况)
//     */
//    private static class LogCacheThread extends Thread {
//        private static Thread THREAD_INSTANCE;
//
//        private static void stopThread() {
//            if (THREAD_INSTANCE != null && (THREAD_INSTANCE.isAlive() || !THREAD_INSTANCE.isInterrupted())) {
//                THREAD_INSTANCE.interrupt();
//                THREAD_INSTANCE = null;
//            }
//        }
//
//        private static void startThread() {
//            if (THREAD_INSTANCE != null && (!THREAD_INSTANCE.isAlive() || THREAD_INSTANCE.isInterrupted())) {
//                THREAD_INSTANCE = null;
//            }
//
//            if (THREAD_INSTANCE == null) {
//                THREAD_INSTANCE = new LogCacheThread();
//                THREAD_INSTANCE.start();
//            }
//        }
//
//
//        public LogCacheThread() {
//            super("LogCacheThread");
//        }
//
//        @Override
//        public void run() {
//            super.run();
//
//            MappedByteBuffer buffer;
//            RandomAccessFile randomAccessFile;
//            String line;
//            boolean record = false;
//            byte[] newLineByteArray = "\n".getBytes();
//            LogFile logFile = null;
//
//            while (!isInterrupted()) {
//
//                try {
//                    if (LOG_CACHE.size() > 0) {
//
//                        line = LOG_CACHE.take();
//
//                        printLog(line);
//
//                        Matcher matcher = LOG_CAT_CONTENT.matcher(line);
//                        if (matcher.matches() && matcher.groupCount() >= 2) {
//                            String priority = matcher.group(1);
//                            String tag = matcher.group(2).trim();
//                            record = nLogPriority.contains(priority) || (tag.endsWith("+") && tag.startsWith("+"));
//
//                            if (nLogContentRegex != null && record) {
//                                record = nLogContentRegex.matcher(line).find();
//                            }
//                            logFile = nLogFileMap.get(tag.contains(NET_TAG) ? LogTypeEnum.Net.toValue() : LogTypeEnum.Bussiness.toValue());
//                        }
//
//                        if (record) {
//                            byte[] logBytes = line.getBytes();
//                            try {
//                                randomAccessFile = logFile.getRandomAccessFile(logBytes.length + newLineByteArray.length);
//
//                                buffer = randomAccessFile.getChannel()
//                                        .map(FileChannel.MapMode.READ_WRITE, randomAccessFile.length(), logBytes.length + newLineByteArray.length);
//                                buffer.put(logBytes);
//                                //添加换行符
//                                buffer.put(newLineByteArray);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//
//                            }
//
//                        }
//
//                    } else {
//                        sleep(500);
//                    }
//
//
//                } catch (InterruptedException e) {
//                    Log.e(TAG, "LogCacheThread InterruptedException", e);
//                    interrupt();
//                } catch (Exception e) {
//                    Log.e(TAG, "LogCacheThread Exception", e);
//                }
//
//            }
//
//
//        }
//    }


    /**
     * Logcat 配置参数
     */
    static class LogcatConfig {
        //日志级别
        LogPriority priority = LogPriority.Error;
        //过滤指定PID日志，默认0记录当前系统所有日志
        //android 4.1 及以上 只能记录当前进程的日志(子进程不支持)
        int pid;
        // 日志内容过滤表达式
        String expr;
        //日志保留天数
        int retainDay = 7;
        // 日志文件最大尺寸
        int maxFileSize = LOG_FILE_MAX_SIZE;
        int logSource = LogBuilder.LogSource.LegoLog;

    }


    private class AlarmReceiver extends BroadcastReceiver {
        static final String ACTION = "com.kedacom.log.alarm";

        @Override
        public void onReceive(Context context, Intent intent) {

            startRecordThread();
        }
    }


}
