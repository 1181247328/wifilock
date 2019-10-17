package com.deelock.wifilock.entity;

public class BleInfo {

    /**
     * softVersion : 00
     * productId : A00318030100110011
     * nickName : 智能门锁
     * pid : A00318030100110011
     * isOnline : 0
     * isRent : 0
     * timeBind : 1540628936
     * isFlowHelp : 3
     * isProsHelp : 0
     * hardVersion : 00
     * isElcLock : 0
     * timeRegister : 1540628936
     * isAllowPwd : 1
     * state : 0
     * power : 64
     */

    private String softVersion;
    private String productId;
    private String nickName;
    private String pid;
    private int isOnline;
    private int isRent;
    private int isWifiOpen;
    private long timeBind;
    private String isFlowHelp;
    private int isProsHelp;
    private String hardVersion;
    private int isElcLock;
    private long timeRegister;
    private int isAllowPwd;
    private int state;
    private String power;
    private String ssid;
    private String signalStrength;

    public String getSoftVersion() {
        return softVersion;
    }

    public void setSoftVersion(String softVersion) {
        this.softVersion = softVersion;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public int getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(int isOnline) {
        this.isOnline = isOnline;
    }

    public int getIsRent() {
        return isRent;
    }

    public void setIsRent(int isRent) {
        this.isRent = isRent;
    }

    public long getTimeBind() {
        return timeBind;
    }

    public void setTimeBind(long timeBind) {
        this.timeBind = timeBind;
    }

    public String getIsFlowHelp() {
        return isFlowHelp;
    }

    public void setIsFlowHelp(String isFlowHelp) {
        this.isFlowHelp = isFlowHelp;
    }

    public int getIsProsHelp() {
        return isProsHelp;
    }

    public void setIsProsHelp(int isProsHelp) {
        this.isProsHelp = isProsHelp;
    }

    public String getHardVersion() {
        return hardVersion;
    }

    public void setHardVersion(String hardVersion) {
        this.hardVersion = hardVersion;
    }

    public int getIsElcLock() {
        return isElcLock;
    }

    public void setIsElcLock(int isElcLock) {
        this.isElcLock = isElcLock;
    }

    public long getTimeRegister() {
        return timeRegister;
    }

    public void setTimeRegister(long timeRegister) {
        this.timeRegister = timeRegister;
    }

    public int getIsAllowPwd() {
        return isAllowPwd;
    }

    public void setIsAllowPwd(int isAllowPwd) {
        this.isAllowPwd = isAllowPwd;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(String signalStrength) {
        this.signalStrength = signalStrength;
    }

    public int getIsWifiOpen() {
        return isWifiOpen;
    }

    public void setIsWifiOpen(int isWifiOpen) {
        this.isWifiOpen = isWifiOpen;
    }
}
