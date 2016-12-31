package com.zhy.http.okhttp.callback;

import com.zhy.http.okhttp.OkHttpUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Response;

/**
 * 回调类——网络请求结束:<br>
 * 将网络请求的数据，弄成"文件"
 */
public abstract class FileCallBack extends Callback<File> {
    /**"目标文件"存储的——文件夹路径*/
    private String destFileDir;
    /**"目标文件"存储的——文件名*/
    private String destFileName;


    /**
     * 构造函数——获取..."文件夹路径"和"文件名"
     * @param destFileDir   "目标文件"存储的——文件夹路径
     * @param destFileName  "目标文件"存储的——文件名
     */
    public FileCallBack(String destFileDir, String destFileName) {
        this.destFileDir = destFileDir;
        this.destFileName = destFileName;
    }

    /**
     * 解析网络响应<p>
     * "应该要"在"子线程/线程池"中运行<p>
     *
     *  这里是——保存"请求到的文件"<br>
     *  将请求到的文件保存到"构造函数"中的到的——"文件夹路径/文件名"
     * @param response
     * @param id
     * @return              返回的是"File"
     * @throws Exception
     */
    @Override
    public File parseNetworkResponse(Response response, int id) throws Exception {
        return saveFile(response,id);
    }


    /**
     * 保存文件
     * @param response
     * @param id
     * @return
     * @throws IOException
     */
    public File saveFile(Response response,final int id) throws IOException {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fos = null;
        try {
            is = response.body().byteStream();                          // 拿到响应体的"流"
            final long total = response.body().contentLength();

            long sum = 0;

            File dir = new File(destFileDir);                        // 文件夹
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, destFileName);                // 文件
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
                final long finalSum = sum;

                // 获取线程池——执行此任务
                OkHttpUtils.getInstance().getDelivery().execute(new Runnable() {
                    @Override
                    public void run() {
                        inProgress(finalSum * 1.0f / total,total,id);
                    }
                });
            }
            fos.flush();

            return file;

        } finally {
            try {
                response.body().close();
                if (is != null) is.close();
            } catch (IOException e) {
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
            }

        }
    }


}
