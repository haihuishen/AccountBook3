package com.shen.accountbook3.domain;

import java.io.Serializable;

/**
 * 返回结果的 JSON
 *
 * implements Serializable  序列化：也可以不写
 * @author shen
 *
 */
public class RegisterResultJSONBean implements Serializable {

    private static final long serialVersionUID = 2740440249422567441L;

    private String result;
    private Long id;

    public RegisterResultJSONBean(){}

    /**
     * 返回结果的 JSON
     * @param result			返回的结果
     * @param id			     注册后获得的id
     */
    public RegisterResultJSONBean(String result, Long id) {
        this.result = result;
        this.id = id;
    }

    public String getResult() {
        return result;
    }
    public void setResult(String result) {
        this.result = result;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }


    @Override
    public String toString() {
        return "   result:" + result + "   id:" + id;
    }

}
