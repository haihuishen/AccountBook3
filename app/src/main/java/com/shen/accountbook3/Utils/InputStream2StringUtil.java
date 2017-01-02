package com.shen.accountbook3.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 将流转换成字符串(工具类封装)
 * @author shen
 *
 */
public class InputStream2StringUtil {
	/**
	 * 流转换成字符串
	 * @param is	流对象
	 * @return		流转换成的字符串	返回null代表异常
	 */
	public static String streamToString(InputStream is) {
		//1,在读取的过程中,将读取的内容存储值缓存中,然后一次性的转换成字符串返回

		ByteArrayOutputStream bos = new ByteArrayOutputStream();// ByteArrayOutputStream:字节数组输出流
		byte[] buffer = new byte[1024];                     //2,读流操作,读到没有为止(循环)
		int temp = -1;                                      //3,记录读取内容的临时变量 (读了多少个字符)
		try {

			while((temp = is.read(buffer))!=-1){            // is -> buffer
				bos.write(buffer, 0, temp);	                // buffer -> bos
			}
			return bos.toString();                          //返回读取数据
		} catch (IOException e) {
			e.printStackTrace();
		}finally{                                           // 关闭流
			try {
				is.close();	
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}


    /**
     * 利用ByteArrayOutputStream：Inputstream------------>String <功能详细描述>
     *
     * @param in
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static String Inputstr2Str_ByteArrayOutputStream(InputStream in,String encode) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int len = 0;
        try {
            if (encode == null || encode.equals("")) {
                // 默认以utf-8形式
                encode = "utf-8";
            }
            while ((len = in.read(b)) > 0) {
                out.write(b, 0, len);
            }
            return out.toString(encode);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
