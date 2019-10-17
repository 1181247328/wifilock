package com.deelock.wifilock.entity;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by binChuan on 2017\9\12 0012.
 */

public class PasswordList {

    public String pid;

    @SerializedName("tempPwdList")
    public List<TempPassword> tempPasswords;

    @SerializedName("pwdList")
    public List<UserPassword> userPasswords;

    public List<TempPassword> getTempPasswords() {
        if (tempPasswords == null){
            tempPasswords = new ArrayList<>();
        }
        return tempPasswords;
    }

    public List<UserPassword> getUserPasswords() {
        if (userPasswords == null){
            userPasswords = new ArrayList<>();
        }
        return userPasswords;
    }
}
