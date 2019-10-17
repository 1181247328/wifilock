package com.deelock.wifilock.entity;

import java.util.List;

/**
 * Created by forgive for on 2018\2\2 0002.
 */

public class Evaluate {

    private String deviceId;
    private List<EvaluateItem> itemList;

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setItemList(List<EvaluateItem> itemList) {
        this.itemList = itemList;
    }

    public List<EvaluateItem> getItemList() {
        return itemList;
    }
}
