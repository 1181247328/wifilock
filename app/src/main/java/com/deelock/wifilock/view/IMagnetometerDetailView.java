package com.deelock.wifilock.view;

/**
 * Created by forgive for on 2018\5\18 0018.
 */
public interface IMagnetometerDetailView {

    void setName(String name);
    void setWiFi(String WiFi);
    void setPush(String open, String close);
    void setWarning(boolean isChecked);
}
