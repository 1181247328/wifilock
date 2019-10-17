package com.deelock.wifilock.entity;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by binChuan on 2017\9\25 0025.
 */

public class UserPasswordList {

    public String pid;

    @SerializedName("userPwdList")
    public List<UserPassword> list;

    public List<UserPassword> getList() {
        if (list == null){
            list = new ArrayList<>();
        }
        return list;
    }
}
