package com.deelock.wifilock.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Created by binChuan on 2017\9\15 0015.
 */

public class UserFPrint implements Comparable<UserFPrint>, Parcelable {

    @SerializedName("fprintId")
    private String pid;


    /**
     * -2删除失败，0表示正在删除，1表示正常使用
     */
    private int state;
    private int type;
    private String authId;
    private String openName;
    private String name;
    private long timeCreate;
    private int isSecurityHelp;

    /**
     * 1 user 2 password
     */
    private int user;

    public UserFPrint(){

    }

    public UserFPrint(UserFPrint o){
        this.pid = o.getPid();
        this.state = o.getState();
        this.type = o.getType();
        this.openName = o.getOpenName();
        this.name = o.getName();
        this.authId = o.getAuthId();
        this.timeCreate = o.getTimeCreate();
        this.isSecurityHelp = o.getIsSecurityHelp();
        this.user = 1;
    }

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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getAuthId() {
        return authId;
    }

    public void setOpenName(String name) {
        this.openName = name;
    }

    public String getOpenName() {
        return openName;
    }

    public void setTimeCreate(long timeCreate) {
        this.timeCreate = timeCreate;
    }

    public long getTimeCreate() {
        return timeCreate;
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pid);
        dest.writeInt(state);
        dest.writeInt(type);
        dest.writeString(openName);
        dest.writeString(authId);
        dest.writeString(name);
        dest.writeLong(timeCreate);
        dest.writeInt(isSecurityHelp);
    }

    public static final Creator<UserFPrint> CREATOR = new Creator<UserFPrint>() {
        @Override
        public UserFPrint createFromParcel(Parcel source) {
            //从Parcel容器中读取传递数据值，封装成Parcelable对象返回逻辑层。
            UserFPrint o = new UserFPrint();
            o.setPid(source.readString());
            o.setState(source.readInt());
            o.setType(source.readInt());
            o.setName(source.readString());
            o.setAuthId(source.readString());
            o.setOpenName(source.readString());
            o.setTimeCreate(source.readLong());
            o.setIsSecurityHelp(source.readInt());
            return o;
        }

        @Override
        public UserFPrint[] newArray(int size) {
            //创建一个类型为T，长度为size的数组，仅一句话（return new T[size])即可。方法是供外部类反序列化本类数组使用。
            return new UserFPrint[size];
        }
    };

    @Override
    public int compareTo(@NonNull UserFPrint o) {
        if (this.name.equals(o.getName())){
            return 1;
        } else {
            return 0;
        }
    }
}
