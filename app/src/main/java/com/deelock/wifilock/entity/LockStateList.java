package com.deelock.wifilock.entity;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by binChuan on 2017\9\11 0011.
 */

public class LockStateList {

    @SerializedName("sdlList")
    private List<LockState> lockList;
    @SerializedName("sdsList")
    private List<LockState> magnetometerList;
    @SerializedName("btsdlList")
    private List<LockState> shareLockList;
    private String uid;

    public void setLockList(List<LockState> lockStateList) {
        this.lockList = lockStateList;
    }

    public List<LockState> getLockList() {
        if (lockList == null){
            lockList = new ArrayList<>();
        }
        return lockList;
    }

    public void setMagnetometerList(List<LockState> magnetometerList) {
        this.magnetometerList = magnetometerList;
    }

    public List<LockState> getShareLockList() {
        return shareLockList;
    }

    public void setShareLockList(List<LockState> shareLockList) {
        this.shareLockList = shareLockList;
    }

    public List<LockState> getMagnetometerList() {
        if (magnetometerList == null){
            magnetometerList = new ArrayList<>();
        }
        return magnetometerList;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }
}
