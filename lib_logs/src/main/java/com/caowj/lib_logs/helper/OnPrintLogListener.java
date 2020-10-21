package com.caowj.lib_logs.helper;

/**
 * @author wangqian
 * @date 2020/7/20
 */
public interface OnPrintLogListener {
    void onPrintLog(LogTypeEnum logType, String logMessage);
}
