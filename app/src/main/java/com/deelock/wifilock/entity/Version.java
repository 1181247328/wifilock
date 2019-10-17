package com.deelock.wifilock.entity;

/**
 * Created by binChuan on 2017\9\11 0011.
 */

public class Version {
    private String pid;

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setVersionCount(int versionCount) {
        this.versionCount = versionCount;
    }

    private int versionCount;

    private String message;

    private int timeUpdate;

    private String version;

    public void setPid(String pid){
        this.pid = pid;
    }

    public String getPid(){
        return this.pid;
    }

    public void setVersion(int versionCount){
        this.versionCount = versionCount;
    }

    public int getVersionCount(){
        return this.versionCount;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }

    public void setTimeUpdate(int timeUpdate){
        this.timeUpdate = timeUpdate;
    }

    public int getTimeUpdate(){
        return this.timeUpdate;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
