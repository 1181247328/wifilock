package com.deelock.wifilock.entity;

/**
 * Created by binChuan on 2017\9\14 0014.
 */

public class RegisterRequest {

    private String phoneNumber;

    private String password;

    private String msgCode;

    private String nickName;

    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }
    public String getPhoneNumber(){
        return this.phoneNumber;
    }
    public void setPassword(String userPwd){
        this.password = userPwd;
    }
    public String getPassword(){
        return this.password;
    }
    public void setMsgCode(String msgCode){
        this.msgCode = msgCode;
    }
    public String getMsgCode(){
        return this.msgCode;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }
}
