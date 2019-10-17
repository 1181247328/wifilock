package com.deelock.wifilock.entity;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by forgive for on 2018\5\8 0008.
 */
public class ProvList {

    private String pid;
    @SerializedName("provList")
    private List<Province> list;

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }

    public List<Province> getList() {
        if (list == null){
            list = new ArrayList<>();
        }
        return list;
    }
}
