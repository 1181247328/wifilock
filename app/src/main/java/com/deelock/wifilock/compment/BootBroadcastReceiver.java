package com.deelock.wifilock.compment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import cn.jpush.android.api.JPushInterface;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("TAG", "开机自动服务自动启动.....");
        JPushInterface.setDebugMode(true);
        JPushInterface.init(context.getApplicationContext());
    }
}