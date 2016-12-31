package com.zhy.http.okhttp.utils;

import android.util.Log;

/**
 * 日志输出
 */
public class L
{
    private static boolean debug = false;

    public static void e(String msg)
    {
        if (debug)
        {
            Log.e("OkHttp", msg);
        }
    }

}

