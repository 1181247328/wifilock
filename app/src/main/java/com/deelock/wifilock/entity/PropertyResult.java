package com.deelock.wifilock.entity;

/**
 * Created by forgive for on 2018\5\9 0009.
 */
public class PropertyResult {

    private String devId;
    private String orderId;
    private long timeUpdate;
    private long timeResult;
    private int buildingId;
    private int result;
    private int uid;
    private long timeCreate;
    private String phoneNumber;
    private int commId;
    private int prosId;
    private String commName;
    private String floor;
    private String prosName;
    private String masterName;

    public void setResult(int result) {
        this.result = result;
    }

    public int getResult() {
        return result;
    }

    public void setProsId(int prosId) {
        this.prosId = prosId;
    }

    public int getProsId() {
        return prosId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setTimeCreate(long timeCreate) {
        this.timeCreate = timeCreate;
    }

    public long getTimeCreate() {
        return timeCreate;
    }

    public void setProsName(String prosName) {
        this.prosName = prosName;
    }

    public String getProsName() {
        return prosName;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setCommId(int commId) {
        this.commId = commId;
    }

    public int getCommId() {
        return commId;
    }

    public void setCommName(String commName) {
        this.commName = commName;
    }

    public String getCommName() {
        return commName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getFloor() {
        return floor;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public int getUid() {
        return uid;
    }

    public void setTimeResult(long timeResult) {
        this.timeResult = timeResult;
    }

    public long getTimeResult() {
        return timeResult;
    }

    public void setTimeUpdate(long timeUpdate) {
        this.timeUpdate = timeUpdate;
    }

    public long getTimeUpdate() {
        return timeUpdate;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getDevId() {
        return devId;
    }
}
