package com.deelock.wifilock.bluetooth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.deelock.wifilock.R;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.databinding.ActivityBleUpdateBinding;

/**
 * 蓝牙门锁硬件升级
 */

public class BleUpdateActivity extends BaseActivity<ActivityBleUpdateBinding> {

    @Override
    protected int initLayout() {
        return R.layout.activity_ble_update;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        String current_version = getIntent().getStringExtra("current_version");
        String newest_version = getIntent().getStringExtra("newest_version");
        String content = getIntent().getStringExtra("content");
        binding.updateHardCurrent.setText(current_version);
        binding.updateHardNewest.setText(newest_version);

        if (!TextUtils.isEmpty(current_version) && current_version.equals(newest_version)) {
            binding.updateHardBtn.setVisibility(View.GONE);
            binding.updateHardContent.setVisibility(View.GONE);
        } else {
            binding.updateHardContent.setText(content);
        }
    }

    public void onBackClicked() {
        finish();
    }

    /**
     * 点击升级
     */
    public void onUpdateClicked() {
        //向蓝牙门锁发送消息
    }
}
