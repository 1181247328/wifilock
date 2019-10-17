package com.deelock.wifilock.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.deelock.wifilock.R;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.databinding.ActivityAboutHardwareBinding;
import com.deelock.wifilock.utils.SPUtil;

public class AboutHardwareActivity extends BaseActivity<ActivityAboutHardwareBinding> {

    @Override
    protected int initLayout() {
        return R.layout.activity_about_hardware;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        String id = getIntent().getStringExtra("id");
        String version_hard = getIntent().getStringExtra("hard");
        String version_soft = getIntent().getStringExtra("soft");

        binding.aboutHardId.setText(id);

        binding.aboutVersionHard.setText(formatData(version_hard));
        binding.aboutVersionSoft.setText(formatData(version_soft));
        binding.setClick(this);

        if ("A003".equals(id.substring(0, 4))) {
            binding.aboutMacRl.setVisibility(View.VISIBLE);
            binding.aboutMac.setText(SPUtil.getStringData(this, id + "mac"));
        }
    }

    public void onBackClicked() {
        finish();
    }

    private String formatData(String data) {
        if (TextUtils.isEmpty(data) || "00".equals(data)) {
            data = "10";
        }
        if (data.contains(".")) {
            return data;
        } else {
            String regex = "(.{1})";
            data = data.replaceAll(regex, "$1.");
            return data.substring(0, data.length() - 1);
        }
    }
}
