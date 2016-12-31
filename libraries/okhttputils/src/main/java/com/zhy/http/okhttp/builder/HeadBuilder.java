package com.zhy.http.okhttp.builder;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.request.OtherRequest;
import com.zhy.http.okhttp.request.RequestCall;

/**
 *
 */
public class HeadBuilder extends GetBuilder
{
    @Override
    public RequestCall build()
    {
        return new OtherRequest(null, null, OkHttpUtils.METHOD.HEAD, url, tag, params, headers,id).build();
    }
}
