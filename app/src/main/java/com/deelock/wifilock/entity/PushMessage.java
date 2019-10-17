package com.deelock.wifilock.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by binChuan on 2017\10\25 0025.
 */

public class PushMessage implements Parcelable {

    int id;
    String deviceId;
    String title;
    String data;
    int code;
    long timePush;

    public PushMessage(){

    }

    @Override
    public String toString() {
        return "PushMessage{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", title='" + title + '\'' +
                ", data='" + data + '\'' +
                ", code=" + code +
                ", timePush=" + timePush +
                '}';
    }

    public PushMessage(PushMessage p){
        id = p.getId();
        deviceId = p.getDeviceId();
        title = p.getTitle();
        data = p.getData();
        code = p.getCode();
        timePush = p.getTimePush();
    }

    public PushMessage(Parcel in) {
        id = in.readInt();
        deviceId = in.readString();
        title = in.readString();
        data = in.readString();
        code = in.readInt();
        timePush = in.readLong();
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setTitle(String msg) {
        this.title = msg;
    }

    public String getTitle() {
        return title;
    }

    public void setTimePush(long timePush) {
        this.timePush = timePush;
    }

    public long getTimePush() {
        return timePush;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(deviceId);
        dest.writeString(title);
        dest.writeString(data);
        dest.writeInt(code);
        dest.writeLong(timePush);
    }

    public static final Creator<PushMessage> CREATOR = new Creator<PushMessage>() {
        @Override
        public PushMessage createFromParcel(Parcel in) {
            return new PushMessage(in);
        }

        @Override
        public PushMessage[] newArray(int size) {
            return new PushMessage[size];
        }
    };
}
