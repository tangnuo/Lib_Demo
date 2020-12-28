package com.caowj.lib_logs;

import android.util.Log;

public class LogUtil {
    public static final String TAG = "caowj";

    private static final boolean DEBUG = !BuildConfig.DEBUG;//发布之后都是false
    private static final int maxLength = 3500;

    public static void v(String msg) {
        if (DEBUG) {
            for (int index = 0; index < msg.length(); index += maxLength) {
                String sub;
                if (msg.length() <= index + maxLength) {
                    sub = msg.substring(index);
                } else {
                    sub = msg.substring(index, index + maxLength);
                }
                Log.v(TAG, sub);
            }
        }
    }

    public static void v(String tag, String msg) {
        if (DEBUG) {
            for (int index = 0; index < msg.length(); index += maxLength) {
                String sub;
                if (msg.length() <= index + maxLength) {
                    sub = msg.substring(index);
                } else {
                    sub = msg.substring(index, index + maxLength);
                }
                Log.v(TAG + "." + tag, sub);
            }
        }
    }

    public static void d(String msg) {
        if (DEBUG) {
            for (int index = 0; index < msg.length(); index += maxLength) {
                String sub;
                if (msg.length() <= index + maxLength) {
                    sub = msg.substring(index);
                } else {
                    sub = msg.substring(index, index + maxLength);
                }
                Log.d(TAG, sub);
            }
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            for (int index = 0; index < msg.length(); index += maxLength) {
                String sub;
                if (msg.length() <= index + maxLength) {
                    sub = msg.substring(index);
                } else {
                    sub = msg.substring(index, index + maxLength);
                }
                Log.d(TAG + "." + tag, sub);
            }
        }
    }

    public static void i(String msg) {
        if (DEBUG) {
            //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
            //  把4*1024的MAX字节打印长度改为2001字符数
            int max_str_length = 2001 - TAG.length();
            //大于4000时
            while (msg.length() > max_str_length) {
                Log.i(TAG, msg.substring(0, max_str_length));
                msg = msg.substring(max_str_length);
            }
            //剩余部分
            Log.i(TAG, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
            //  把4*1024的MAX字节打印长度改为2001字符数
            int max_str_length = 2001 - tag.length();
            //大于4000时
            while (msg.length() > max_str_length) {
                Log.i(tag, msg.substring(0, max_str_length));
                msg = msg.substring(max_str_length);
            }
            //剩余部分
            Log.i(tag, msg);
        }
    }

    public static void w(String msg) {
        if (DEBUG) {
            Log.w(TAG, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(TAG + "." + tag, msg);
        }
    }

    public static void w(String msg, Throwable tr) {
        if (DEBUG) {
            Log.w(TAG, msg, tr);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.w(TAG + "." + tag, msg, tr);
        }
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(TAG + "." + tag, msg);
    }

    public static void e(String msg, Throwable tr) {
        Log.e(TAG, msg, tr);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(TAG + "." + tag, msg, tr);
    }
}