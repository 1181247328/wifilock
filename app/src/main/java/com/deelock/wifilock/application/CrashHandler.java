package com.deelock.wifilock.application;

import com.deelock.wifilock.utils.BluetoothUtil;
import com.orhanobut.logger.Logger;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static CrashHandler mCrashHandler;

    private CrashHandler() {

    }

    public static CrashHandler getInstance() {
        if (mCrashHandler == null) {
            mCrashHandler = new CrashHandler();
        }
        return mCrashHandler;
    }

    public void init() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Logger.e(throwable.getMessage());
        BluetoothUtil.closeBluetooth();
        BluetoothUtil.stopBluetooth();
        BluetoothUtil.clearInfo();
//        android.os.Process.killProcess(android.os.Process.myPid());
//        System.exit(1);
    }


}
