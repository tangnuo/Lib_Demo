package com.caowj.lib_utils

/**
 * Created by wangqian on 2019/12/20.
 */
object Util{

    @JvmStatic
    fun <T> checkNotNull(`object`: T?, message: String): T {
        if (`object` == null) {
            throw NullPointerException(message)
        }
        return `object`
    }
}