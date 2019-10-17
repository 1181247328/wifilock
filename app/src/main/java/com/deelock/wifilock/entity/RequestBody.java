package com.deelock.wifilock.entity;

/**
 * Created by binChuan on 2017\10\16 0016.
 */

public class RequestBody {

    int check;
    String content;

    public void setContent(String content) {
        this.content = content;
    }

    public int getCheck() {
        return check;
    }

    public void setCheck(int check) {
        this.check = check;
    }

    public String getContent() {
        return content;
    }
}
