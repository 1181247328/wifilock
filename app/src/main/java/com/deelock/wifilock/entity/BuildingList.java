package com.deelock.wifilock.entity;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by forgive for on 2018\5\10 0010.
 */
public class BuildingList {

    long timeCreate;
    int commId;
    List<Property> prosList;
    @SerializedName("buildingList")
    List<Building> list;

    public void setCommId(int commId) {
        this.commId = commId;
    }

    public int getCommId() {
        return commId;
    }

    public List<Building> getList() {
        if (list == null){
            list = new ArrayList<>();
        }
        return list;
    }

    public List<Property> getProsList() {
        if (prosList == null){
            prosList = new ArrayList<>();
        }
        return prosList;
    }
}
