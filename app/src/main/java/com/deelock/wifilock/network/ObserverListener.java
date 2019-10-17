package com.deelock.wifilock.network;

/**
 * Created by forgive for on 2018\3\23 0023.
 */

public interface ObserverListener<T> {

    void onNext(T t);
}
