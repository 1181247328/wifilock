package com.deelock.wifilock.bluetooth;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.deelock.wifilock.utils.BluetoothUtil;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BleService extends Service {

    public BleService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO
        String mac = intent.getStringExtra("mac");
        BluetoothUtil.openBluetooth();
        BluetoothUtil.connectByScan(mac);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.e("main", "---后台销毁");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
