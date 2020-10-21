package com.caowj.lib_utils;

import android.os.SystemClock;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author wangqian
 * @date 2020/8/14
 * 限制某些操作触发的频率，在设置的gapTimeMillis时间内只能执行一次操作，多则舍弃
 * 可用于防止recyclerView快速点击
 */
public class ThrottleUtil<T> {

    LinkedHashMap<T, Long> mLinkedHashMap;
    // 默认为1秒
    int mGapTimeMillis = 1000;
    public ThrottleUtil() {

    }
    public ThrottleUtil(int gapTimeMillis) {
        this.mGapTimeMillis = gapTimeMillis;
        mLinkedHashMap = new LinkedHashMap<>(0, 0.75f, true);
    }

    public boolean canRun(T id) {

        boolean result = false;
        Long time = mLinkedHashMap.get(id);
        if (time == null) {
            mLinkedHashMap.put(id, SystemClock.elapsedRealtime() + mGapTimeMillis);
            result = true;
        } else {
            if (SystemClock.elapsedRealtime() >= time) {// 过期
                mLinkedHashMap.put(id, SystemClock.elapsedRealtime() + mGapTimeMillis);
                result = true;
            } else {
                result = false;
            }
        }
        clearExpiredData();
        return result;

    }

    public void clear(){
        mLinkedHashMap.clear();
    }
    /**
     * 清理已经过期的数据
     */
    private void clearExpiredData() {
        Set<Map.Entry<T, Long>> entries = mLinkedHashMap.entrySet();
        Iterator<Map.Entry<T, Long>> entryIterator = entries.iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<T, Long> entry = entryIterator.next();
            // 因是升序排列，所以遇到一个不到时间的，后面的肯定也未到时间，就break
            if (SystemClock.elapsedRealtime() < entry.getValue()) {//未到时间
                break;
            } else {
                entryIterator.remove();
            }

        }
    }
//        private void debugValue(){
//
//            for (Map.Entry<T, Long> entry : linkedHashMap.entrySet()) {
//                LegoLog.e("ClickTimeSession",
//                        entry.getKey() + ":" + entry.getValue());
//            }
//        }

}
