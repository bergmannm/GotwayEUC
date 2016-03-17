package app.gotway.euc.util;

import android.util.Log;

public class DebugLogger {
    public static void v(String tag, String text) {
        Log.v(tag, text);
    }

    public static void d(String tag, String text) {
        Log.d(tag, text);
    }

    public static void i(String tag, String text) {
        Log.i(tag, text);
    }

    public static void w(String tag, String text) {
        Log.w(tag, text);
    }

    public static void e(String tag, String text) {
        Log.e(tag, text);
    }

    public static void wtf(String tag, String text) {
        Log.wtf(tag, text);
    }
}
