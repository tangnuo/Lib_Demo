package com.caowj.lib_logs.helper;

public enum LogTypeEnum {
    Net(1, "网络日志"), Crash(2, "Crash日志"),
    Logcat(3, "Logcat日志"), Bussiness(4, "业务日志");

    private int value = 0;
    private String name = null;

    private LogTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int toValue() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }

    public static LogTypeEnum valueOf(int value) {
        LogTypeEnum[] logTypeEnums = values();
        for (LogTypeEnum logTypeEnum : logTypeEnums) {
            if (logTypeEnum.value == value) {
                return logTypeEnum;
            }
        }
        return null;
    }
}
