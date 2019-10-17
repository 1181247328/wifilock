package com.deelock.wifilock.entity;

import android.support.annotation.NonNull;

import com.contrarywind.interfaces.IPickerViewData;

/**
 * Created by forgive for on 2018\5\10 0010.
 */
public class Building implements IPickerViewData,Comparable<Building>{

    String buildingName;
    int buildingId;

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public int getBuildingId() {
        return buildingId;
    }

    @Override
    public String getPickerViewText() {
        return buildingName;
    }


    @Override
    public int compareTo(@NonNull Building o) {
        if(this.buildingName.length()==o.buildingName.length()){
            int compare = this.buildingName.compareTo(o.buildingName);
            return compare;
        }else {
            return this.buildingName.length()-o.buildingName.length();
        }
    }
}
