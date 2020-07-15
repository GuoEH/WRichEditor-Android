package cn.carbs.wricheditor.library.utils;

import android.util.Log;

public class LogUtil {

    public static void d(String tag, String message) {
        if (DebugUtil.DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void printMethodStack() {
        Log.d("method", Log.getStackTraceString(new Throwable()));
    }
}
