package com.deelock.wifilock.entity;

import java.util.List;

public class DeviceStateList {

    private LockStateList singleWifiObjs;
    private List<GateWay> gatewayList;

    public LockStateList getSingleWifiObjs() {
        return singleWifiObjs;
    }

    public void setSingleWifiObjs(LockStateList singleWifiObjs) {
        this.singleWifiObjs = singleWifiObjs;
    }

    public List<GateWay> getGatewayList() {
        return gatewayList;
    }

    public void setGatewayList(List<GateWay> gatewayList) {
        this.gatewayList = gatewayList;
    }
}
