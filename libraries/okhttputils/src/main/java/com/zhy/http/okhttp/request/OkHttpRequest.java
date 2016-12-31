package com.zhy.http.okhttp.request;

import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.utils.Exceptions;

import java.util.Map;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 自定义的OkHttpRequest请求<p>
 * 构造函数: 获取到 url,传参，请求头...<br>
 * 将得到的参数，设置进——Request.Builder (是OkHttp的)
 */
public abstract class OkHttpRequest
{
    protected String url;
    protected Object tag;
    protected Map<String, String> params;
    protected Map<String, String> headers;
    protected int id;

    /**
     * 获取——Request的"构建者" -- .Builder()相当于实例化？
     */
    protected Request.Builder builder = new Request.Builder();

    /**
     * 构造函数
     *
     * @param url           url
     * @param tag           ???
     * @param params        Map键值对——参数
     * @param headers       Map键值对——请求头信息
     * @param id
     */
    protected OkHttpRequest(String url,
                            Object tag,
                            Map<String, String> params,
                            Map<String, String> headers,
                            int id) {
        this.url = url;
        this.tag = tag;
        this.params = params;
        this.headers = headers;
        this.id = id ;

        if (url == null) {
            Exceptions.illegalArgument("url can not be null.");
        }

        initBuilder();
    }



    /**
     * 初始化一些基本参数 url , tag , headers
     */
    private void initBuilder() {
        // tag()
        //  高度的标签要求。它可以使用后取消请求。
        //  如果标签是不明或null,请求取消使用请求本身作为标记。
        builder.url(url).tag(tag);
        appendHeaders();
    }

    /**
     * 获取——RequestBody(请求体)<p>
     * abstract——抽象，子类实现
     */
    protected abstract RequestBody buildRequestBody();

    /**
     * 获取——RequestBody(请求体)<p>
     * 获取传递的 requestBody -- 好像没什么用
     * @param requestBody
     * @param callback      参数2又不使用？？？
     */
    protected RequestBody wrapRequestBody(RequestBody requestBody, final Callback callback) {
        return requestBody;
    }

    /**
     * 根据RequestBody获取——Request<p>
     * abstract——抽象，子类实现
     *
     * @param requestBody
     * @return
     */
    protected abstract Request buildRequest(RequestBody requestBody);


    /**
     * 里面将——OkHttpRequest，作为RequestCall构造函数的参数<p>
     *   return  new RequestCall(this);
     * @return
     */
    public RequestCall build() {
        return new RequestCall(this);
    }


    /**
     * 使用自定义Callback —— 生成Request(请求)——返回Request
     * @param callback
     * @return
     */
    public Request generateRequest(Callback callback) {
        RequestBody requestBody = buildRequestBody();
        RequestBody wrappedRequestBody = wrapRequestBody(requestBody, callback);
        Request request = buildRequest(wrappedRequestBody);
        return request;
    }


    /**
     * 加载请求头<p>
     * Headers.Builder headerBuilder <br>
     * builder.headers(headerBuilder.build());<br>
     */
    protected void appendHeaders() {
        Headers.Builder headerBuilder = new Headers.Builder();
        if (headers == null || headers.isEmpty()) return;

        for (String key : headers.keySet()) {
            headerBuilder.add(key, headers.get(key));
        }
        builder.headers(headerBuilder.build());
    }

    /**
     * 获取 id?
     * @return
     */
    public int getId() {
        return id  ;
    }

}
