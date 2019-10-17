package com.deelock.wifilock.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class GateWay implements Parcelable {
    private String devId;
    private String devType;
    private int isProsHelp;
    private int isOnline;
    private String power;
    private String devName;
    private long timeBind;

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getDevType() {
        return devType;
    }

    public void setDevType(String devType) {
        this.devType = devType;
    }

    public int getIsProsHelp() {
        return isProsHelp;
    }

    public void setIsProsHelp(int isProsHelp) {
        this.isProsHelp = isProsHelp;
    }

    public int getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(int isOnline) {
        this.isOnline = isOnline;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public long getTimeBind() {
        return timeBind;
    }

    public void setTimeBind(long timeBind) {
        this.timeBind = timeBind;
    }

    @Override
    public String toString() {
        return "GateWay{" +
                "devId='" + devId + '\'' +
                ", devType='" + devType + '\'' +
                ", isProsHelp=" + isProsHelp +
                ", isOnline=" + isOnline +
                ", power='" + power + '\'' +
                ", devName='" + devName + '\'' +
                ", timeBind=" + timeBind +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.devId);
        dest.writeString(this.devType);
        dest.writeInt(this.isProsHelp);
        dest.writeInt(this.isOnline);
        dest.writeString(this.power);
        dest.writeString(this.devName);
        dest.writeLong(this.timeBind);
    }

    public GateWay() {
    }

    protected GateWay(Parcel in) {
        this.devId = in.readString();
        this.devType = in.readString();
        this.isProsHelp = in.readInt();
        this.isOnline = in.readInt();
        this.power = in.readString();
        this.devName = in.readString();
        this.timeBind = in.readLong();
    }

    public static final Creator<GateWay> CREATOR = new Creator<GateWay>() {
        @Override
        public GateWay createFromParcel(Parcel source) {
            return new GateWay(source);
        }

        @Override
        public GateWay[] newArray(int size) {
            return new GateWay[size];
        }
    };
}
