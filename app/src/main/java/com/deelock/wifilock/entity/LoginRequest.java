package com.deelock.wifilock.entity;

/**
 * Created by binChuan on 2017\9\14 0014.
 */

public class LoginRequest {

    private String phoneNumber;
    private String password;
    private String did;

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getDid() {
        return did;
    }
}
