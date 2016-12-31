package com.zhy.http.okhttp;

import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.builder.HeadBuilder;
import com.zhy.http.okhttp.builder.OtherRequestBuilder;
import com.zhy.http.okhttp.builder.PostFileBuilder;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.builder.PostStringBuilder;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.request.RequestCall;
import com.zhy.http.okhttp.utils.Platform;

import java.io.IOException;
import java.util.concurrent.Executor;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * 重新封装的"OkHttp"的工具类
 */
public class OkHttpUtils {
    public static final long DEFAULT_MILLISECONDS = 10_000L;
    private volatile static OkHttpUtils mInstance;
    private OkHttpClient mOkHttpClient;
    private Platform mPlatform;

    /**
     * 重新封装的"OkHttp"的工具类——构造函数<p>
     * 如果 OkHttpClient == null  就实例化一个! <br>
     * 获取线程池——用于执行耗时任务
     * @param okHttpClient
     */
    public OkHttpUtils(OkHttpClient okHttpClient) {
        if (okHttpClient == null) {
            mOkHttpClient = new OkHttpClient();
        } else {
            mOkHttpClient = okHttpClient;
        }

        mPlatform = Platform.get();
    }

    /**
     * 本例子是:懒汉
     *
     * 单例中懒汉和饿汉的本质区别在于以下几点：
     *
     * 　1、饿汉式是线程安全的,在类创建的同时就已经创建好一个静态的对象供系统使用,以后不在改变。
     * 懒汉式如果在创建实例对象时不加上synchronized则会导致对对象的访问不是线程安全的。
     *      　　
     *    2、从实现方式来讲他们最大的区别就是
     *          懒汉式是延时加载,他是在需要的时候才创建对象,
     *          而饿汉式在虚拟机启动的时候就会创建，饿汉式无需关注多线程问题、写法简单明了、能用则用。
     * 但是它是加载类时创建实例、所以如果是一个工厂模式、缓存了很多实例、那么就得考虑效率问题，
     * 因为这个类一加载则把所有实例不管用不用一块创建。
     * @param okHttpClient
     * @return
     */
    public static OkHttpUtils initClient(OkHttpClient okHttpClient) {
        if (mInstance == null) {
            synchronized (OkHttpUtils.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpUtils(okHttpClient);
                }
            }
        }
        return mInstance;
    }

    /**
     * 重新封装的"OkHttp"的工具类——构造函数<p>
     * 如果 OkHttpClient == null  就实例化一个! <br>
     * 获取线程池——用于执行耗时任务
     * @return
     */
    public static OkHttpUtils getInstance() {
        return initClient(null);
    }

    /**
     * 获取线程池
     * @return
     */
    public Executor getDelivery() {
        return mPlatform.defaultCallbackExecutor();
    }

    /**
     * 获取 OkHttpClient
     * @return
     */
    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    /**
     *
     * @return
     */
    public static GetBuilder get() {
        return new GetBuilder();
    }

    public static PostStringBuilder postString() {
        return new PostStringBuilder();
    }

    public static PostFileBuilder postFile() {
        return new PostFileBuilder();
    }

    public static PostFormBuilder post() {
        return new PostFormBuilder();
    }

    public static OtherRequestBuilder put() {
        return new OtherRequestBuilder(METHOD.PUT);
    }

    public static HeadBuilder head() {
        return new HeadBuilder();
    }

    public static OtherRequestBuilder delete() {
        return new OtherRequestBuilder(METHOD.DELETE);
    }

    public static OtherRequestBuilder patch() {
        return new OtherRequestBuilder(METHOD.PATCH);
    }

    public void execute(final RequestCall requestCall, Callback callback) {
        if (callback == null)
            callback = Callback.CALLBACK_DEFAULT;
        final Callback finalCallback = callback;
        final int id = requestCall.getOkHttpRequest().getId();

        requestCall.getCall().enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                sendFailResultCallback(call, e, finalCallback, id);
            }

            @Override
            public void onResponse(final Call call, final Response response) {
                try {
                    if (call.isCanceled()) {
                        sendFailResultCallback(call, new IOException("Canceled!"), finalCallback, id);
                        return;
                    }

                    if (!finalCallback.validateReponse(response, id)) {
                        sendFailResultCallback(call, new IOException("request failed , reponse's code is : " + response.code()), finalCallback, id);
                        return;
                    }

                    Object o = finalCallback.parseNetworkResponse(response, id);
                    sendSuccessResultCallback(o, finalCallback, id);
                } catch (Exception e) {
                    sendFailResultCallback(call, e, finalCallback, id);
                } finally {
                    if (response.body() != null)
                        response.body().close();
                }

            }
        });
    }


    public void sendFailResultCallback(final Call call, final Exception e, final Callback callback, final int id) {
        if (callback == null) return;

        mPlatform.execute(new Runnable() {
            @Override
            public void run() {
                callback.onError(call, e, id);
                callback.onAfter(id);
            }
        });
    }

    public void sendSuccessResultCallback(final Object object, final Callback callback, final int id) {
        if (callback == null) return;
        mPlatform.execute(new Runnable() {
            @Override
            public void run() {
                callback.onResponse(object, id);
                callback.onAfter(id);
            }
        });
    }

    public void cancelTag(Object tag) {
        for (Call call : mOkHttpClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : mOkHttpClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    public static class METHOD {
        public static final String HEAD = "HEAD";
        public static final String DELETE = "DELETE";
        public static final String PUT = "PUT";
        public static final String PATCH = "PATCH";
    }
}

