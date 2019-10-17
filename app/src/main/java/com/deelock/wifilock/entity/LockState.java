package com.deelock.wifilock.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by binChuan on 2017\9\11 0011.
 */

public class LockState implements Parcelable {

    private String pid;
    private int state;
    /**
     * 0未上提反锁，1已上提反锁
     */
    private int isOnline;
    private String power;
    private long timeBind;
    private String nickName;
    private int isCall;
    private String macAddr;
    private String ssid;
    private int isManager;
    private int isBtopen;

    public LockState() {

    }

    public LockState(String pid, int state, int isOnline, String power, long timeBind, String nickName, int isCall) {
        this.pid = pid;
        this.state = state;
        this.isOnline = isOnline;
        this.power = power;
        this.timeBind = timeBind;
        this.nickName = nickName;
        this.isCall = isCall;
    }

    protected LockState(Parcel in) {
        pid = in.readString();
        state = in.readInt();
        isOnline = in.readInt();
        power = in.readString();
        timeBind = in.readLong();
        nickName = in.readString();
        isCall = in.readInt();
        macAddr = in.readString();
        ssid = in.readString();
        isManager = in.readInt();
        isBtopen = in.readInt();
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public static final Creator<LockState> CREATOR = new Creator<LockState>() {
        @Override
        public LockState createFromParcel(Parcel in) {
            return new LockState(in);
        }

        @Override
        public LockState[] newArray(int size) {
            return new LockState[size];
        }
    };

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setIsOnline(int isOnline) {
        this.isOnline = isOnline;
    }

    public int getIsOnline() {
        return isOnline;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getPower() {
        return power;
    }

    public void setTimeBind(long timeBind) {
        this.timeBind = timeBind;
    }

    public long getTimeBind() {
        return timeBind;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setIsCall(int isCall) {
        this.isCall = isCall;
    }

    public int getIsCall() {
        return isCall;
    }

    public String getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(String macAddr) {
        this.macAddr = macAddr;
    }

    public int getIsManager() {
        return isManager;
    }

    public void setIsManager(int isManager) {
        this.isManager = isManager;
    }

    public int getIsBtopen() {
        return isBtopen;
    }

    public void setIsBtopen(int isBtopen) {
        this.isBtopen = isBtopen;
    }

    @Override

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pid);
        dest.writeInt(state);
        dest.writeInt(isOnline);
        dest.writeString(power);
        dest.writeLong(timeBind);
        dest.writeString(nickName);
        dest.writeInt(isCall);
        dest.writeString(macAddr);
        dest.writeString(ssid);
        dest.writeInt(isManager);
        dest.writeInt(isBtopen);
    }

    @Override
    public String toString() {
        return "LockState{" +
                "pid='" + pid + '\'' +
                ", state=" + state +
                ", isOnline=" + isOnline +
                ", power='" + power + '\'' +
                ", timeBind=" + timeBind +
                ", nickName='" + nickName + '\'' +
                ", isCall=" + isCall +
                ", macAddr='" + macAddr + '\'' +
                ", ssid='" + ssid + '\'' +
                ", isManager=" + isManager +
                ", isBtopen=" + isBtopen +
                '}';
    }
}
