package com.deelock.wifilock.network;

/**
 * Created by binChuan on 2017\9\19 0019.
 */

public class TimeUtil {

    public static long getTime(){
        return System.currentTimeMillis()/1000L;
    }

    public static long getDay(long timeBind){
        return (getTime() - timeBind)/(60 * 60 * 24) + 1;
    }
}
