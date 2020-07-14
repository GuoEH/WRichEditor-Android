package cn.carbs.wricheditor.library.utils;

import android.util.Log;

public class LogUtil {

    public static final boolean DEBUG = true;

    public static void d(String tag, String message) {
        if (DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void printMethodStack() {
        Log.d("wangwang", Log.getStackTraceString(new Throwable()));
    }
}
