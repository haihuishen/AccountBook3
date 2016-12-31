package com.zhy.http.okhttp.callback;

/**
 * 通用接口？
 */
public interface IGenericsSerializator {
    <T> T transform(String response, Class<T> classOfT);
}
