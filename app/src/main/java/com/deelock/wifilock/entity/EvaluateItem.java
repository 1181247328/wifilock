package com.deelock.wifilock.entity;

/**
 * Created by forgive for on 2018\2\2 0002.
 */

public class EvaluateItem {

    String pid;
    int isSelect;
    String name;
    String description;

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIsSelect() {
        return isSelect;
    }

    public void setIsSelect(int isSelect) {
        this.isSelect = isSelect;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }
}
