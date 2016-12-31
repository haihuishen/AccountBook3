package com.zhy.http.okhttp.utils;

/**
 * Exceptions 异常信息
 */
public class Exceptions {

    /**
     * 抛出IllegalArgumentException异常<p>
     *
     * IllegalArgumentException此异常表明向方法传递了一个不合法或不正确的参数。
     *
     * @param msg           错误信息
     * @param params        错误的参数(Object...  ——可省略)
     */
    public static void illegalArgument(String msg, Object... params) {
        // IllegalArgumentException此异常表明向方法传递了一个不合法或不正确的参数。
        // 你看看传值的方法是否参数不正确。
        throw new IllegalArgumentException(String.format(msg, params));
    }
}
