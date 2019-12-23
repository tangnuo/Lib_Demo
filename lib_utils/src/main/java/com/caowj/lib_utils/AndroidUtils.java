package com.caowj.lib_utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.RequiresPermission;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.lang.reflect.Field;

/**
 * @author : wangqian,袁兵兵
 * @version : 0.5.7
 * @since : 2018-6-21
 */
public class AndroidUtils {

    private AndroidUtils() {

    }

    /**
     * 获得sd卡剩余容量，即可用大小
     *
     * @return 字节
     */
    public static long getSDAvailableSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return blockSize * availableBlocks;
    }

    /**
     * 获取网络连接状态，返回false-未连接，true-已连接
     *
     * @param connectivityManager
     * @return
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    public static boolean getNetState(ConnectivityManager connectivityManager) {
        NetworkInfo activeInfo = connectivityManager.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            return activeInfo.getType() == ConnectivityManager.TYPE_WIFI ||
                    activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;

        } else {
            return false;
        }
    }

    /**
     * px转dp
     *
     * @param context Android Context
     * @param pxValue
     * @return
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * dp转为px
     *
     * @param context  Android Context
     * @param dipValue dp数值
     * @return
     */
    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * px转sp
     *
     * @param context Android Context
     * @param pxValue
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * sp转px
     *
     * @param context Android Context
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 获取版本号
     *
     * @param context Android Context
     * @return 版本号
     */
    public static int getVersionCode(Context context) {
        PackageManager pManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = pManager.getPackageInfo(context.getPackageName(), 0);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo.versionCode;
    }

    /**
     * 获取版本名称
     *
     * @param context Android Context
     * @return 版本名称
     */
    public static String getVersionName(Context context) {
        PackageManager pManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = pManager.getPackageInfo(context.getPackageName(), 0);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo.versionName;
    }


    /**
     * 获取应用程序包名
     *
     * @param context Android Context
     * @return 应用程序包名
     * @since 0.4.7
     */
    public static String getPackageName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.applicationInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取是否为debug版本，当appid与menifest中配置的package不同时，此方法不生效
     *
     * @param context
     * @return
     */
    @Deprecated
    public static boolean isDebug(Context context) {
        try {
            Class clazz = Class.forName(context.getApplicationInfo().packageName + ".BuildConfig");
            Field field = clazz.getField("DEBUG");
            boolean isDebug = field.getBoolean(clazz);
            return isDebug;

        } catch (Exception e) {

            LegoLog.d("isDebug", e);
            return false;
        }
    }

    /**
     * 获取设备的屏幕高度（px）
     */
    public static int getScreenHeightPx(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.heightPixels;
    }

    /**
     * 获取设备的屏幕高度（dp）
     */
    public static int getScreenHeightDp(Context context) {
        return px2dp(context, getScreenHeightPx(context));
    }

    /**
     * 获取设备的屏幕宽度（px）
     */
    public static int getScreenWidthPx(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.widthPixels;
    }

    /**
     * 获取设备的屏幕宽度（dp）
     */
    public static int getScreenWidthDp(Context context) {
        return px2dp(context, getScreenWidthPx(context));
    }


}
