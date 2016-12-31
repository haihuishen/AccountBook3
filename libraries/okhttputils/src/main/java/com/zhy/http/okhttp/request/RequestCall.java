package com.zhy.http.okhttp.request;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 自定义RequestCall<p>
 * 对OkHttpRequest的封装，对外提供更多的接口：cancel(),readTimeOut()...<br>
 *
 *
 */
public class RequestCall
{
    private OkHttpRequest okHttpRequest;
    private Request request;                                        // OkHttp.jar
    private Call call;                                              // OkHttp.jar

    private long readTimeOut;
    private long writeTimeOut;
    private long connTimeOut;

    private OkHttpClient clone;                                     // 克隆;复制


    /**
     * 构造函数:获取传递过来的——OkHttpRequest
     * @param request
     */
    public RequestCall(OkHttpRequest request) {
        this.okHttpRequest = request;
    }

    /**
     * 设置"读取数据超时"
     * @param readTimeOut
     * @return
     */
    public RequestCall readTimeOut(long readTimeOut) {
        this.readTimeOut = readTimeOut;
        return this;
    }

    /**
     * 设置"写数据超时"
     * @param writeTimeOut
     * @return
     */
    public RequestCall writeTimeOut(long writeTimeOut) {
        this.writeTimeOut = writeTimeOut;
        return this;
    }

    /**
     * 设置"链接超时时间"
     * @param connTimeOut
     * @return
     */
    public RequestCall connTimeOut(long connTimeOut) {
        this.connTimeOut = connTimeOut;
        return this;
    }


    /**
     *
     * 将"读、写、链接超时"，设置到OkHttpClient()<p>
     * 使用OkHttpClient() + Request 生成===Call
     * @param callback  RequestCall类中可以获得"读、写、链接超时"
     * @return
     */
    public Call buildCall(Callback callback) {
        request = generateRequest(callback);

        if (readTimeOut > 0 || writeTimeOut > 0 || connTimeOut > 0) {
            readTimeOut = readTimeOut > 0 ? readTimeOut : OkHttpUtils.DEFAULT_MILLISECONDS;
            writeTimeOut = writeTimeOut > 0 ? writeTimeOut : OkHttpUtils.DEFAULT_MILLISECONDS;
            connTimeOut = connTimeOut > 0 ? connTimeOut : OkHttpUtils.DEFAULT_MILLISECONDS;

            clone = OkHttpUtils.getInstance().getOkHttpClient().newBuilder()
                    .readTimeout(readTimeOut, TimeUnit.MILLISECONDS)
                    .writeTimeout(writeTimeOut, TimeUnit.MILLISECONDS)
                    .connectTimeout(connTimeOut, TimeUnit.MILLISECONDS)
                    .build();

            call = clone.newCall(request);
        } else {
            call = OkHttpUtils.getInstance().getOkHttpClient().newCall(request);
        }
        return call;
    }

    /**
     * 使用自定义Callback —— 生成Request(请求)——返回Request
     * @param callback
     * @return
     */
    private Request generateRequest(Callback callback) {
        return okHttpRequest.generateRequest(callback);
    }

    /**
     *
     * @param callback 回调——"耗时任务"
     */
    public void execute(Callback callback) {
        buildCall(callback);

        if (callback != null) {
            callback.onBefore(request, getOkHttpRequest().getId());
        }

        OkHttpUtils.getInstance().execute(this, callback);
    }

    /**
     * 获取  Call——OkHttp.jar
     * @return
     */
    public Call getCall() {
        return call;
    }

    /**
     * 获取  Request——OkHttp.jar
     * @return
     */
    public Request getRequest() {
        return request;
    }

    /**
     * 获取  OkHttpRequest——自定义的
     * @return
     */
    public OkHttpRequest getOkHttpRequest() {
        return okHttpRequest;
    }

    /**
     * 开始请求 —— 使用 OkHttp.jar 的Call —— 执行"耗时任务"<p>
     * 执行本类的 buildCall(null);方法<br>
     * Call.execute() ——  返回的是Response  —— OkHttp.jar<br>
     * @return
     * @throws IOException
     */
    public Response execute() throws IOException {
        buildCall(null);
        return call.execute();
    }

    /**
     * 结束"请求"
     */
    public void cancel() {
        if (call != null) {
            call.cancel();
        }
    }


}
