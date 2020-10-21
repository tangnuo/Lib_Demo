package com.caowj.lib_logs.helper;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;

import com.caowj.lib_logs.LegoLog;
import com.caowj.lib_utils.AppUtil;
import com.caowj.lib_utils.FileUtil;
import com.caowj.lib_utils.SdCardUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constance {
    static final int LOG_FILE_MAX_SIZE = 1024 * 1024 * 10; //10M
    // 每个文件夹的日志个数最多100个
    static final int LOG_FILE_MAX_SUM =100;
    private static final String LOG_FILE_NAME = "(\\d{4}-\\d{1,2}-\\d{1,2})_(\\d+)(_\\d{1,2}:\\d{1,2}:\\d{1,2})?(-\\d{1,2}:\\d{1,2}:\\d{1,2})?";
    private static final Pattern LOG_FILE_NAME_PATTERN = Pattern.compile(LOG_FILE_NAME, Pattern.CASE_INSENSITIVE);
    private static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    // 文件名上的时间格式，因windows文件名不能包含：，所以用-连接
    private static final SimpleDateFormat LOG_FILE_NAME_TIME_FORMAT = new SimpleDateFormat("HH-mm-ss");
    protected static final SimpleDateFormat LOG_DATA_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static Map<LogTypeEnum, String> LOG_FOLDER_NAME_MAP = new HashMap<>();
    public static String GLOBAL_PATH;

    static {
        LOG_FOLDER_NAME_MAP.put(LogTypeEnum.Net, "net");
        LOG_FOLDER_NAME_MAP.put(LogTypeEnum.Crash, "crash");
        LOG_FOLDER_NAME_MAP.put(LogTypeEnum.Logcat, "logcat");
        LOG_FOLDER_NAME_MAP.put(LogTypeEnum.Bussiness, "bussiness");
    }


    /**
     * 初始化目录
     */
    public static void initLogFolder(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            GLOBAL_PATH = SdCardUtil.getSDCardPathByEnvironment() + File.separator + "kedacom" + File.separator
                    + AppUtil.getPackageName(context);
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            GLOBAL_PATH = context.getFilesDir().getAbsolutePath()
                    + File.separator + "log";
        }
        File file = new File(GLOBAL_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }


    }


    /**
     * 搜索计算可用的日志存储路径
     *
     * @return log日志路径
     */
    static String findAvailableFilePath(String fileDirPath) throws IOException {
        File logPath = new File(fileDirPath);
        if (!logPath.exists()) {
            logPath.mkdirs();
        }

        if (logPath.exists()) {
            int index = 1;
            final String nowDate = LOG_DATE_FORMAT.format(new Date());
            File[] result = logPath.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    String filename = FileUtil.getFileNameWithoutExtension(name);
                    Matcher matcher = LOG_FILE_NAME_PATTERN.matcher(filename);
                    if (!matcher.matches() || matcher.groupCount() < 2) {
                        return false;
                    } else {
                        return nowDate.equalsIgnoreCase(matcher.group(1));
                    }
                }
            });
            if (result != null && result.length > 0) {
                index = result.length + 1;
            }

            return fileDirPath + File.separator + nowDate + "_" + String.format("%03d", index) + "_" + LOG_FILE_NAME_TIME_FORMAT.format(new Date()) + ".log";

        } else {
            // 当第一次启用应用时，没有读取SD卡的权限，就会创建失败
                throw new FileNotFoundException(fileDirPath+"创建失败");
        }
    }

    static void appendEndTime(String logFile) {
        FileUtil.renameFile(new File(logFile), FileUtil.getFileNameWithoutExtension(logFile) + "-" + LOG_FILE_NAME_TIME_FORMAT.format(new Date()));
    }


    /**
     * 自动启动指定day前的数据
     *
     * @param day         保留天数
     * @param fileDirPath 文件夹路径
     */
    static void autoClearLog(int day, String fileDirPath, final String tag) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, -Math.abs(day));
        final Date retainDate = calendar.getTime();

        FileUtil.deleteFilesInDirWithFilter(fileDirPath, new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                String s = FileUtil.getFileNameWithoutExtension(pathname);
                Matcher matcher = LOG_FILE_NAME_PATTERN.matcher(s);
                if (!matcher.matches() || matcher.groupCount() < 2) {
                    return false;
                } else {
                    try {
                        Date date = LOG_DATE_FORMAT.parse(matcher.group(1));
                        return date.compareTo(retainDate) < 0;

                    } catch (ParseException e) {
                        LegoLog.e(tag, "autoClearLog 异常", e);
                        return false;
                    }
                }
            }
        });
        autoClearMoreThanMaxCount(fileDirPath);

    }

    /**
     * 清楚大于100日志的日志文件
     *
     * @param fileDirPath
     */
    static void autoClearMoreThanMaxCount(String fileDirPath) {

        File folder = new File(fileDirPath);
        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }
        if (files.length > LOG_FILE_MAX_SUM) {
            File delFile = null;
            List<String> list = new ArrayList<>();
            for (File file : files) {
                list.add(file.getName());
            }
            java.util.Collections.sort(list);
            while (list.size() > LOG_FILE_MAX_SUM) {
                String fileName = list.get(0);
                delFile = new File(fileDirPath + File.separator + fileName);
                delFile.delete();
                list.remove(0);
            }
        }


    }

    public static String getProcessName(Context cxt) {
        int pid = android.os.Process.myPid();
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }
}
