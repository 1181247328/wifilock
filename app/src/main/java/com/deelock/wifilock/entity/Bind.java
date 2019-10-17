package com.deelock.wifilock.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by binChuan on 2017\9\11 0011.
 */

public class Bind {
    private String pid;

    private String productId;

    private String uid;

    @SerializedName("isComment")
    private int isEvaluate;

    private String phoneNumber;

    public void setPid(String pid){
        this.pid = pid;
    }

    public String getPid(){
        return this.pid;
    }

    public void setProductId(String productId){
        this.productId = productId;
    }

    public String getProductId(){
        return this.productId;
    }

    public void setUid(String uid){
        this.uid = uid;
    }

    public String getUid(){
        return this.uid;
    }

    public void setIsEvaluate(int isEvaluate) {
        this.isEvaluate = isEvaluate;
    }

    public int getIsEvaluate() {
        return isEvaluate;
    }

    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber(){
        return this.phoneNumber;
    }
}
