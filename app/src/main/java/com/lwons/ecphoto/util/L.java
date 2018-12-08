package com.lwons.ecphoto.util;

import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by liuwons on 18-12-7.
 */
public class L {
    public static void e(@Nullable String tag, @Nullable String message) {
        Log.e(tag, message);
    }

    public static void d(@Nullable String tag, @Nullable String message) {
        Log.d(tag, message);
    }

    public static void i(@Nullable String tag, @Nullable String message) {
        Log.i(tag, message);
    }

    public static void v(@Nullable String tag, @Nullable String message) {
        Log.v(tag, message);
    }
}
