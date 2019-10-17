package com.deelock.wifilock.entity;

import com.contrarywind.interfaces.IPickerViewData;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by forgive for on 2018\5\7 0007.
 */
public class Province implements IPickerViewData {

    private int provId;
    private String provName;
    @SerializedName("cityList")
    private List<City> list;
    private long timeCreate;

    public Province(String provName){
        this.provName = provName;
    }

    public void setProvId(int provId) {
        this.provId = provId;
    }

    public int getProvId() {
        return provId;
    }

    public void setProvName(String provName) {
        this.provName = provName;
    }

    public String getProvName() {
        return provName;
    }

    public List<City> getList() {
        if (list == null){
            list = new ArrayList<>();
        }
        return list;
    }

    public void setTimeCreate(long timeCreate) {
        this.timeCreate = timeCreate;
    }

    public long getTimeCreate() {
        return timeCreate;
    }

    @Override
    public String getPickerViewText() {
        return provName + "省";
    }
}
