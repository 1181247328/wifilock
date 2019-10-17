package com.deelock.wifilock.entity;

/**
 * Created by forgive for on 2018\3\21 0021.
 */

public class Message {

    private String devId;
    private long uid;
    private String sdlId;
    private long timeUpdate;
    private String alertId;
    private int type;
    private String deviceId;
    private String sdsId;
    private int dealtCode;

    public int getDealtCode() {
        return dealtCode;
    }

    public void setDealtCode(int dealtCode) {
        this.dealtCode = dealtCode;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getSdlId() {
        return sdlId;
    }

    public void setSdlId(String sdlId) {
        this.sdlId = sdlId;
    }

    public long getTimeUpdate() {
        return timeUpdate;
    }

    public void setTimeUpdate(long timeUpdate) {
        this.timeUpdate = timeUpdate;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSdsId() {
        return sdsId;
    }

    public void setSdsId(String sdsId) {
        this.sdsId = sdsId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "devId='" + devId + '\'' +
                ", uid=" + uid +
                ", sdlId='" + sdlId + '\'' +
                ", timeUpdate=" + timeUpdate +
                ", alertId='" + alertId + '\'' +
                ", type=" + type +
                ", deviceId='" + deviceId + '\'' +
                ", sdsId='" + sdsId + '\'' +
                ", dealtCode=" + dealtCode +
                '}';
    }
}
