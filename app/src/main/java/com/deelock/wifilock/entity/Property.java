package com.deelock.wifilock.entity;

/**
 * Created by forgive for on 2018\5\10 0010.
 */
public class Property {

    int prosId;
    String prosName;
    long timeCreate;

    public void setTimeCreate(long timeCreate) {
        this.timeCreate = timeCreate;
    }

    public long getTimeCreate() {
        return timeCreate;
    }

    public void setProsId(int prosId) {
        this.prosId = prosId;
    }

    public int getProsId() {
        return prosId;
    }

    public void setProsName(String prosName) {
        this.prosName = prosName;
    }

    public String getProsName() {
        return prosName;
    }
}
