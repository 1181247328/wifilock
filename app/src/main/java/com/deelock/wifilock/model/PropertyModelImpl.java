package com.deelock.wifilock.model;

import com.deelock.wifilock.entity.Building;
import com.deelock.wifilock.entity.Community;
import com.deelock.wifilock.entity.Property;
import com.deelock.wifilock.entity.PropertyInfo;
import com.deelock.wifilock.entity.PropertyResult;
import com.deelock.wifilock.entity.Province;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by forgive for on 2018\5\7 0007.
 */
public interface PropertyModelImpl {

    void getCityList(List<Province> list);
    void getCommunityList(List<Community> list);
    void getBuildingList(List<List<Building>> list, List<Property> propertyList);
    void applySucceed();
    void getResult(PropertyResult list);
    void cancelSucceed();
    void getInfo(PropertyInfo info);
    void onFinish();
}
