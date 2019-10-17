package com.deelock.wifilock.ui.activity;

import android.os.Bundle;

import com.deelock.wifilock.R;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.databinding.ActivityGateLockDetailBinding;

public class GateLockActivity extends BaseActivity<ActivityGateLockDetailBinding> {

    @Override
    protected int initLayout() {
        return R.layout.activity_gate_lock_detail;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        binding.setClick(this);
    }

    public void onBackClicked() {
        finish();
    }

    public void onDeviceNameClicked() {

    }

    public void onAboutDeviceClicked() {

    }

    public void onChangeGateClicked() {

    }

    public void onLockFunctionClicked() {

    }

    public void onShareClicked() {

    }

    public void onPwdClicked() {

    }

    public void onPrintClicked() {

    }

    public void onPropertyClicked() {

    }

    public void onUnbind() {

    }
}
