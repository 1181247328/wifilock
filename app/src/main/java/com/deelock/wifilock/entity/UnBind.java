package com.deelock.wifilock.entity;

/**
 * Created by binChuan on 2017\9\11 0011.
 */

public class UnBind {
    private String pid;

    private String uid;

    public void setPid(String pid){
        this.pid = pid;
    }

    public String getPid(){
        return this.pid;
    }

    public void setUid(String uid){
        this.uid = uid;
    }

    public String getUid(){
        return this.uid;
    }
}
