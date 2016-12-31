package com.zhy.http.okhttp.request;

import com.zhy.http.okhttp.utils.Exceptions;

import java.util.Map;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 *
 * Post方式，网络请求<p>
 *
 * 里面的参数是通过 OkHttpRequest自定义类 —— 获取"Request请求"<p>
 * ——extends OkHttpRequest <br>
 *
 * 构造函数：传入"对应的参数".<p>
 * 使用 buildRequest(RequestBody requestBody) 获取——Request<br>
 */
public class PostStringRequest extends OkHttpRequest {
    /** 媒体类型  */
    private static MediaType MEDIA_TYPE_PLAIN = MediaType.parse("text/plain;charset=utf-8");

    private String content;
    private MediaType mediaType;


    public PostStringRequest(String url,
                             Object tag, Map<String, String> params,
                             Map<String, String> headers,
                             String content,
                             MediaType mediaType,
                             int id) {
        super(url, tag, params, headers,id);
        this.content = content;
        this.mediaType = mediaType;

        if (this.content == null) {
            Exceptions.illegalArgument("the content can not be null !");
        }
        if (this.mediaType == null) {
            this.mediaType = MEDIA_TYPE_PLAIN;
        }

    }

    /**
     * 获取请求体
     * @return
     */
    @Override
    protected RequestBody buildRequestBody() {
        return RequestBody.create(mediaType, content);
    }


    /**
     * 获取"Request类(请求)" —— OkHttp.jar<p>
     * 将requestBody，整合到Request
     *
     * @param requestBody 请求参数(数据)载体
     * @return
     */
    @Override
    protected Request buildRequest(RequestBody requestBody) {
        // builder —— OkHttpRequest 中声明的
        // protected Request.Builder builder = new Request.Builder(); —— OkHttp.jar
        return builder.post(requestBody).build();
    }


}
