package com.deelock.wifilock.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by binChuan on 2017\9\22 0022.
 */

public class User implements Parcelable, Comparable<User> {

    private String pid;

    private int state;

    private int type;

    private String name;

    private int isPush;

    private long timeCreate;

    private String headUrl;

    private int isBtopen;

    private String phoneNumber;

    private int authUid;

    /**
     * 0表示只有密码，1表示只有指纹，2表示都有嘛，-1表示均无
     */
    public int userRight;

    protected User(Parcel in) {
        pid = in.readString();
        state = in.readInt();
        type = in.readInt();
        name = in.readString();
        userRight = in.readInt();
        isPush = in.readInt();
        timeCreate = in.readLong();
        headUrl = in.readString();
        isBtopen = in.readInt();
        phoneNumber = in.readString();
        authUid = in.readInt();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public void setPid(String uid){
        this.pid = uid;
    }
    public String getPid(){
        return this.pid;
    }
    public void setState(int state){
        this.state = state;
    }
    public int getState(){
        return this.state;
    }
    public void setType(int type){
        this.type = type;
    }
    public int getType(){
        return this.type;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    public void setUserRight(int userRight) {
        this.userRight = userRight;
    }
    public int getUserRight() {
        return userRight;
    }

    public void setIsPush(int isPush) {
        this.isPush = isPush;
    }

    public int getIsPush() {
        return isPush;
    }

    public void setTimeCreate(long timeCreate) {
        this.timeCreate = timeCreate;
    }

    public long getTimeCreate() {
        return timeCreate;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setIsBtopen(int isBtopen){
        this.isBtopen = isBtopen;
    }
    public int getIsBtopen(){return isBtopen;}

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getAuthUid() {
        return authUid;
    }

    public void setAuthUid(int authUid) {
        this.authUid = authUid;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pid);
        dest.writeInt(state);
        dest.writeInt(type);
        dest.writeString(name);
        dest.writeInt(userRight);
        dest.writeInt(isPush);
        dest.writeLong(timeCreate);
        dest.writeString(headUrl);
        dest.writeInt(isBtopen);
        dest.writeString(phoneNumber);
        dest.writeInt(authUid);
    }

    @Override
    public int compareTo(@NonNull User o) {
        if (timeCreate > o.getTimeCreate()){
            return 1;
        } else if (timeCreate < o.getTimeCreate()){
            return -1;
        } else {
            if (pid.equals("00000000000000000000000000000000")){
                return -1;
            } else {
                return 1;
            }
        }
    }
}
