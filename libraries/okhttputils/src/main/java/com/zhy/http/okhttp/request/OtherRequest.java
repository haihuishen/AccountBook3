package com.zhy.http.okhttp.request;

import android.text.TextUtils;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.utils.Exceptions;

import java.util.Map;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.internal.http.HttpMethod;

/**
 *
 */
public class OtherRequest extends OkHttpRequest
{
    /** 媒体类型  */
    private static MediaType MEDIA_TYPE_PLAIN = MediaType.parse("text/plain;charset=utf-8");

    private RequestBody requestBody;
    private String method;
    private String content;

    /**
     *
     * @param requestBody
     * @param content
     * @param method            上传类型——
     * @param url               路径
     * @param tag
     * @param params            Map键值对——参数
     * @param headers           Map键值对——请求头信息
     * @param id
     */
    public OtherRequest(RequestBody requestBody,
                        String content,
                        String method,
                        String url,
                        Object tag,
                        Map<String, String> params,
                        Map<String, String> headers,
                        int id) {

        super(url, tag, params, headers,id);

        this.requestBody = requestBody;
        this.method = method;
        this.content = content;
    }

    /**
     * 如果"请求体"，"内容"，"上传方式" 都为空;<br>
     * 会抛出异常——illegalArgument("requestBody and content can not be null in method:" + method);<p>
     *
     * requestBody = RequestBody.create(MEDIA_TYPE_PLAIN, content);
     *
     * @return
     */
    @Override
    protected RequestBody buildRequestBody() {
        if (requestBody == null && TextUtils.isEmpty(content) && HttpMethod.requiresRequestBody(method)) {
            Exceptions.illegalArgument("requestBody and content can not be null in method:" + method);
        }

        if (requestBody == null && !TextUtils.isEmpty(content)) {
            requestBody = RequestBody.create(MEDIA_TYPE_PLAIN, content);
        }

        return requestBody;
    }


    /**
     * 获取"Request"<p>
     * 里面还传递了"month"
     * @param requestBody
     * @return
     */
    @Override
    protected Request buildRequest(RequestBody requestBody) {
        if (method.equals(OkHttpUtils.METHOD.PUT)) {
            builder.put(requestBody);

        } else if (method.equals(OkHttpUtils.METHOD.DELETE)) {
            if (requestBody == null)
                builder.delete();
            else
                builder.delete(requestBody);

        } else if (method.equals(OkHttpUtils.METHOD.HEAD)) {
            builder.head();

        } else if (method.equals(OkHttpUtils.METHOD.PATCH)) {
            builder.patch(requestBody);
        }

        return builder.build();
    }

}
