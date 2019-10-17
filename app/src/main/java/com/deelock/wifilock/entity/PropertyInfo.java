package com.deelock.wifilock.entity;

/**
 * Created by forgive for on 2018\5\8 0008.
 */
public class PropertyInfo {

    private String masterName;
    private int commId;
    private String commName;
    private String buildingName;
    private int buildingId;
    private String floor;
    private String phoneNumber;

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public String getMasterName() {
        return masterName;
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

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getFloor() {
        return floor;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getBuildingName() {
        return buildingName;
    }
}
