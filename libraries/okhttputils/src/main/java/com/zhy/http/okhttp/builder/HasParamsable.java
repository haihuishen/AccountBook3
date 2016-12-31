package com.zhy.http.okhttp.builder;

import java.util.Map;

/**
 * 接口
 */
public interface HasParamsable
{
    OkHttpRequestBuilder params(Map<String, String> params);
    OkHttpRequestBuilder addParams(String key, String val);
}
