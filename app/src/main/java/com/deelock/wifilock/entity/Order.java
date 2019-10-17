package com.deelock.wifilock.entity;

/**
 * Created by Administrator on 2017\11\1 0001.
 */

public class Order {

    String orderId;
    long time;
    String authId;
    String name;

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getAuthId() {
        return authId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
