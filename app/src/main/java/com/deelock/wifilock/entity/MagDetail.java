package com.deelock.wifilock.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class MagDetail implements Parcelable {

    @Override
    public String toString() {
        return "MagDetail{" +
                "softVersion='" + softVersion + '\'' +
                ", alertType='" + alertType + '\'' +
                ", productId='" + productId + '\'' +
                ", signalStrength='" + signalStrength + '\'' +
                ", nickName='" + nickName + '\'' +
                ", isCall=" + isCall +
                ", pid='" + pid + '\'' +
                ", isOnline=" + isOnline +
                ", isRent=" + isRent +
                ", ssid='" + ssid + '\'' +
                ", timeBind=" + timeBind +
                ", hardVersion='" + hardVersion + '\'' +
                ", timeRegister=" + timeRegister +
                ", state=" + state +
                ", power='" + power + '\'' +
                '}';
    }

    /**
     * softVersion : 10
     * alertType : 01
     * productId : B001002C3A313EDF3D
     * signalStrength : 64
     * nickName : 123
     * isCall : 0
     * pid : B001002C3A313EDF3D
     * isOnline : 1
     * isRent : 0
     * ssid : 2
     * timeBind : 1520000000
     * hardVersion : 10
     * timeRegister : 1526536531
     * state : 0
     * power : 64
     */

    private String softVersion;
    private String alertType;
    private String productId;
    private String signalStrength;
    private String nickName;
    private int isCall;
    private String pid;
    private int isOnline;
    private int isRent;
    private String ssid;
    private int timeBind;
    private String hardVersion;
    private int timeRegister;
    private int state;
    private String power;

    public String getSoftVersion() {
        return softVersion;
    }

    public void setSoftVersion(String softVersion) {
        this.softVersion = softVersion;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(String signalStrength) {
        this.signalStrength = signalStrength;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getIsCall() {
        return isCall;
    }

    public void setIsCall(int isCall) {
        this.isCall = isCall;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public int getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(int isOnline) {
        this.isOnline = isOnline;
    }

    public int getIsRent() {
        return isRent;
    }

    public void setIsRent(int isRent) {
        this.isRent = isRent;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getTimeBind() {
        return timeBind;
    }

    public void setTimeBind(int timeBind) {
        this.timeBind = timeBind;
    }

    public String getHardVersion() {
        return hardVersion;
    }

    public void setHardVersion(String hardVersion) {
        this.hardVersion = hardVersion;
    }

    public int getTimeRegister() {
        return timeRegister;
    }

    public void setTimeRegister(int timeRegister) {
        this.timeRegister = timeRegister;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.softVersion);
        dest.writeString(this.alertType);
        dest.writeString(this.productId);
        dest.writeString(this.signalStrength);
        dest.writeString(this.nickName);
        dest.writeInt(this.isCall);
        dest.writeString(this.pid);
        dest.writeInt(this.isOnline);
        dest.writeInt(this.isRent);
        dest.writeString(this.ssid);
        dest.writeInt(this.timeBind);
        dest.writeString(this.hardVersion);
        dest.writeInt(this.timeRegister);
        dest.writeInt(this.state);
        dest.writeString(this.power);
    }

    public MagDetail() {
    }

    protected MagDetail(Parcel in) {
        this.softVersion = in.readString();
        this.alertType = in.readString();
        this.productId = in.readString();
        this.signalStrength = in.readString();
        this.nickName = in.readString();
        this.isCall = in.readInt();
        this.pid = in.readString();
        this.isOnline = in.readInt();
        this.isRent = in.readInt();
        this.ssid = in.readString();
        this.timeBind = in.readInt();
        this.hardVersion = in.readString();
        this.timeRegister = in.readInt();
        this.state = in.readInt();
        this.power = in.readString();
    }

    public static final Parcelable.Creator<MagDetail> CREATOR = new Parcelable.Creator<MagDetail>() {
        @Override
        public MagDetail createFromParcel(Parcel source) {
            return new MagDetail(source);
        }

        @Override
        public MagDetail[] newArray(int size) {
            return new MagDetail[size];
        }
    };
}
