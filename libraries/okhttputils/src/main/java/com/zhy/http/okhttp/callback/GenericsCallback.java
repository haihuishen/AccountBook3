package com.zhy.http.okhttp.callback;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

import okhttp3.Response;

/**
 * class GenericsCallback<T> extends Callback<T> <p>
 *
 * class GenericsCallback &lt;T&gt; extends Callback&lt;T&gt;  <p>
 * Generics ：通用(复数)
 */
public abstract class GenericsCallback<T> extends Callback<T> {
    IGenericsSerializator mGenericsSerializator;

    public GenericsCallback(IGenericsSerializator serializator) {
        mGenericsSerializator = serializator;
    }

    /**
     * 解析网络响应<p>
     * "应该要"在"子线程/线程池"中运行<p>
     *
     *  通用的
     * @param response
     * @param id
     * @return              返回的是"T(泛型)"
     * @throws Exception
     */
    @Override
    public T parseNetworkResponse(Response response, int id) throws IOException {
        String string = response.body().string();

        // getClass().getGenericSuperclass()
        // 返回表示此 Class 所表示的实体（类、接口、基本类型或 void）的直接超类的 Type
        // 然后将其转换ParameterizedType。。
        // getActualTypeArguments()返回表示此类型实际类型参数的 Type 对象的数组。
        // [0]就是这个数组中第一个了。。
        // 简而言之就是获得超类的泛型参数的实际类型。。
        Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

        if (entityClass == String.class) {              // 如果是字符串直接返回字符串
            return (T) string;
        }
        T bean = mGenericsSerializator.transform(string, entityClass);
        return bean;
    }

}
