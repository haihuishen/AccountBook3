package com.zhy.http.okhttp.request;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Decorates an OkHttp request body to count the number of bytes written when writing it. Can
 * decorate any request body, but is most useful for tracking the upload progress of large
 * multipart requests.<p>
 *
 * 装饰一个OkHttp请求体数写在写的字节数。可以装饰任何请求主体,但最有用的是跟踪大型多部分的上载进度要求。<br>
 *     CountingRequestBody extends RequestBody<p>
 *
 *     上传数据时，通过一个缓冲区 —— 可以通过这来"计算上传进度"
 *
 */
public class CountingRequestBody extends RequestBody {

    protected RequestBody delegate;
    protected Listener listener;

    protected CountingSink countingSink;                // 计算槽.

    public CountingRequestBody(RequestBody delegate, Listener listener) {
        this.delegate = delegate;
        this.listener = listener;
    }

    /**
     * RequestBody.contentType(); <p>
     * 获取"内容类型"
     *
     * @return
     */
    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    /**
     * RequestBody.contentLength(); <p>
     * 获取"内容长度"
     *
     * @return
     */
    @Override
    public long contentLength() {
        try {
            return delegate.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 上传数据时，通过一个缓冲区 —— 可以通过这来"计算上传进度"
     * @param sink
     * @throws IOException
     */
    @Override
    public void writeTo(BufferedSink sink) throws IOException {

        countingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(countingSink);

        delegate.writeTo(bufferedSink);

        bufferedSink.flush();
    }

    /**
     * 用于计算，"上传"的"进度" 的类<p>
     *
     * CountingSink extends ForwardingSink<p>
     *
     */
    protected final class CountingSink extends ForwardingSink {

        private long bytesWritten = 0;

        /**
         * 如果Sink为空，会抛出异常——IllegalArgumentException("delegate == null");
         * @param delegate Sink
         */
        public CountingSink(Sink delegate) {
            super(delegate);
        }

        /**
         * 根据——上传byteCount个字符，可能多次写——累计——计算进度条
         * @param source
         * @param byteCount
         * @throws IOException
         */
        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);

            bytesWritten += byteCount;
            listener.onRequestProgress(bytesWritten, contentLength());
        }
    }


    /**
     * Listener接口 —— 用于"进度"显示 —— 子类实现
     */
    public static interface Listener {

        /**
         * 上传/下载的进度<p>
         * 子类实现
         *
         * @param bytesWritten          已经上传/下载——的数据大小
         * @param contentLength         总上传/下载——的数据大小
         */
        public void onRequestProgress(long bytesWritten, long contentLength);
    }

}