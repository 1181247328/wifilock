package com.deelock.wifilock.view;


import com.deelock.wifilock.entity.MagDetail;

/**
 * Created by forgive for on 2018\5\17 0017.
 */
public interface IMagnetometerView {

    void onWifiClose();

    void onWifiOpen();

    void onTouch();

    void initViewData(MagDetail detail);
}
