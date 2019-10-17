package com.deelock.wifilock.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.deelock.wifilock.R;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.databinding.ActivityGateWayInfoBinding;
import com.deelock.wifilock.entity.GateInfo;
import com.deelock.wifilock.entity.GateWay;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.dialog.DeleteDialog;
import com.deelock.wifilock.ui.dialog.NickNameDialog;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.util.HashMap;
import java.util.Map;

public class GateWayInfoActivity extends BaseActivity<ActivityGateWayInfoBinding> {

    private GateInfo.GtwObjBean gateWay;
    private DeleteDialog deleteDialog;
    private NickNameDialog nickNameDialog;

    @Override
    protected int initLayout() {
        return R.layout.activity_gate_way_info;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        gateWay = getIntent().getParcelableExtra("gateState");
        binding.gatewayInfoName.setText(gateWay.getDevName());
        binding.gatewayInfoWifi.setText(gateWay.getSsid());

        nickNameDialog = new NickNameDialog(this, R.style.dialog);
        nickNameDialog.setEvent(new NickNameDialog.Event() {
            @Override
            public void ensure(final String name) {
                requestChangeName(name);
            }
        });

        deleteDialog = new DeleteDialog(this, R.style.dialog);
        deleteDialog.setNoticeTitle("您确定要解绑该设备");
        deleteDialog.setEvent(new DeleteDialog.Event() {
            @Override
            public void delete() {
                requestUnBind();
            }

            @Override
            public void cancel() {
                deleteDialog.dismiss();
            }
        });

        binding.setClick(this);
    }

    public void onBackClicked() {
        finish();
    }

    public void onDeviceNameClicked() {
        //设备名称
        nickNameDialog.show();
    }

    public void onAboutDeviceClicked() {
        //关于设备
        Intent intent = new Intent(this, AboutHardwareActivity.class);
        intent.putExtra("id", gateWay.getDevId());
        intent.putExtra("hard", "100");
        intent.putExtra("soft", "100");
        startActivity(intent);
    }

    public void onDeviceWifiClicked() {
        //设备wifi
        Intent intent = new Intent(this, LinkWifiActivity.class);
        intent.putExtra("ssid", gateWay.getSsid());
        intent.putExtra("type", gateWay.getDevId().substring(0, 3));
        startActivity(intent);
    }

    public void onUnBindClicked() {
        deleteDialog.show();
    }

    private void requestUnBind() {
        HashMap<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params.put("uid", SPUtil.getUid(this));
        params.put("devId", gateWay.getDevId());
        RequestUtils.request(RequestUtils.GATEWAY_UNBIND, this, params)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        if (code == 1) {
                            ToastUtil.toastShort(GateWayInfoActivity.this, "已解除绑定");
                            Intent intent = new Intent(GateWayInfoActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        ToastUtil.toastShort(GateWayInfoActivity.this, message);
                    }
                });
    }

    private void requestChangeName(final String name) {
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("devId", gateWay.getDevId());
        params.put("devName", name);
        RequestUtils.request(RequestUtils.GATEWAY_UPDATE, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        ToastUtil.toastShort(GateWayInfoActivity.this, "修改成功");
                        binding.gatewayInfoName.setText(name);
                        Intent intent = new Intent();
                        intent.putExtra("name", name);
                        setResult(200, intent);
                        nickNameDialog.dismiss();
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        ToastUtil.toastShort(GateWayInfoActivity.this, message);
                    }
                }
        );
    }

}
