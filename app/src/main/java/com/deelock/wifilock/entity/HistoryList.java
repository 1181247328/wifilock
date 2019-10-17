package com.deelock.wifilock.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by binChuan on 2017\9\30 0030.
 */

public class HistoryList {

    public String pid;

    public List<LockRecord> list;

    public List<LockRecord> getList(){
        if (list == null){
            list = new ArrayList<>();
        }
        return list;
    }
}
