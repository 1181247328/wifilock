package com.deelock.wifilock.ui.activity;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.deelock.wifilock.R;
import com.deelock.wifilock.utils.BluetoothUtil;

public class AddLockActivity extends BaseActivity implements View.OnClickListener {

    private ImageButton back;
    private ImageView wifi;
    private ImageView ble;

    @Override
    protected void bindActivity() {
        setContentView(R.layout.activity_add_lock);
    }

    @Override
    protected void doBusiness() {
        back = findViewById(R.id.add_lock_back);
        wifi = findViewById(R.id.add_lock_wifi);
        ble = findViewById(R.id.add_lock_ble);
    }

    @Override
    protected void setEvent() {
        back.setOnClickListener(this);
        wifi.setOnClickListener(this);
        ble.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
       if(v==back){
           finish();
       }else if(v== wifi){
           onWifiClicked();
       }else if(v== ble){
           onBleClicked();
       }

    }
    /**
     * wifi锁
     */
    private void onWifiClicked() {
        Bundle b = new Bundle();
        b.putString("type", "A001");
        openActivity(LinkWifiActivity.class, b);
    }

    /**
     * 蓝牙锁
     */
    private void onBleClicked() {
        boolean b1 = BluetoothUtil.openBluetooth();
        if (b1) {
            Bundle b = new Bundle();
            b.putString("type", "A003");
            openActivity(BindLockActivity.class, b);
        } else {
            Toast.makeText(AddLockActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
        }
    }

}
