package com.zhy.http.okhttp.request;

import java.util.Map;

import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Get方式，网络请求<p>
 *
 * 里面的参数是通过 OkHttpRequest自定义类 —— 获取"Request请求"<p>
 * ——extends OkHttpRequest <br>
 *
 * 构造函数：传入"对应的参数".<p>
 * 使用 buildRequest(RequestBody requestBody) 获取——Request<br>
 */
public class GetRequest extends OkHttpRequest
{
    /**
     * 构造函数<p>
     * 因为 extends OkHttpRequest <br>
     * 所以这里面的——super(url, tag, params, headers,id);  // 就是执行，OkHttpRequest的构造函数
     * @param url           url
     * @param tag           ???
     * @param params        Map键值对——参数
     * @param headers       Map键值对——请求头信息
     * @param id
     */
    public GetRequest(String url,
                      Object tag,
                      Map<String, String> params,
                      Map<String, String> headers,
                      int id) {
        super(url, tag, params, headers,id);           // 就是执行，OkHttpRequest的构造函数
    }

    /**
     * 因为Get网络请求——参数都在url中，所以不需要用到RequestBody存放参数
     * @return
     */
    @Override
    protected RequestBody buildRequestBody() {
        return null;
    }

    /**
     * 获取"Request类(请求)" —— OkHttp.jar
     * @param requestBody 请求参数(数据)载体
     * @return
     */
    @Override
    protected Request buildRequest(RequestBody requestBody) {
        // builder —— OkHttpRequest 中声明的
        // protected Request.Builder builder = new Request.Builder(); —— OkHttp.jar
        return builder.get().build();
    }


}
