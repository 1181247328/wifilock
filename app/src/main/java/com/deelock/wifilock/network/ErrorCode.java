package com.deelock.wifilock.network;

/**
 * Created by binChuan on 2017\9\12 0012.
 */

public enum ErrorCode {
    Success(1), Exception(-100);

    private int mValue;

    private ErrorCode(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }
}
