package com.deelock.wifilock.entity;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by binChuan on 2017\9\21 0021.
 */

public class FPrintList {

    public String pid;

    @SerializedName("userFprintList")
    public List<UserFPrint> list;

    public List<UserFPrint> getList(){
        if (list == null){
            list = new ArrayList<>();
        }
        return list;
    }
}
