package com.shen.accountbook3.Utils;

import android.os.Environment;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by shen on 1/3 0003.
 */
public class MyOkHttpUtils {

    /** 上传"json"的数据*/
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    /** 上传"文件"的数据*/
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    /** 上传"Image"*/
    public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    public OkHttpClient mOkHttpClient;
    public static MyOkHttpUtils mInstance;
    //public Handler mHandler;
    public static Gson mGson;


    /********************************** 初始化 OkHttpClient  ******************************************/
    /**
     * 设置缓存，超时什么的
     * 通过OkHttpClient.Builder来设置，
     * 通过builder配置好OkHttpClient后用builder.build()来返回OkHttpClient，
     * 所以我们通常不会调用new OkHttpClient()来得到OkHttpClient，而是通过builder.build()：
     */
    private  MyOkHttpUtils() {

        File sdcache = Environment.getDownloadCacheDirectory();
        int cacheSize = 10 * 1024 * 1024;
        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)                       // 设置连接的超时时间
                .writeTimeout(20, TimeUnit.SECONDS)                         // 设置响应的超时时间
                .readTimeout(20, TimeUnit.SECONDS) ;                         // 请求的超时时间
        //.cache(new Cache(sdcache.getAbsoluteFile(), cacheSize));

        // 获取实力对象
        mOkHttpClient =  okHttpClient.build();

        // 允许使用Cookie
        //        mOkHttpClient.Builder().setCookieHandler(new CookieManager(null,
        //                CookiePolicy.ACCEPT_ORIGINAL_SERVER));
        // 获取主线程的handler
        // mHandler = new Handler(Looper.getMainLooper());
        // mHandler = AccountBookApplication.getHandler();
        // 初始化gson
        mGson = new Gson();
    }


    /**
     * 该方法通过单例获取对象
     */
    public static MyOkHttpUtils getInstence() {
        if (mInstance == null) {
            synchronized (MyOkHttpUtils.class) {
                if (mInstance == null) {
                    mInstance = new MyOkHttpUtils();
                }
            }
        }
        return mInstance;
    }


    //  if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
    /**************************************  同步 *******************************************/
    /**
     * 同步<p>
     * 该方法为Get请求<br>
     *
     * @param url
     */
    public Response requestGet(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = mOkHttpClient.newCall(request).execute();

        return response;
    }

    /**
     * 同步<p>
     * 该方法为Post请求<br>
     * 提交"键值对"
     *
     * @param url
     * @param params
     */
    public Response requestPost(String url, List<Param> params) throws IOException {
        Request request = bulidRequest(url, params);            // 添加参数
        Response response = mOkHttpClient.newCall(request).execute();

        return response;
    }


    /**
     * 同步<p>
     * 该方法为Post请求<br>
     * 提交" File 或是 Json(实际是String)"
     *
     * @param url
     * @param mediaType            提交类型
     * @param data                  File 或是 Json(实际是String)
     */
    public Response requestPost(String url,MediaType mediaType, String data) throws IOException {
        Request request = bulidRequest(url, mediaType, data);            // 添加参数
        Response response = mOkHttpClient.newCall(request).execute();

        return response;
    }

/****************************************************************************************************/


    /************************  实现 Callback——异步 *****************************/
    /**
     * 异步<p>
     * 该方法为Get请求<br>
     *
     * @param url
     * @param callback
     */
    public void requestGetAsyn(String url, Callback callback) {
        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    /**
     * 异步<p>
     * 该方法为Post请求<br>
     * 提交"键值对"
     *
     * @param url
     * @param params
     * @param callback
     */
    public void requestPostAsyn(String url, List<Param> params, Callback callback) {
        Request request = bulidRequest(url, params);            // 添加参数
        mOkHttpClient.newCall(request).enqueue(callback);
    }


    /**
     * 异步<p>
     * 该方法为Post请求<br>
     * 提交" File 或是 Json(实际是String)"
     *
     * @param url
     * @param mediaType            提交类型
     * @param data                  File 或是 Json(实际是String)
     * @param callback
     */
    public void requestPostAsyn(String url,MediaType mediaType, String data, Callback callback) {
        Request request = bulidRequest(url, mediaType, data);            // 添加参数
        mOkHttpClient.newCall(request).enqueue(callback);
    }


    /********************************* post 添加参数 ************************************/

    /**
     * 该方法用来对Post请求的request添加请求参数<p>
     *
     * @param url
     * @param mediaType
     * @param data
     */
    private Request bulidRequest(String url, MediaType mediaType, String data) {
        RequestBody requestBody = null;
        if (mediaType == JSON) {
            requestBody = RequestBody.create(mediaType, data);

        } else if(mediaType == MEDIA_TYPE_PNG ){

//            requestBody = new MultipartBody.Builder()
//                    .setType(MultipartBody.FORM)
//                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"title\""),                    // 一般数据
//                            RequestBody.create(null, "Square Logo"))
//                    .addFormDataPart("files", null, new MultipartBody.Builder()                                     // 复杂数据：参数一：name   Servlet使用getFieldName()获取
//                            .addPart(Headers.of("Content-Disposition", "form-data; filename=\"head_img.png\""),      // 复杂数据  filename Servlet使用getName()获取
//                                    RequestBody.create(MEDIA_TYPE_PNG, (File) object))                              // 添加多张图片，在这个表单中添加多一个 addPart
//                            .build())
//                    .build();

            String fileName = data.substring(data.lastIndexOf('/')+1);      // 获取路径中的"文件名"
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("files", null, new MultipartBody.Builder()                                     // 复杂数据：参数一：name   Servlet使用getFieldName()获取
                            .addPart(Headers.of("Content-Disposition", "form-data; filename=\""+fileName+"\""),      // 复杂数据  filename Servlet使用getName()获取
                                    RequestBody.create(mediaType, new File(data)))                              // 添加多张图片，在这个表单中添加多一个 addPart
                            .build())
                    .build();

        }else if(mediaType == MEDIA_TYPE_MARKDOWN ){
            String fileName = data.substring(data.lastIndexOf('/')+1);      // 获取路径中的"文件名"

            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("files", null, new MultipartBody.Builder()                                     // 复杂数据：参数一：name   Servlet使用getFieldName()获取
                            .addPart(Headers.of("Content-Disposition", "form-data; filename=\""+fileName+"\""),      // 复杂数据  filename Servlet使用getName()获取
                                    RequestBody.create(mediaType, new File(data)))                              // 添加多张图片，在这个表单中添加多一个 addPart
                            .build())
                    .build();
        }

        return new Request.Builder().url(url).post(requestBody).build();
    }


    /**
     * 该方法用来对Post请求的request添加请求参数<p>
     *
     * @param url
     * @param params
     */
    private Request bulidRequest(String url, List<Param> params) {

        FormBody.Builder formBody = new FormBody.Builder();
        for (Param param : params) {
            formBody.add(param.getKey(), param.getValue());
        }
        RequestBody requestBody = formBody.build();

        return new Request.Builder().url(url).post(requestBody).build();
    }

    /********************************* 将json数据弄成 javabean ************************************/

    /**
     * 将json数据弄成 javabean
     * @param response  响应
     * @param clazz     javabean
     * @return
     * @throws IOException
     */
    public static Object fromJson(Response response, Class clazz) throws IOException {
        String json = response.body().string();
        Object object = mGson.fromJson(json, clazz);

        return object;
    }

    /****************************************** post 参数 *********************************************/

    // 该类是携带参数的类
    public static class Param {
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        String key;
        String value;

        public Param() {}

        public Param(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
