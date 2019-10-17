package com.deelock.wifilock.view;

import com.deelock.wifilock.entity.PropertyInfo;

/**
 * Created by forgive for on 2018\5\3 0003.
 */
public interface IPropertyView {

    void notOpen();
    void opening();
    void opened();
    void openFailed();
    void openRefused();
    void setInfo(PropertyInfo info);
    void setCity(String city);
    void setAddress(String address);
}
