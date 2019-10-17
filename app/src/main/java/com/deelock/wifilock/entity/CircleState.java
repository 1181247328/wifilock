package com.deelock.wifilock.entity;

/**
 * Created by Administrator on 2017\10\31 0031.
 */

public class CircleState {

    String orderId;
    int state;

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
