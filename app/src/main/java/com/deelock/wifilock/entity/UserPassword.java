package com.deelock.wifilock.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Created by binChuan on 2017\9\12 0012.
 */

public class UserPassword implements Comparable<UserPassword>, Parcelable {

    @SerializedName("pwdId")
    private String pid;

    /**
     *  -1 表示添加失败， 0 表示正在删除， 1 表示正常使用， 2 表示添加中
     */
    private int state;
    private int type;
    String authId;
    String name;
    private String openName;
    private long timeBegin;
    private int isSecurityHelp;

    /**
     * 1 user 2 password
     */
    private int user;

    public UserPassword(){

    }

    public UserPassword(UserPassword o){
        pid = o.getPid();
        state = o.getState();
        type = o.getType();
        authId = o.getAuthId();
        name = o.getName();
        openName = o.getOpenName();
        timeBegin = o.getTimeBegin();
        isSecurityHelp = o.getIsSecurityHelp();
        user = 1;
    }

    protected UserPassword(Parcel in) {
        pid = in.readString();
        state = in.readInt();
        type = in.readInt();
        authId = in.readString();
        name = in.readString();
        openName = in.readString();
        timeBegin = in.readLong();
        user = in.readInt();
        isSecurityHelp = in.readInt();
    }

    public static final Creator<UserPassword> CREATOR = new Creator<UserPassword>() {
        @Override
        public UserPassword createFromParcel(Parcel in) {
            return new UserPassword(in);
        }

        @Override
        public UserPassword[] newArray(int size) {
            return new UserPassword[size];
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

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getAuthId() {
        return authId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setOpenName(String name) {
        this.openName = name;
    }

    public String getOpenName() {
        return openName;
    }

    public void setTimeBegin(long timeBegin) {
        this.timeBegin = timeBegin;
    }

    public long getTimeBegin() {
        return timeBegin;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getUser() {
        return user;
    }

    public void setIsSecurityHelp(int isSecurityHelp) {
        this.isSecurityHelp = isSecurityHelp;
    }

    public int getIsSecurityHelp() {
        return isSecurityHelp;
    }

    @Override
    public int compareTo(@NonNull UserPassword o) {
        if (this.name.equals(o.getName())){
            return 1;
        } else {
            return 0;
        }
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
        dest.writeString(authId);
        dest.writeString(name);
        dest.writeString(openName);
        dest.writeLong(timeBegin);
        dest.writeInt(user);
        dest.writeInt(isSecurityHelp);
    }
}
