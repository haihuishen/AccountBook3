package com.shen.accountbook3.domain;

/**
 * Created by shen on 10/14 0014.
 */
public class UserInfo {
//    createFile table if not exists user(
//            id bigint primary key not null,
//            name varchar(20) not null,
//    password varchar(20) not null,
//    sex tinyint(1),
//    image varchar(50),
//    birthdate date,
//    qq varchar(20)
//    );



    private Long id;
    private String userName;
    private String passWord;
    private int sex;
    private String image;
    private String birthday;
    private String qq;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

}
