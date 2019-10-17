package com.deelock.wifilock.ui.activity;

import android.os.Bundle;

import com.deelock.wifilock.R;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.databinding.ActivityAddMagBinding;
import com.deelock.wifilock.utils.ToastUtil;

public class AddMagActivity extends BaseActivity<ActivityAddMagBinding> {

    @Override
    protected int initLayout() {
        return R.layout.activity_add_mag;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        binding.setClick(this);
    }

    public void onBackClicked() {
        finish();
    }

    public void onWifiClicked() {
        Bundle bundle = new Bundle();
        bundle.putString("type", "B001");
        openView(LinkWifiActivity.class, bundle);
    }

    public void onZigbeeClicked() {
//        Bundle bundle = new Bundle();
//        bundle.putString("type", "C00");
//        openView(SelectGateActivity.class, bundle);
        ToastUtil.toastShort(this,"敬请期待");
    }
}
