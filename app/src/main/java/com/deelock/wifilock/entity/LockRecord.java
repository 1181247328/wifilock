package com.deelock.wifilock.entity;

/**
 * Created by binChuan on 2017\9\11 0011.
 */

public class LockRecord {

    private String pid;
    private String authId;
    private int historyState;
    private String name;
    private String openName;
    private int headIcon;
    private long dtft;
    private String time;
    private String headUrl;
    private long timeBegin;
    private long timeEnd;
    private String week;

    /**
     * 1 记录 2 日起
     */
    int type;

    public void setPid(String pid){
        this.pid = pid;
    }

    public String getPid(){
        return this.pid;
    }

    public void setHistoryState(int historyState){
        this.historyState = historyState;
    }

    public int getHistoryState(){
        return this.historyState;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setOpenName(String openName){
        this.openName = openName;
    }

    public String getOpenName(){
        return this.openName;
    }
    public void setHeadIcon(int headIcon){
        this.headIcon = headIcon;
    }

    public int getHeadIcon(){
        return this.headIcon;
    }

    public void setDtft(long dtft) {
        this.dtft = dtft;
    }

    public long getDtft() {
        return dtft;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getAuthId() {
        return authId;
    }

    public void setTimeBegin(long timeBegin) {
        this.timeBegin = timeBegin;
    }

    public long getTimeBegin() {
        return timeBegin;
    }

    public void setTimeEnd(long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getWeek() {
        return week;
    }

}
