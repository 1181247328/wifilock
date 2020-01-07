package com.deelock.state;

import com.deelock.wifilock.entity.DeviceStateList;

/**
 * 为了不影响曾经作者的代码，特在此开启一个中间逻辑中转站来处理数据逻辑
 */
public class LockState {
    private static LockState lockState = new LockState();

    public  static LockState getLockState() {
        return lockState;
    }

    private DeviceStateList deviceStateList = new DeviceStateList();

    private LockMac lockMac = new LockMac();

    public DeviceStateList getDeviceStateList() {
        return deviceStateList;
    }

    public void setDeviceStateList(DeviceStateList deviceStateList) {
        this.deviceStateList = deviceStateList;
    }

    public LockMac getLockMac() {
        return lockMac;
    }

    public void setLockMac(LockMac lockMac) {
        this.lockMac = lockMac;
    }
}
