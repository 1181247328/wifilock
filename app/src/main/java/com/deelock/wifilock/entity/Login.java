package com.deelock.wifilock.entity;

/**
 * Created by binChuan on 2017\9\11 0011.
 */

public class Login {
    private String pid;
    private String token;
    private String phoneNumber;
    private String nickName;
    private int headIcon;
    private int sex;
    private String headUrl;

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    private String accountMark;

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setHeadIcon(int headIcon) {
        this.headIcon = headIcon;
    }

    public int getHeadIcon() {
        return headIcon;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getSex() {
        return sex;
    }

    public void setAccountMark(String accountMark) {
        this.accountMark = accountMark;
    }

    public String getAccountMark() {
        return accountMark;
    }
}
