package com.shen.accountbook3.Utils;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * ClassName:OkHttpUtils Function:
 *
 * @deprecated 通过封装OkHttp进行的网络请求的GET POST工具类
 */
public class OkHttpUtils
{
    public OkHttpClient mOkHttpClient;
    public static OkHttpUtils mInstance;
    public Handler mHandler;
    public Gson mGson;


    /**
     * 设置缓存，超时什么的
     * 通过OkHttpClient.Builder来设置，
     * 通过builder配置好OkHttpClient后用builder.build()来返回OkHttpClient，
     * 所以我们通常不会调用new OkHttpClient()来得到OkHttpClient，而是通过builder.build()：
     */
    private OkHttpUtils() {

        File sdcache = Environment.getDownloadCacheDirectory();
        int cacheSize = 10 * 1024 * 1024;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)                       // 设置连接的超时时间
                .writeTimeout(20, TimeUnit.SECONDS)                         // 设置响应的超时时间
                .readTimeout(20, TimeUnit.SECONDS)                          // 请求的超时时间
                .cache(new Cache(sdcache.getAbsoluteFile(), cacheSize));

        // 获取实力对象
         mOkHttpClient=builder.build();

        // 允许使用Cookie
        //        mOkHttpClient.Builder().setCookieHandler(new CookieManager(null,
        //                CookiePolicy.ACCEPT_ORIGINAL_SERVER));
        // 获取主线程的handler
        mHandler = new Handler(Looper.getMainLooper());
        // 初始化gson
        //mGson = new Gson();
    }


    /**
    * 该方法通过单例获取对象
    */
    public static OkHttpUtils getInstence() {
        if (mInstance == null) {
            synchronized (OkHttpUtils.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpUtils();
                }
            }
        }
        return mInstance;
    }


    /**
     * 该方法为Get请求<p>
     *
     * @param url
     * @param callback
     */
    public void requestGet(String url, ResultCallback callback) {
        Request request = new Request.Builder().url(url).build();
        deliveryResult(callback, request);
    }

    /**
     * 该方法为Post请求<p>
     *
     * @param url
     * @param callback
     * @param params
     */
    public void requestPost(String url, ResultCallback callback,List<Param> params) {
        Request request = bulidRequest(url, params);            // 添加参数
        deliveryResult(callback, request);
    }


    /**
     * 该方法用来对Post请求的request添加请求参数<p>
     *
     * @param url
     * @param params
     */
    private Request bulidRequest(String url, List<Param> params) {
        FormBody.Builder builder = new FormBody.Builder();
        // FormEncodingBuilder builder = new FormEncodingBuilder();
        for (Param param : params) {
            builder.add(param.key, param.value);
        }
        RequestBody requestBody = builder.build();

        return new Request.Builder().url(url).patch(requestBody).build();
    }

    /**
     * 该方法是传递结果
     *
     * @param callback
     * @param request
     */
    private void deliveryResult(final ResultCallback callback, Request request) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                sendFailureCallBack(callback, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                sendSuccessCallBack(callback, response);
                // 请求成功时返回的结果
//                try {
//                    String str = response.body().toString();
//                    LogUtils.i("response:" + str);
//                    //sendSuccessCallBack(callback, str);
//                    if (callback.mType == String.class) {
//                        LogUtils.i("deliveryResult : callback.mType == String.class");
//                        ///sendSuccessCallBack(callback, str);
//                    } else {
//                        LogUtils.i("deliveryResult :  else");
//                        //Object object = mGson.fromJson(str, callback.mType);
//                        //sendSuccessCallBack(callback, object);
//                    }
//                } catch (Exception e) {
//                    //sendFailureCallBack(callback, e);
//                }
            }
        });
    }


    /**
     * 该方法是请求 失败后对返回的结果进行处理
     */
    protected void sendFailureCallBack(final ResultCallback callback, final Exception e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onFailse(e);
                }
            }
        });
    }


    /**
     * 该方法是请求成功后,将结果发送到主线程，对返回的结果进行处理
     */
    protected void sendSuccessCallBack(final ResultCallback callback, final Object obj) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onSuccess(obj);
                }
            }
        });
    }

    /**********************************************************************************************/
            // ////////////////////对外暴露的调用接口///////////////////////////////
    /**********************************************************************************************/


    public void get(String url, ResultCallback callback) {
        getInstence().requestGet(url, callback);
    }


    public void post(String url, ResultCallback callback, List<Param> params) {
        getInstence().requestPost(url, callback, params);
    }


    // 该类是对返回的结果进行处理
    public static abstract class ResultCallback<T> {
        Type mType;

        public ResultCallback() {
            mType = getSuperclassTypeParameter(getClass());
        }


        // 该静态代码块主要是通过类$Gson$Types进行泛型的确定
        static Type getSuperclassTypeParameter(Class<?> subclass) {
            Type superclass = subclass.getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }
            ParameterizedType parameterized = (ParameterizedType) superclass;
            return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
        }

        // 成功时的抽象方法，参数为返回的数据，此方法是在主线程中执行的
        public abstract void onSuccess(Object obj);

        public abstract void onFailse(Exception e);

    }


    // 该类是携带参数的类
    public static class Param {
        String key;
        String value;

        public Param() {}

        public Param(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
