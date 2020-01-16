package com.deelock.state;

import android.app.Activity;
import android.view.View;

import com.deelock.wifilock.entity.DeviceStateList;

/**
 * 为了不影响曾经作者的代码，特在此开启一个中间逻辑中转站来处理数据逻辑
 */
public class LockState {
    private static LockState lockState = new LockState();

    public static LockState getLockState() {
        return lockState;
    }

    private DeviceStateList deviceStateList = new DeviceStateList();

    private LockMac lockMac = new LockMac();

    /**
     * 界面设置状态栏字体颜色
     */
    public void changeStatusBarTextImgColor(Activity activity, boolean isBlack) {
        if (isBlack) {
            //设置状态栏黑色字体
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            //恢复状态栏白色字体
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

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

    public static void setLockState(LockState lockState) {
        LockState.lockState = lockState;
    }
}
