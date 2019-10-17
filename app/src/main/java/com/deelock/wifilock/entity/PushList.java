package com.deelock.wifilock.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by forgive for on 2018\3\14 0014.
 */

public class PushList {
    String uid;
    List<Message> list;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<Message> getList() {
        if (list == null){
            list = new ArrayList<>();
        }
        return list;
    }
}
