package com.caowj.lib_logs.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.caowj.lib_logs.ui.CrashDialog;
import com.caowj.lib_utils.AppUtil;
import com.caowj.lib_utils.FileUtil;
import com.caowj.lib_utils.SdCardUtil;
import com.caowj.lib_utils.SystemUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * APP 崩溃异常日志收集工具类
 *
 * @author : yuanbingbing
 * @version : 0.5.7
 * @since : 2018/7/25 15:30
 */
class LegoCrashHandler implements Thread.UncaughtExceptionHandler {

    private static String TAG = "LegoCrashHandler";
    private Thread.UncaughtExceptionHandler nDefaultHandler;
    private Context nContext;
    private StringBuffer nInfos = new StringBuffer();
    private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private boolean nShowJavaCrashDialog = true;
    private List<CrashCallback> crashCallbackList = new ArrayList<>(1);
    String errorMsg = "";
    Object lock = new Object();

    volatile boolean  isFinishBlockMainThread = false;
    /**
     * 默认捕获到异常后退出应用
     */
    private CrashCallback defaultCrashCallback;


    private LegoCrashHandler() {
    }


    public static LegoCrashHandler getInstance() {
        return LegoCrashHandlerInstance.handler;
    }

    /**
     * 初始化，默认销毁10天前的日志
     *
     * @param context
     */
    public LegoCrashHandler init(Context context) {
        //自动销毁5天前的日志文件
        return init(context, 5);
    }


    /**
     * 初始化
     *
     * @param context
     * @param autoClearDay 自动销毁autoClearDay天前的日志文件，如果为0或者负数，则不销毁
     */
    public LegoCrashHandler init(Context context, int autoClearDay) {

        if (!Thread.getDefaultUncaughtExceptionHandler().getClass().getName().contains(LegoCrashHandler.class.getSimpleName())) {
            // 获取系统默认的UncaughtException处理器
            nDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            // 设置该CrashHandler为程序的默认处理器
            Thread.setDefaultUncaughtExceptionHandler(this);
            nContext = context;

            //自动销毁autoClearDay天前的日志文件
            if (autoClearDay > 0) {
                autoClear(autoClearDay);
            }
        }
        return this;

    }

    /**
     *  初始化
     * @param context
     * @param autoClearDay 自动销毁autoClearDay天前的日志文件，如果为0或者负数，则不销毁
     * @param showJavaCrashDialog 是否弹日志dialog
     * @return
     */
    public LegoCrashHandler init(Context context, int autoClearDay, boolean showJavaCrashDialog) {
        this.nShowJavaCrashDialog = showJavaCrashDialog;
        return init(context, autoClearDay);
    }

