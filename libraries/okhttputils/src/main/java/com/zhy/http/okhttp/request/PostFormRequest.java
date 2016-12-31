package com.zhy.http.okhttp.request;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.Callback;

import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 上传表格——混合的数据，有String,也有文件<p>
 * Post方式，网络请求<p>
 *
 * 里面的参数是通过 OkHttpRequest自定义类 —— 获取"Request请求"<p>
 * ——extends OkHttpRequest <br>
 *
 * 构造函数：传入"对应的参数".<p>
 * 使用 buildRequest(RequestBody requestBody) 获取——Request<br>
 */
public class PostFormRequest extends OkHttpRequest {

    /** 文件列表*/
    private List<PostFormBuilder.FileInput> files;

    /**
     * @param url               路径
     * @param tag
     * @param params            Map键值对——参数
     * @param headers           Map键值对——请求头信息
     * @param files              文件s
     * @param id
     */
    public PostFormRequest(String url,
                           Object tag,
                           Map<String, String> params,
                           Map<String, String> headers,
                           List<PostFormBuilder.FileInput> files,
                           int id) {
        super(url, tag, params, headers,id);
        this.files = files;
    }

    /**
     * 拿到RequestBody
     * @return
     */
    @Override
    protected RequestBody buildRequestBody() {
        if (files == null || files.isEmpty()) {
            FormBody.Builder builder = new FormBody.Builder();
            addParams(builder);
            FormBody formBody = builder.build();
            return formBody;
        } else {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            addParams(builder);

            for (int i = 0; i < files.size(); i++) {
                PostFormBuilder.FileInput fileInput = files.get(i);
                RequestBody fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileInput.filename)), fileInput.file);
                builder.addFormDataPart(fileInput.key, fileInput.filename, fileBody);
            }
            return builder.build();
        }
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
        if (callback == null) return requestBody;
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


    @Override
    protected Request buildRequest(RequestBody requestBody) {
        return builder.post(requestBody).build();
    }

    /**
     * 推测"媒体类型"——返回"媒体类型"字符串
     * @param path
     * @return
     */
    private String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = null;
        try {
            contentTypeFor = fileNameMap.getContentTypeFor(URLEncoder.encode(path, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    /**
     * 添加表格内容
     * @param builder
     */
    private void addParams(MultipartBody.Builder builder) {
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + key + "\""),
                        RequestBody.create(null, params.get(key)));
            }
        }
    }

    /**
     * 如果全局变量——"params" 不为null,就将其，添加到FormBody.Builder中
     * @param builder
     */
    private void addParams(FormBody.Builder builder) {
        if (params != null) {
            for (String key : params.keySet()) {
                builder.add(key, params.get(key));
            }
        }
    }

}
