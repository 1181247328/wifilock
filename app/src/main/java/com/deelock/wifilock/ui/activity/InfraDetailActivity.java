package com.deelock.wifilock.ui.activity;

import android.content.Intent;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.entity.MagDetail;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.dialog.AccessDialog;
import com.deelock.wifilock.ui.dialog.DeleteDialog;
import com.deelock.wifilock.ui.dialog.NickNameDialog;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.util.HashMap;

/**
 * 红外设备详情
 */

public class InfraDetailActivity extends BaseActivity implements
        View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private RelativeLayout deviceNameRl;
    private TextView deviceName;   //当前设备名

    private RelativeLayout aboutDeviceRl;   //关于硬件
    private RelativeLayout replaceWifiRl;
    private ImageButton back;
    private TextView wifiName;  //当前设备绑定wifi名
    private SwitchCompat pushSwitch;  //推送开关
    private SwitchCompat warnSwitch;  //警戒开关
    private RelativeLayout accessXingJiaRl;  //猩家接入
    private LinearLayout deleteDeviceLl;  //解绑该设备

    private MagDetail device;  //红外详情
    private String deviceId;

    private NickNameDialog deviceNameDialog;  //修改设备名称提示框
    private DeleteDialog deleteDialog;    //删除设备提示框
    private AccessDialog accessDialog;    //接入猩家提示框

    @Override
    protected void bindActivity() {
        setContentView(R.layout.activity_infra_detail);
    }

    @Override
    protected void findView() {
        back = f(R.id.infra_back);
        deviceNameRl = f(R.id.infra_name_rl);
        deviceName = f(R.id.infra_name);
        aboutDeviceRl = f(R.id.infra_about_rl);
        replaceWifiRl = f(R.id.infra_wifi_rl);
        wifiName = f(R.id.infra_wifi);
        pushSwitch = f(R.id.infra_push);
        warnSwitch = f(R.id.infra_warn);
        accessXingJiaRl = f(R.id.infra_access_rl);
        deleteDeviceLl = f(R.id.infra_delete_ll);
    }

    @Override
    protected void doBusiness() {
        device = getIntent().getParcelableExtra("device");
        if ("智能门磁".equals(device.getNickName()) || TextUtils.isEmpty(device.getNickName())) {
            deviceName.setText("人体红外");
        } else {
            deviceName.setText(device.getNickName());
        }
        wifiName.setText(device.getSsid());
        pushSwitch.setChecked("11".equals(device.getAlertType()));
        warnSwitch.setChecked(device.getIsCall() == 1);

        deviceId = device.getPid();
        deviceNameDialog = new NickNameDialog(this, R.style.dialog);
        deleteDialog = new DeleteDialog(this, R.style.dialog);
        deleteDialog.setNoticeTitle("您确定要解绑该设备");
        accessDialog = new AccessDialog(this, R.style.dialog);
    }

    @Override
    protected void setEvent() {
        back.setOnClickListener(this);
        deviceNameRl.setOnClickListener(this);
        aboutDeviceRl.setOnClickListener(this);
        replaceWifiRl.setOnClickListener(this);
        pushSwitch.setOnCheckedChangeListener(this);
        warnSwitch.setOnCheckedChangeListener(this);
        deleteDeviceLl.setOnClickListener(this);
        accessXingJiaRl.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == pushSwitch) {
            changePush(isChecked ? "11" : "01");
        } else if (buttonView == warnSwitch) {
            changeWarn(isChecked ? "1" : "0");
        }
    }

    @Override
    public void onClick(View v) {
        if (v == deviceNameRl) {
            deviceNameDialog.setEvent(new NickNameDialog.Event() {
                @Override
                public void ensure(String name) {
                    modifyName(name);
                }
            });
            deviceNameDialog.show();
        } else if (v == aboutDeviceRl) {
            forwardPage();
        } else if (v == replaceWifiRl) {
            changeWifi();
        } else if (v == deleteDeviceLl) {
            deleteDialog.setEvent(new DeleteDialog.Event() {
                @Override
                public void delete() {
                    deleteDevice();
                }

                @Override
                public void cancel() {

                }
            });
            deleteDialog.show();
        } else if (v == accessXingJiaRl) {
            accessDialog.setEvent(new AccessDialog.Event() {
                @Override
                public void ensure(String inputContent) {
                    if (inputContent.length() != 8) {
                        ToastUtil.toastShort(InfraDetailActivity.this, "请输入8位验证码");
                        return;
                    }
                    accessThird(inputContent);
                }

                @Override
                public void cancel() {

                }
            });
            accessDialog.show();
        } else if (v == back) {
            finish();
        }
    }


    /**
     * 修改当前设备名称
     *
     * @param name 修改后的名称
     */
    private void modifyName(final String name) {
        HashMap<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params.put("uid", SPUtil.getUid(this));
        params.put("pid", deviceId);
        params.put("nickName", name);
        RequestUtils.request(RequestUtils.MAGNETOMETER_UPDATE, this, params)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        if (code == 1) {
                            deviceName.setText(name);
                        }
                    }
                });
    }

    /**
     * 跳转到设备硬件界面
     */
    private void forwardPage() {
        Intent intent = new Intent(this, AboutHardwareActivity.class);
        intent.putExtra("id", device.getPid());
        intent.putExtra("hard", device.getHardVersion());
        intent.putExtra("soft", device.getSoftVersion());
        startActivity(intent);
    }

    /**
     * 更换设备wifi
     */
    private void changeWifi() {
        Intent intent1 = new Intent(this, ChangeWifiActivity.class);
        intent1.putExtra("ssid", device.getSsid());
        intent1.putExtra("type", deviceId.substring(0, 3));
        startActivity(intent1);
    }

    /**
     * 更换消息推送模式，红外设备只分推送与不推送，即11与01
     */
    private void changePush(String type) {
        HashMap<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params.put("uid", SPUtil.getUid(this));
        params.put("pid", deviceId);
        params.put("alertType", type);
        RequestUtils.request(RequestUtils.MAGNETOMETER_UPDATE, this, params)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {

                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        ToastUtil.toastShort(InfraDetailActivity.this, message);
                    }
                });
    }

    /**
     * 更换警戒模式
     */
    private void changeWarn(String isCall) {
        HashMap<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params.put("uid", SPUtil.getUid(this));
        params.put("pid", deviceId);
        params.put("isCall", isCall);
        RequestUtils.request(RequestUtils.MAGNETOMETER_UPDATE, this, params)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        ToastUtil.toastShort(InfraDetailActivity.this, message);
                    }
                });
    }

    /**
     * 接入第三方服务（猩家）
     */
    private void accessThird(String inputCode) {
        HashMap<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params.put("uid", SPUtil.getUid(this));
        params.put("accountId", String.valueOf(1000));
        params.put("sdsId", deviceId);
        params.put("settingCode", inputCode);
        RequestUtils.request(RequestUtils.MAGNETIC_ACCESS, this, params)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        ToastUtil.toastLong(InfraDetailActivity.this, content);
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        ToastUtil.toastLong(InfraDetailActivity.this, message);
                    }
                });
    }

    /**
     * 删除当前设备
     */
    private void deleteDevice() {
        HashMap<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params.put("uid", SPUtil.getUid(this));
        params.put("pid", deviceId);
        params.put("type", "B00");
        RequestUtils.request(RequestUtils.UNBIND, this, params)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        if (code == 1) {
                            ToastUtil.toastShort(InfraDetailActivity.this, "已解绑该红外设备");
                            Intent intent = new Intent(InfraDetailActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            ToastUtil.toastShort(InfraDetailActivity.this, content);
                        }
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        ToastUtil.toastShort(InfraDetailActivity.this, message);
                    }
                });
    }
}
