package com.shen.accountbook3.domain;

/**
 * 组图对象<p>
 * 
 * 使用Gson解析时,对象书写技巧: <br>
 * 1. 逢{}创建"对象", 逢[]创建"集合(ArrayList)" <br>
 * 
 * 2. 所有"字段名称"要和"json返回字段"高度一致 (要一样!!!)<br>
 * Javabean
 *
 */
public class UpdateVersionInfo {

        public String versionName;      // 新版本名称
        public String versionDes;       // 新版本号
        public String versionCode;      // 新版本描述
        public String downloadUrl;      // 新版本下载Url

    @Override
    public String toString() {
        return "   versionName:" + versionName +
                "   versionDes:" + versionDes +
                "   versionCode:" + versionCode +
                "   downloadUrl:" + downloadUrl ;
    }
}
