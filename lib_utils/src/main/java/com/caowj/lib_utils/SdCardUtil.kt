package com.caowj.lib_utils;

import android.os.Environment;
import android.os.StatFs

/**
 * @author : yuanbingbing
 * @since : 2018/7/26 10:33
 *
 */
 object SdCardUtil {

    /**
     * SD 是否启用
     * @return true:启用  <br/> fasle:disabled
     */
    @JvmStatic
   fun isSDCardEnableByEnvironment():Boolean {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 获取SdCard 根路径
     * @return SdCard路径
     */
    @JvmStatic
    fun getSDCardPathByEnvironment():String {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return "";
    }

    /**
     * 获得sd卡剩余容量，即可用大小
     *
     * @return 字节
     */
    @JvmStatic
    fun getSDAvailableSize(): Long {
        val path = Environment.getExternalStorageDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.getBlockSizeLong()
        val availableBlocks = stat.getAvailableBlocksLong()
        return blockSize * availableBlocks
    }

}
