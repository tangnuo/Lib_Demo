package com.caowj.lib_utils;

import android.os.Environment;

/**
 * @author : yuanbingbing
 * @since : 2018/7/26 10:33
 */
public class SdCardUtil {

    /**
     * SD 是否启用
     * @return true:启用
     */
    public static boolean isSDCardEnableByEnvironment() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 获取SdCard 根路径
     * @return SdCard路径
     */
    public static String getSDCardPathByEnvironment() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return "";
    }

}
