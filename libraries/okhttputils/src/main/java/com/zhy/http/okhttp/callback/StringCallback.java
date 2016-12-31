package com.zhy.http.okhttp.callback;

import java.io.IOException;

import okhttp3.Response;

/**
 * 回调类——网络请求结束:<br>
 * 将网络请求的数据，弄成"String"
 */
public abstract class StringCallback extends Callback<String>
{
    /**
     * 解析网络响应<p>
     * "应该要"在"子线程/线程池"中运行<p>
     *
     * 将网络请求的数据，弄成"String"
     * @param response
     * @param id
     * @return              返回的是"String"
     * @throws Exception
     */
    @Override
    public String parseNetworkResponse(Response response, int id) throws IOException
    {
        return response.body().string();
    }
}
