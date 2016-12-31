package com.zhy.http.okhttp.callback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import okhttp3.Response;

/**
 * 回调类——网络请求结束:<br>
 * 将网络请求的数据，弄成"位图"
 */
public abstract class BitmapCallback extends Callback<Bitmap>
{
    /**
     * 解析网络响应<p>
     * "应该要"在"子线程/线程池"中运行<p>
     *
     * 将网络请求的数据，弄成"位图"
     * @param response
     * @param id
     * @return              返回的是"Bitmap"
     * @throws Exception
     */
    @Override
    public Bitmap parseNetworkResponse(Response response , int id) throws Exception
    {
        return BitmapFactory.decodeStream(response.body().byteStream());
    }

}
