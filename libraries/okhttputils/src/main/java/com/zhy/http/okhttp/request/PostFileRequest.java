package com.zhy.http.okhttp.request;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.utils.Exceptions;

import java.io.File;
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
public class PostFileRequest extends OkHttpRequest
{
    /** 媒体类型  */
    private static MediaType MEDIA_TYPE_STREAM = MediaType.parse("application/octet-stream");

    private File file;
    private MediaType mediaType;

    /**
     * @param url               路径
     * @param tag
     * @param params            Map键值对——参数
     * @param headers           Map键值对——请求头信息
     * @param file              文件
     * @param mediaType         媒体类型
     * @param id
     */
    public PostFileRequest(String url,
                           Object tag,
                           Map<String, String> params,
                           Map<String, String> headers,
                           File file,
                           MediaType mediaType,
                           int id) {
        super(url, tag, params, headers,id);
        this.file = file;
        this.mediaType = mediaType;

        if (this.file == null) {
            Exceptions.illegalArgument("the file can not be null !");
        }
        if (this.mediaType == null) {                              // 媒体类型为"null",使用"默认"
            this.mediaType = MEDIA_TYPE_STREAM;
        }
    }

    /**
     * 根据"媒体类型"和"文件"，创建"请求体"内容;返回"请求体"
     * @return
     */
    @Override
    protected RequestBody buildRequestBody() {
        return RequestBody.create(mediaType, file);
    }


    /**
     * 有回调,计算上传进度
     * @param requestBody
     * @param callback
     *
     * @return
     */
    @Override
    protected RequestBody wrapRequestBody(RequestBody requestBody, final Callback callback) {
        if (callback == null)
            return requestBody;

        CountingRequestBody countingRequestBody = new CountingRequestBody(requestBody, new CountingRequestBody.Listener() {

            // 计算上传进度
            @Override
            public void onRequestProgress(final long bytesWritten, final long contentLength) {

                OkHttpUtils.getInstance().getDelivery().execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.inProgress(bytesWritten * 1.0f / contentLength,contentLength,id);
                    }
                });

            }
        });
        return countingRequestBody;
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
