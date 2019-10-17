package com.deelock.wifilock.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Created by binChuan on 2017\9\11 0011.
 */

public class UserDetail implements Parcelable {

    private long pid;
    private String phoneNumber;
    private String nickName;
    private int headIcon;
    private String email;
    private int sex;
    private long birth;
    private String address;
    private long timeRegister;
    private String headUrl;

    protected UserDetail(Parcel in) {
        pid = in.readLong();
        phoneNumber = in.readString();
        nickName = in.readString();
        headIcon = in.readInt();
        email = in.readString();
        sex = in.readInt();
        birth = in.readLong();
        address = in.readString();
        timeRegister = in.readLong();
        headUrl = in.readString();
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public long getPid() {
        return pid;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setHeadIcon(int headIcon) {
        this.headIcon = headIcon;
    }

    public int getHeadIcon() {
        return headIcon;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getSex() {
        return sex;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setBirth(long birth) {
        this.birth = birth;
    }

    public long getBirth() {
        return birth;
    }

    public void setTimeRegister(long timeRegister) {
        this.timeRegister = timeRegister;
    }

    public long getTimeRegister() {
        return timeRegister;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(pid);
        dest.writeString(phoneNumber);
        dest.writeString(nickName);
        dest.writeInt(headIcon);
        dest.writeString(email);
        dest.writeInt(sex);
        dest.writeLong(birth);
        dest.writeString(address);
        dest.writeLong(timeRegister);
        dest.writeString(headUrl);
    }

    public static final Creator<UserDetail> CREATOR = new Creator<UserDetail>() {
        @Override
        public UserDetail createFromParcel(Parcel in) {
            return new UserDetail(in);
        }

        @Override
        public UserDetail[] newArray(int size) {
            return new UserDetail[size];
        }
    };
}
