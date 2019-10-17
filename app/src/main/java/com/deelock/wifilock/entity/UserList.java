package com.deelock.wifilock.entity;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by binChuan on 2017\9\22 0022.
 */

public class UserList {

    public String pid;

    @SerializedName("userList")
    private List<User> list;

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }

    public void setList(List<User> list) {
        this.list = list;
    }

    public List<User> getList(){
        if (list == null){
            list = new ArrayList<>();
        }
        return list;
    }
}
