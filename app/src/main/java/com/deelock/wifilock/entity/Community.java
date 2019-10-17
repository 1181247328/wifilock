package com.deelock.wifilock.entity;

import com.contrarywind.interfaces.IPickerViewData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by forgive for on 2018\5\9 0009.
 */
public class Community implements IPickerViewData {

    long timeCreate;
    int commId;
    String commName;
    List<Building> buildingList;
    List<Property> propertyList;

    public void setTimeCreate(long timeCreate) {
        this.timeCreate = timeCreate;
    }

    public long getTimeCreate() {
        return timeCreate;
    }

    public void setCommId(int commId) {
        this.commId = commId;
    }

    public int getCommId() {
        return commId;
    }

    public void setCommName(String commName) {
        this.commName = commName;
    }

    public String getCommName() {
        return commName;
    }

    public void setBuildingList(List<Building> buildingList){
        this.buildingList = buildingList;
    }

    public List<Building> getBuildingList() {
        if (buildingList == null){
            return new ArrayList<>();
        }
        return buildingList;
    }

    public void setPropertyList(List<Property> propertyList) {
        this.propertyList = propertyList;
    }

    public List<Property> getPropertyList() {
        if (propertyList == null){
            return new ArrayList<>();
        }
        return propertyList;
    }

    @Override
    public String getPickerViewText() {
        return commName;
    }
}
