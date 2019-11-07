package com.deelock.bleradio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * 监听蓝牙开关的广播接受者
 */
public class DeelockRadio extends BroadcastReceiver {

    public String TAG = DeelockRadio.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            //监听蓝牙开关
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                //获取蓝牙开关状态
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                switch (blueState) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.e(TAG, "---蓝牙正在打开中---");

                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.e(TAG, "---蓝牙已经打开---");

                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.e(TAG, "---蓝牙正在关闭中---");

                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.e(TAG, "---蓝牙已经关闭---");
                        removePairDevice();
                        break;
                }
                break;
        }
    }

    private static void removePairDevice() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
            mBluetoothAdapter.enable();
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
        if (mBluetoothAdapter != null) {
            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                unpairDevice(device);
            }
        }
    }

    private static void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e("蓝牙清除缓存异常", e.getMessage());
        }
    }
}
