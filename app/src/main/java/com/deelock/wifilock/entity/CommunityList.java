package com.deelock.wifilock.entity;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by forgive for on 2018\5\10 0010.
 */
public class CommunityList {

    int cityId;
    List<Property> prosList;
    @SerializedName("commList")
    List<Community> list;

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getCityId() {
        return cityId;
    }

    public List<Community> getList() {
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
