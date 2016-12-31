package com.zhy.http.okhttp.callback;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;


/**
 * 回调类(处理网络请求返回的数据)——抽象类<p>
 *
 * 抽象类里面可以定义方法，也可以实现方法，interface只能定义；<br>
 * 抽象类的的方法在子类中可以不实现，也可以重写父类的方法。 而接口中的所有方法一定要实现，不可遗漏<br>
 * @param <T>
 */
public abstract class Callback<T> {
    /**
     * 执行"耗时任务"前，的准备<p>
     * 在"主线"程运行
     * @param request
     */
    public void onBefore(Request request, int id) {
    }

    /**
     * 执行"耗时任务"后干的事情<p>
     * 在"主线"程运行
     * @param
     */
    public void onAfter(int id) {
    }

    /**
     * 在"主线"程运行
     * @param progress
     */
    public void inProgress(float progress, long total , int id) {
    }

    /**
     * 验证响应——通过响应码<p>
     * if you parse reponse code in parseNetworkResponse, you should make this method return true.<br>
     * 如果你在"parseNetworkResponse"解析响应代码,你应该让这个方法返回true<br>
     * 如果响应码在 [200..300) 中，返回true;否则反之
     * @param response 响应
     * @return 如果响应码在 [200..300) 中，返回true;否则反之
     */
    public boolean validateReponse(Response response, int id) {
        return response.isSuccessful();
    }

    /**
     * 解析网络响应<p>
     *
     * "应该要"在"子线程/线程池"中运行<br>
     * Thread Pool Thread <br>
     *
     * @param response
     */
    public abstract T parseNetworkResponse(Response response, int id) throws Exception;

    public abstract void onError(Call call, Exception e, int id);

    public abstract void onResponse(T response, int id);


    /*************************************************************************************/
    /*************************************************************************************/
    /**
     *  实例化一个"静态"的"本类——new Callback()" <p>
     *  有三个"方法可重写":<br>
     *  一、public Object parseNetworkResponse(Response response, int id) throws Exception <br>
     *  二、public void onError(Call call, Exception e, int id) <br>
     *  三、public void onResponse(Object response, int id) <br>
     */
    public static Callback CALLBACK_DEFAULT = new Callback() {

        @Override
        public Object parseNetworkResponse(Response response, int id) throws Exception {
            return null;
        }

        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(Object response, int id) {

        }
    };

}