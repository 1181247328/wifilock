package com.deelock.wifilock.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by binChuan on 2017\9\11 0011.
 */

public class LockDetail implements Parcelable {

    private String pid;
    private String productId;
    private String nickName;
    private String power;
    private String hardVersion;
    private String softVersion;
    private String ssid;
    private long timeRegister;
    private long timeBind;
    private String signalStrength;
    private int isAllowPwd;
    private String alertType;
    private int isCall;
    private int isProsHelp;

    protected LockDetail(Parcel in) {
        pid = in.readString();
        productId = in.readString();
        nickName = in.readString();
        power = in.readString();
        hardVersion = in.readString();
        softVersion = in.readString();
        ssid = in.readString();
        timeRegister = in.readLong();
        timeBind = in.readLong();
        signalStrength = in.readString();
        isAllowPwd = in.readInt();
        alertType = in.readString();
        isCall = in.readInt();
        isProsHelp = in.readInt();
    }

    public static final Creator<LockDetail> CREATOR = new Creator<LockDetail>() {
        @Override
        public LockDetail createFromParcel(Parcel in) {
            return new LockDetail(in);
        }

        @Override
        public LockDetail[] newArray(int size) {
            return new LockDetail[size];
        }
    };

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getPower() {
        return power;
    }

    public void setHardVersion(String hardVersion) {
        this.hardVersion = hardVersion;
    }

    public String getHardVersion() {
        return hardVersion;
    }

    public void setSoftVersion(String softVersion) {
        this.softVersion = softVersion;
    }

    public String getSoftVersion() {
        return softVersion;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getSsid() {
        return ssid;
    }

    public void setTimeRegister(long timeRegister) {
        this.timeRegister = timeRegister;
    }

    public long getTimeRegister() {
        return timeRegister;
    }

    public void setTimeBind(long timeBind) {
        this.timeBind = timeBind;
    }

    public long getTimeBind() {
        return timeBind;
    }

    public void setSignalStrength(String signalStrength) {
        this.signalStrength = signalStrength;
    }

    public String getSignalStrength() {
        return signalStrength;
    }

    public void setIsAllowPwd(int isAllowPwd) {
        this.isAllowPwd = isAllowPwd;
    }

    public int getIsAllowPwd() {
        return isAllowPwd;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public void setIsProsHelp(int isProsHelp) {
        this.isProsHelp = isProsHelp;
    }

    public int getIsProsHelp() {
        return isProsHelp;
    }

    /**
     * 第一位表示开的状态（0:不推送,1推送），第二位代表关（0:不推送,1推送）
     * @return alertType
     */
    public String getAlertType() {
        return alertType;
    }

    public void setIsCall(int isCall) {
        this.isCall = isCall;
    }

    public int getIsCall() {
        return isCall;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pid);
        dest.writeString(productId);
        dest.writeString(nickName);
        dest.writeString(power);
        dest.writeString(hardVersion);
        dest.writeString(softVersion);
        dest.writeString(ssid);
        dest.writeLong(timeRegister);
        dest.writeLong(timeBind);
        dest.writeString(signalStrength);
        dest.writeInt(isAllowPwd);
        dest.writeString(alertType);
        dest.writeInt(isCall);
        dest.writeInt(isProsHelp);
    }
}
