package com.deelock.wifilock.ui.activity;

import android.os.Bundle;

import com.deelock.wifilock.R;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.databinding.ActivityAddEquipmentBinding;
import com.deelock.wifilock.utils.ToastUtil;

public class AddEquipmentActivity extends BaseActivity<ActivityAddEquipmentBinding> {

    @Override
    protected int initLayout() {
        return R.layout.activity_add_equipment;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        binding.setClick(this);
    }

    public void onBackClicked() {
        finish();
    }

    public void onAddLockClicked() {
        openView(AddLockActivity.class, null);
    }

    public void onAddMagClicked() {
        openView(AddMagActivity.class, null);
    }

    public void onAddGateClicked() {
//        Bundle bundle = new Bundle();
//        bundle.putString("type", "000");
//        openView(LinkWifiActivity.class, bundle);
        ToastUtil.toastShort(this, "敬请期待");
    }

    public void onAddInfraClicked() {
        Bundle bundle = new Bundle();
        bundle.putString("type", "B002");
        openView(LinkWifiActivity.class, bundle);
    }

    public void onAddCatClicked() {
        ToastUtil.toastShort(this, "敬请期待");
    }

}
