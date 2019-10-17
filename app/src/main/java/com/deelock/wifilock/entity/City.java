package com.deelock.wifilock.entity;

import com.contrarywind.interfaces.IPickerViewData;

/**
 * Created by forgive for on 2018\5\7 0007.
 */
public class City implements IPickerViewData {

    private int cityId;
    private String cityName;
    private long timeCreate;

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setTimeCreate(long timeCreate) {
        this.timeCreate = timeCreate;
    }

    public long getTimeCreate() {
        return timeCreate;
    }

    @Override
    public String getPickerViewText() {
        return cityName + "市";
    }
}
