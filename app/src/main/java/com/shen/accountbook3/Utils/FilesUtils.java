package com.shen.accountbook3.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by shen on 11/26 0026.
 */
public class FilesUtils {

    private FilesUtils()
    {
		/* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }


    /**
     * 不存在就创建"文件夹"<p>
     * 拿到后使用： files.exists() 再判断一次，比如文件创建失败
     *
     * @param path 文件夹路径
     */
    public static File createFile(String path){
        File files = new File(path);
        if (!files.exists()) {                              // 不存在，创建
            try {
                //按照指定的路径创建文件夹
                files.mkdirs();
            } catch (Exception e) {
                LogUtils.i("创建文件夹失败:"+path);
            }
        }

        return files;           // 拿到后使用： files.exists() 再判断一次，比如文件创建失败
    }


    /**
     * 将 InputStream 写入到指定的文件
     * @param is        InputStream
     * @param path  将 InputStream 写入到指定的文件 如：f:/fqf.txt
     * @return boolean
     */
    public static File InputStream2File(InputStream is, String path) throws IOException {
        int byteSum = 0;
        int byteRead = 0;
        FileOutputStream fs = null;

        try {
            File file = new File(path);
            if (is != null) {                                           // InputStream流不为空
                fs = new FileOutputStream(file);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteRead = is.read(buffer)) != -1) {
                    byteSum += byteRead;                                    //字节数 文件大小
                    System.out.println(byteSum);
                    fs.write(buffer, 0, byteRead);
                }
            }

            return file;
        }  catch (Exception e) {
            System.out.println("将InputStream写入到指定的文件，操作出错");
            e.printStackTrace();
            return null;
        } finally {
            is.close();
            fs.close();
        }
    }


}