    public void addCrashCallback(CrashCallback crashCallback) {
        crashCallbackList.add(crashCallback);
    }


    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        String crashFileFullPath = "";
        if (ex == null && this.nDefaultHandler != null) {
            this.nDefaultHandler.uncaughtException(thread, ex);
        } else {
            crashFileFullPath = this.handleException(thread, ex);
        }

//        revokeCallback(thread,ex,crashFileFullPath);

//        if (crashCallbackList.size() == 0) {
//            if (defaultCrashCallback != null) {
//                defaultCrashCallback.callback(thread, ex);
//                defaultCrashCallback.callback(thread,ex,crashFileFullPath);
//            }
//            nDefaultHandler.uncaughtException(thread, ex);
//
//        } else {
//            for (CrashCallback crashCallback : crashCallbackList) {
//                // 为了兼容之前版本，暂不删除
//                crashCallback.callback(thread, ex);
//                crashCallback.callback(thread,ex,crashFileFullPath);
//            }
//        }

    }


    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     */
    protected String handleException(Thread thread, Throwable ex) {
        String crashFileFullPath = null;
        if (ex == null) {
            return crashFileFullPath;
        }

        try {

            // 收集设备参数信息
            collectDeviceInfo(nContext);
            // 保存日志文件
            crashFileFullPath = saveCrashInfoFile(ex);

            new Thread("crashThread") {
                @Override
                public void run() {
                    Looper.prepare();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.canDrawOverlays(nContext)&&nShowJavaCrashDialog) {
                            showDialog();
                        }else{
                            showToastAndSleep();
                        }
                    }else{
                        if (nShowJavaCrashDialog) {
                            showDialog();
                        }else{
                            showToastAndSleep();
                        }

                    }

                    Looper.loop();
                }
            }.start();
            // 阻塞主线程，是为了在dialog关闭之后，在主线程中回掉callback
            // 如果在"crashThread"子线程中调用Callback，则在Callback中不能再新建子线程进行上传文件操作
           while (!isFinishBlockMainThread){
                synchronized (lock){
                    lock.wait(3000);
                }
            }
            // 主线程中回调callback
            revokeCallback(thread,ex);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return crashFileFullPath;
    }

    private void unLock(){
        isFinishBlockMainThread = true;
        synchronized (lock){
            lock.notify();
        }
    }
    private void showToastAndSleep(){
        Toast.makeText(nContext, "很抱歉,出现不可预知的异常.", Toast.LENGTH_LONG).show();
        // 不加此行，如果在callback中马上关闭进程，就不会弹出toast
        SystemClock.sleep(3000);
        unLock();
    }
    private void revokeCallback(Thread thread, Throwable ex){
        if (crashCallbackList.size() == 0) {
            if (defaultCrashCallback != null) {
                defaultCrashCallback.callback(thread, ex);
                defaultCrashCallback.callback(thread,ex,errorMsg);
            }
            nDefaultHandler.uncaughtException(thread, ex);

        } else {
            for (CrashCallback crashCallback : crashCallbackList) {
                // 为了兼容之前版本，暂不删除
                crashCallback.callback(thread, ex);
                crashCallback.callback(thread,ex,errorMsg);
            }
        }
    }
    /**
     * 收集设备参数信息
     */
    private void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), 0);
            if (pi != null) {
                String versionName = pi.versionName + "";
//                String versionCode = pi.versionCode + "";
//                nInfos.put("versionName", versionName);
                nInfos.append("版本号:").append(versionName).append(" ");
            }
        } catch (Exception e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }

        //获取设备信息
        nInfos.append("终端型号:").append(SystemUtil.getModel()).append("\n")
        .append("Android ").append(Build.VERSION.RELEASE).append(" ")
        .append("ROM版本:").append(SystemUtil.getVersionName()).append("\n ");


    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    protected String saveCrashInfoFile(Throwable ex) throws Exception {
        StringBuffer sb = new StringBuffer();
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
            String date = sDateFormat.format(new Date());
            sb.append("\r\n" + date ).append(" ").append(nInfos);
//            for (Map.Entry<String, String> entry : nInfos.entrySet()) {
//                String key = entry.getKey();
//                String value = entry.getValue();
//                sb.append(key + "=" + value + "\n");
//            }

            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.flush();
            printWriter.close();
            String result = writer.toString();
            sb.append(result);

            String fullPath = writeFile(sb.toString());
            return fullPath;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);

            try {
                return writeFile(sb.toString());
            } finally {

            }

        }

    }

    /**
     * 保存异常信息到sd卡
     *
     * @param sb
     * @return
     * @throws
     */
    protected String writeFile(String sb) throws Exception {
        errorMsg = sb;
        String time = formatter.format(new Date());
        String fileName = "crash-" + time + ".log";
        String fullPath = "";
        if (SdCardUtil.isSDCardEnableByEnvironment()) {
            String path = getGlobalPath();
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            fullPath = path + fileName;
            File file = new File(fullPath);

            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(fullPath, true);
            fos.write(sb.getBytes());
            fos.flush();
            fos.close();
        }
        return fullPath;
    }

    public String getGlobalPath() {
        return SdCardUtil.getSDCardPathByEnvironment() + File.separator + "kedacom" + File.separator +
                (nContext != null ? (AppUtil.getPackageName(nContext) + File.separator + "crash") : "crash")
                + File.separator;
    }


    /**
     * 文件删除
     *
     * @param autoClearDay 文件保存天数
     */
    public void autoClear(final int autoClearDay) {
        FileUtil.deleteFilesInDirWithFilter(getGlobalPath(), new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                String s = FileUtil.getFileNameWithoutExtension(pathname);
                int day = autoClearDay < 0 ? autoClearDay : -1 * autoClearDay;
                String date = "crash-" + getOtherDay(day);
                return date.compareTo(s) >= 0;
            }
        });

    }

    private static String getOtherDay(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_YEAR, day);
        return formatter.format(calendar.getTime());
    }


    private static class LegoCrashHandlerInstance {

        private static LegoCrashHandler handler = new LegoCrashHandler();

    }


    public void setDefaultCrashCallback(CrashCallback crashCallback) {
        defaultCrashCallback = crashCallback;
    }

    private void showDialog() {
        final CrashDialog dialog = new CrashDialog(nContext,errorMsg);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0+
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }

        dialog.setCanceledOnTouchOutside(false);//设置点击屏幕其他地方，dialog不消失
        dialog.setCancelable(false);//设置点击返回键和HOme键，dialog不消失

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                unLock();
            }
        });
        dialog.show();
    }






}

