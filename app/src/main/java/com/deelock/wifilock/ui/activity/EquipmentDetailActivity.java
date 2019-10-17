package com.deelock.wifilock.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.deelock.wifilock.R;
import com.deelock.wifilock.bluetooth.BleActivity;
import com.deelock.wifilock.bluetooth.BleFunctionActivity;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.constants.CommonUtils;
import com.deelock.wifilock.constants.Constants;
import com.deelock.wifilock.databinding.ActivityEquipmentDetailBinding;
import com.deelock.wifilock.entity.BleInfo;
import com.deelock.wifilock.event.EquipmentDetailEvent;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.dialog.DeleteDialog;
import com.deelock.wifilock.ui.dialog.NickNameDialog;
import com.deelock.wifilock.ui.dialog.NoticeDialog;
import com.deelock.wifilock.utils.GsonUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.util.HashMap;

/**
 * Created by binChuan on 2017\9\20 0020.
 */

public class EquipmentDetailActivity extends BaseActivity<ActivityEquipmentDetailBinding>
        implements EquipmentDetailEvent, NickNameDialog.Event, DeleteDialog.Event {

    private String devId;  //设备Id
    private String mac;    //蓝牙mac地址
    private String nickname;  //设备昵称
    private String hardVersion;  //设备硬件版本
    private String softVersion;  //设备软件版本
    private String wifiName;     //设备绑定wifi名
    private int isFlow;         //设备防尾随时间
    private int remotePw;       //设备远程下发密码
    private int fansuo;        //设备反锁
    private int isProsHelp;    //物业管理

    private NickNameDialog nickNameDialog;
    private DeleteDialog dialog;
    private NoticeDialog noticeDialog;

    private int dialogType = -1;
    private int currentRemote = -1;
    private String currentName = null;

    @Override
    protected int initLayout() {
        return R.layout.activity_equipment_detail;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        devId = getIntent().getStringExtra("devId");
        mac = getIntent().getStringExtra("mac");
        nickname = getIntent().getStringExtra("nickname");
        hardVersion = getIntent().getStringExtra("hardVersion");
        softVersion = getIntent().getStringExtra("softVersion");
        wifiName = getIntent().getStringExtra("wifiName");
        isFlow = getIntent().getIntExtra("isFlow", -1);
        mac = getIntent().getStringExtra("mac");
        remotePw = getIntent().getIntExtra("remotePw", -1);
        fansuo = getIntent().getIntExtra("fansuo", -1);
        isProsHelp = getIntent().getIntExtra("isProsHelp", -1);

        binding.nameTv.setText(nickname);
        if (TextUtils.isEmpty(wifiName)) {
            binding.wifiTy0.setText("设置wifi");
        } else {
            binding.wifiTy0.setText("更换wifi");
        }
        binding.wifiTv.setText(wifiName);
        binding.dispatchTv.setText(remotePw == 0 ? "关闭" : "开启");
        binding.propertyStateTv.setText(isProsHelp == 1 ? "开启" : "关闭");
        binding.functionRl.setVisibility(mac == null ? View.GONE : View.VISIBLE);
        binding.dispatchRl.setVisibility(mac == null ? View.VISIBLE : View.GONE);
        binding.equipUnbind.setVisibility(mac == null ? View.VISIBLE : View.GONE);

        nickNameDialog = new NickNameDialog(this, R.style.dialog);
        dialog = new DeleteDialog(this, R.style.dialog);
        dialog.setButton("确定");
        nickNameDialog = new NickNameDialog(this, R.style.dialog);
        noticeDialog = new NoticeDialog(this, R.style.dialog);

        nickNameDialog.setEvent(this);
        dialog.setEvent(this);
        binding.setEvent(this);
    }

    @Override
    public void back() {
        finish();
    }

    /**
     * 修改设备名称
     */
    @Override
    public void equipmentName() {
        nickNameDialog.show();
    }

    /**
     * 关于硬件
     */
    @Override
    public void about() {
        Bundle b = new Bundle();
        b.putString("id", devId);
        b.putString("hard", hardVersion);
        b.putString("soft", softVersion);
        openView(AboutHardwareActivity.class, b);
    }

    /**
     * 更换wifi
     */
    @Override
    public void changeWifi() {
        Bundle b = new Bundle();
        b.putString("type", devId.substring(0, 4));
        b.putString("mac", mac);
        b.putString("deviceId", devId);
        openView(LinkWifiActivity.class, b);
    }

    /**
     * 远程下发密码
     */
    @Override
    public void dispatch() {
        if (remotePw == 1) {
            dialogType = 1;
            dialog.setNoticeTitle("您确定关闭远程下发密码功能？");
            dialog.setNotice("该功能关闭后，用户将不能通过APP远程添加密码给门锁，且只能在门锁上开启该功能。");
            dialog.show();
        } else {
            noticeDialog.setNotice("请在设备上按“设置”+“1#”开启");
            noticeDialog.show();
        }
    }

    /**
     * 所有密码
     */
    @Override
    public void allPassword() {
        Bundle b = new Bundle();
        b.putString("sdlId", devId);
        b.putString("mac", mac);
        openView(AllPasswordActivity.class, b);
    }

    /**
     * 所有指纹
     */
    @Override
    public void allFingerPrint() {
        Bundle b = new Bundle();
        b.putString("sdlId", devId);
        b.putString("mac", mac);
        openView(AllFingerPrintActivity.class, b);
    }

    /**
     * 质保信息
     */
    @Override
    public void warranty() {

    }

    /**
     * 安装服务评价
     */
    @Override
    public void evaluate() {

    }

    /**
     * 物业
     */
    @Override
    public void property() {
        Intent intent = new Intent(this, PropertyActivity.class);
        intent.putExtra("sdlId", devId);
        intent.putExtra("isProsHelp", isProsHelp);
        startActivityForResult(intent, 5);
    }

    /**
     * wifi锁解除绑定
     */
    @Override
    public void unbind() {

        dialog.setNoticeTitle("您确定要删除该设备?");
        dialogType = 2;
        dialog.show();
    }

    /**
     * 蓝牙门锁功能
     */
    @Override
    public void function() {
        Bundle b = new Bundle();
        b.putString("devId", devId);
        b.putString("mac", mac);
        b.putString("hardVersion", hardVersion);
        b.putInt("remotePw", remotePw);
        b.putInt("isFlow", isFlow);
        b.putInt("fansuo", fansuo);
        openView(BleFunctionActivity.class, b);
    }

    /**
     * 蓝牙锁分享功能，暂不实现
     */
    @Override
    public void share() {

    }

    /**
     * 修改设备名称
     *
     * @param name 修改后的设备名称
     */
    @Override
    public void ensure(final String name) {
        HashMap<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params.put("uid", SPUtil.getUid(this));
        params.put("pid", devId);
        params.put("nickName", name);
        RequestUtils.request(RequestUtils.UPDATE_LOCK, this, params)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        ToastUtil.toastShort(getApplicationContext(), "修改成功");
                        nickNameDialog.dismiss();
                        binding.nameTv.setText(name);
                        currentName = name;
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                    }
                });
    }

    /**
     * dialogType==1  wifi锁关闭远程下发密码
     * dialogType==2  wifi锁解除绑定
     */
    @Override
    public void delete() {
        if (dialogType == 1) {
            HashMap<String, String> params = new HashMap<>();
            params.put("timestamp", String.valueOf(TimeUtil.getTime()));
            params.put("uid", SPUtil.getUid(EquipmentDetailActivity.this));
            params.put("pid", devId);
            params.put("isAllowPwd", String.valueOf(0));
            RequestUtils.request(RequestUtils.UPDATE_LOCK, this, params).enqueue(
                    new ResponseCallback<BaseResponse>(this) {
                        @Override
                        protected void onSuccess(int code, String content) {
                            super.onSuccess(code, content);
                            binding.dispatchTv.setText("已关闭");
                            currentRemote = 0;
                        }

                        @Override
                        protected void onFinish() {
                            super.onFinish();
                            dialog.dismiss();
                        }
                    }
            );
        } else if (dialogType == 2) {
            if (CommonUtils.isGestureSet(this) && !Constants.isVerifiedGesture) {
                dialog.dismiss();
                Intent intent = new Intent(this, GestureActivity.class);
                intent.putExtra("isDelete", true);
                startActivityForResult(intent, 0);
            } else {
                dialog.dismiss();
                Intent intent = new Intent(this, VerifyPasswordActivity.class);
                intent.putExtra("isVerify", true);
                intent.putExtra("isDelete", true);
                startActivityForResult(intent, 0);
            }
        }
    }

    /**
     * 重新刷新数据
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        requestDetailInfo();
    }

    /**
     * 请求门锁详情
     */
    private void requestDetailInfo() {
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(this));
        map.put("pid", devId);
        RequestUtils.request(RequestUtils.BLE_INFO, this, map)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);

                        BleInfo bleInfo = GsonUtil.json2Bean(content, BleInfo.class);
                        binding.nameTv.setText(bleInfo.getNickName());
                        if (!TextUtils.isEmpty(bleInfo.getSsid())) {
                            binding.wifiTy0.setText("更换wifi");
                            boolean canUseWifi = bleInfo.getSsid() != null && bleInfo.getIsOnline() == 1;
                            SPUtil.saveData(EquipmentDetailActivity.this, devId+ "wifi", canUseWifi);
                            SPUtil.saveData(EquipmentDetailActivity.this, devId + "onlyWifi", true);
                            SPUtil.saveData(EquipmentDetailActivity.this, devId + "remote", bleInfo.getIsAllowPwd() == 1);   //为1时远程下发密码开启
                            binding.wifiTv.setText(bleInfo.getSsid());
                        }
                        binding.propertyStateTv.setText(bleInfo.getIsProsHelp() == 1 ? "开启" : "关闭");
                        binding.dispatchTv.setText(bleInfo.getIsAllowPwd() == 0 ? "关闭" : "开启");
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        Toast.makeText(EquipmentDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void cancel() {
        dialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        setResult();
        super.onBackPressed();
    }

    /**
     * 返回给设备主界面的数据（EquipmentActivity、BleActivity）
     */
    private void setResult() {
        Intent intent = new Intent();
        intent.putExtra("name", currentName);
        intent.putExtra("isAllowPwd", currentRemote);
        setResult(2, intent);
    }

    /**
     * 具体解绑
     */
    private void requestUnBind() {
        HashMap<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params.put("token", SPUtil.getToken(this));
        params.put("uid", SPUtil.getUid(this));
        params.put("pid", devId);
        params.put("type", "A00");
        RequestUtils.request(RequestUtils.UNBIND, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        if (code == 1) {
                            Toast.makeText(EquipmentDetailActivity.this,
                                    "设备已解除绑定", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EquipmentDetailActivity.this,
                                    MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }

                    @Override
                    protected void onFinish() {
                        super.onFinish();
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        boolean isDelete = data.getBooleanExtra("isDelete", false);
        if (isDelete) {
            requestUnBind();
        }
    }
}
