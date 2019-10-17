package com.deelock.wifilock.model;

/**
 * Created by forgive for on 2018\5\8 0008.
 */
public interface IPropertyModel {

    void getCity();
    void getCommunity(int cityId);
    void apply(int commId, int prosId, String number);
    void getApplyResult();
    void cancelApply();
    void addOwner(String name, final int prosId, final int commId, final int buildingId, String address, final String number);
    void modifyOwner(String name, final int prosId, final int commId, final int buildingId, String address, final String number);
    void getOwner();
    void getBuilding(int commId);
    void switchPros(boolean isOpen);
}
