package com.deelock.wifilock.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.deelock.wifilock.R;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.databinding.ActivityGateWayBinding;
import com.deelock.wifilock.entity.GateDetail;
import com.deelock.wifilock.entity.GateInfo;
import com.deelock.wifilock.entity.GateWay;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.utils.DensityUtil;
import com.deelock.wifilock.utils.GsonUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.util.HashMap;


public class GateWayActivity extends BaseActivity<ActivityGateWayBinding> {

    private GateWay gate;

    @Override
    protected int initLayout() {
        return R.layout.activity_gate_way;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        gate = getIntent().getParcelableExtra("gateState");
        binding.gatewayName.setText(gate.getDevName());

        int day = (int) TimeUtil.getDay(gate.getTimeBind());
        int screenWidth = DensityUtil.getScreenWidth(this);
        int daySize = 30 * screenWidth / 750;
        AbsoluteSizeSpan as = new AbsoluteSizeSpan(daySize);
        String string = getString(R.string.gateway_day_bind, day);
        SpannableString day_bind = new SpannableString(string);
        ForegroundColorSpan bgSpan = new ForegroundColorSpan(getResources().getColor(R.color.day_bind));
        day_bind.setSpan(as, 2, 2 + String.valueOf(day).length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        day_bind.setSpan(bgSpan, 2, 2 + String.valueOf(day).length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        binding.gatewayDay.setText(day_bind);

        int isOnline = gate.getIsOnline();
        boolean online = isOnline == 1;
        binding.gatewayIsOnline.setText(online ? getString(R.string.gateway_online) : getString(R.string.gateway_offline));
        binding.gatewayIsOnline.setTextColor(online ? getResources().getColor(R.color.online) : getResources().getColor(R.color.offline));

        binding.gatewayCircleBackground.setImageResource(online ? R.mipmap.magnetometer_blue : R.mipmap.magnetometer_orange);
        binding.gatewayCircleSafe.setImageResource(online ? R.mipmap.circle_outer_safe : R.mipmap.circle_outer_unsafe);

        binding.setClick(this);
    }

    /**
     * 请求网关下所有设备
     *
     * @param gateWay 网关
     */
    private void getDetailList(GateWay gateWay) {
        HashMap<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params.put("uid", SPUtil.getUid(this));
        params.put("gtwId", gateWay.getDevId());
        RequestUtils.request(RequestUtils.GATEWAY_DEVICES, this, params)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        GateDetail gateDetail = GsonUtil.json2Bean(content, GateDetail.class);
                        binding.setList(gateDetail.getSubList());
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        ToastUtil.toastShort(GateWayActivity.this, message);
                    }
                });
    }

    public void onSettingClicked() {
        HashMap<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params.put("uid", SPUtil.getUid(this));
        params.put("devId", gate.getDevId());
        RequestUtils.request(RequestUtils.GATEWAY_DETAIL, this, params)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        GateInfo gateInfo = GsonUtil.json2Bean(content, GateInfo.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("gateState", gateInfo.getGtwObj());
                        openView(GateWayInfoActivity.class, bundle);
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        ToastUtil.toastShort(GateWayActivity.this, message);
                    }
                });
    }

    public void onBackClicked() {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDetailList(gate);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == 200) {
            if (data != null) {
                String name = data.getStringExtra("name");
                if (!TextUtils.isEmpty(name)) {
                    binding.gatewayName.setText(name);
                }
            }
        }
    }
}
