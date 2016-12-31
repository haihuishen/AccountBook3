package com.zhy.http.okhttp.builder;

import com.zhy.http.okhttp.request.PostFormRequest;
import com.zhy.http.okhttp.request.RequestCall;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class PostFormBuilder extends OkHttpRequestBuilder<PostFormBuilder> implements HasParamsable
{
    private List<FileInput> files = new ArrayList<>();

    @Override
    public RequestCall build() {
        return new PostFormRequest(url, tag, params, headers, files,id).build();
    }

    /**
     * 添加"文件集合"<p>
     * 将文件，文件名 和 表单的name,对应的放到 List<br>
     *
     * 返回的是：当前类——可以拿到List
     *
     * @param key           表单中的 name
     * @param files         文件的集合
     * @return
     */
    public PostFormBuilder files(String key, Map<String, File> files) {
        for (String filename : files.keySet()) {
            this.files.add(new FileInput(key, filename, files.get(filename)));
        }
        return this;
    }


    /**
     * 添加"文件集合"<p>
     * 将文件，文件名 和 表单的name,对应的放到 List<br>
     *
     * 返回的是：当前类PostFormBuilder——可以拿到List
     *
     * @param name          表单中的 name
     * @param filename      文件名
     * @param file          文件
     * @return
     */
    public PostFormBuilder addFile(String name, String filename, File file) {
        files.add(new FileInput(name, filename, file));
        return this;
    }


    /**
     * 内部类——文件的信息<p>
     * 获取:<br>
     * name              表单中的 name<br>
     * filename          文件名<br>
     * file               文件内容——File<br>
     */
    public static class FileInput {
        public String key;
        public String filename;
        public File file;

        /**
         *
         * @param name              表单中的 name
         * @param filename          文件名
         * @param file               文件内容——File
         */
        public FileInput(String name, String filename, File file) {
            this.key = name;
            this.filename = filename;
            this.file = file;
        }

        @Override
        public String toString() {
            //  FileInput{ key='xxx', filename='xxx', file= file }
            return "FileInput{" +
                    "key='" + key + '\'' +
                    ", filename='" + filename + '\'' +
                    ", file=" + file +
                    '}';
        }
    }


    /**
     * 拿到传递进来的参数(Map)
     *
     * 返回的是：当前类PostFormBuilder——可以拿到里面的参数
     *
     * @param params
     * @return
     */
    @Override
    public PostFormBuilder params(Map<String, String> params) {
        this.params = params;
        return this;
    }

    /**
     * 将 键值，放到Map中
     *
     * 返回的是：当前类PostFormBuilder——可以拿到里面的参数
     *
     * @param key
     * @param val
     * @return
     */
    @Override
    public PostFormBuilder addParams(String key, String val) {
        if (this.params == null) {
            params = new LinkedHashMap<>();
        }
        params.put(key, val);
        return this;
    }




}
