package com.deelock.wifilock.ui.activity;

import android.os.Bundle;

import com.deelock.wifilock.R;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.databinding.ActivitySelectGateBinding;
import com.deelock.wifilock.entity.DeviceStateList;
import com.deelock.wifilock.utils.GsonUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;

public class SelectGateActivity extends BaseActivity<ActivitySelectGateBinding> {

    public static String type;

    @Override
    protected int initLayout() {
        return R.layout.activity_select_gate;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

        type = getIntent().getStringExtra("type");
        binding.setClick(this);
        String stringData = SPUtil.getStringData(this, SPUtil.LOCK_STATE);
        DeviceStateList deviceStateList = GsonUtil.json2Bean(stringData, DeviceStateList.class);
        binding.setList(deviceStateList.getGatewayList());
    }

    public void onBackClicked() {
        finish();
    }

    public void addGate() {
//        ToastUtil.toastShort(this,"敬请期待");
        Bundle bundle = new Bundle();
        bundle.putString("type", "000");
        openView(LinkWifiActivity.class, bundle);
    }
}
