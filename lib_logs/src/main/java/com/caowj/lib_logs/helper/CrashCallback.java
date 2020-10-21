package com.caowj.lib_logs.helper;

/**
 * App Crash回调监听
 *
 * @author : 袁兵兵
 * @version : 0.5.7
 * @since : 2019-10-10
 */
public interface CrashCallback {

    /**
     * 异常回调执行
     *
     * @param thread 异常线程
     * @param ex     异常类
     * @deprecated 请使用 {@link #callback(Thread, Throwable, String)} 来代替,
     */
    @Deprecated
    default void callback(Thread thread, Throwable ex) {

    }

    /**
     * 异常回调执行
     *
     * @param thread
     * @param ex
     * @param crashLogInfo 此次crash日志内容
     */
    default void callback(Thread thread, Throwable ex, String crashLogInfo) {

    }
}
