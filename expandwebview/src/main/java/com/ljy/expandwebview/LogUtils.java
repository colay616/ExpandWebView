package com.ljy.expandwebview;

import android.util.Log;

/**
 * 描   述:
 * 作   者:lijiayan_mail@163.com
 * 创建日期:2017/08/01 13:49
 * 修改历史:
 */
public class LogUtils {
    private final static boolean isDebug = BuildConfig.DEBUG;
    private final static String TAG = LogUtils.class.getSimpleName();

    public static void e(String msg) {
        if (isDebug) Log.e(TAG, msg);
    }

    public static void d(String msg) {
        if (isDebug) Log.d(TAG, msg);
    }
}
