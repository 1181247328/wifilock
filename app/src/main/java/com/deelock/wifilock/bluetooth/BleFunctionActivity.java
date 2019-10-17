package com.deelock.wifilock.bluetooth;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.deelock.wifilock.R;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.databinding.ActivityBleFunctionBinding;
import com.deelock.wifilock.entity.BleInfo;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.activity.MainActivity;
import com.deelock.wifilock.ui.activity.VerifyPasswordActivity;
import com.deelock.wifilock.ui.dialog.BleNetDialog;
import com.deelock.wifilock.utils.BluetoothUtil;
import com.deelock.wifilock.utils.DensityUtil;
import com.deelock.wifilock.utils.GsonUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

/**
 * 蓝牙门锁功能
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BleFunctionActivity extends BaseActivity<ActivityBleFunctionBinding> {

    private Dialog dialog;
    private BleFunctionView bleFunctionView;
    private String mac, devId, hardVersion;
    private ProgressDialog mProgressDialog;
    private String isFlowHelp;
    private int isElcLock;
    private int isAllowPwd;
    private boolean isRestore = false;
    private BleInfo bleInfo;
    private String type;
    private String currentOrder;
    private CompositeDisposable mCompositeDisposable;
    private HandleOrder mHandle;

    @Override
    protected int initLayout() {
        return R.layout.activity_ble_function;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        binding.setClick(this);

        mac = getIntent().getStringExtra("mac");
        devId = getIntent().getStringExtra("devId");

        mCompositeDisposable = new CompositeDisposable();
        mHandle = new HandleOrder();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("正在连接门锁蓝牙...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && mProgressDialog.isShowing()) {
                    Toast.makeText(BleFunctionActivity.this, "请等待操作完成", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        dialog = new Dialog(this, R.style.bottomDialog);
        bleFunctionView = new BleFunctionView(this);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.windowAnimation);
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = DensityUtil.dip2px(this, 240);
            window.setGravity(Gravity.BOTTOM);
            window.setAttributes(layoutParams);
        }
        dialog.setContentView(bleFunctionView);
        bleFunctionView.setInterface(new MyListener());

        requestDetailInfo();

    }

    /**
     * 设置版本类型格式
     *
     * @param data 版本数据不存在默认为1.0
     * @return 返回版本号
     */
    private String formatData(String data) {
        if (TextUtils.isEmpty(data) || "00".equals(data)) {
            data = "10";
        }
        if (data.contains(".")) {
            return data;
        } else {
            String regex = "(.{1})";
            data = data.replaceAll(regex, "$1.");
            return data.substring(0, data.length() - 1) + "版";
        }
    }

    /**
     * 返回
     */
    public void onBackClicked() {
        finish();
    }

    /**
     * 升级
     */
    public void onUpdateClicked() {
        //需要传入参数，未完成
//        openView(BleUpdateActivity.class, null);
        Toast.makeText(this, "敬请期待", Toast.LENGTH_SHORT).show();
    }

    /**
     * 音量修改
     */
    public void onVolumeClicked() {
        bleFunctionView.setCurrentView(BleFunctionView.FUN_VOLUME);
        dialog.show();
    }

    /**
     * 防尾随
     */
    public void onAntiTailClicked() {
        bleFunctionView.setCurrentView(BleFunctionView.FUN_WEISUI);
        dialog.show();
    }

    /**
     * 远程下发密码
     */
    public void onRemoteClicked() {
        if (SPUtil.getBooleanData(this, devId + "onlyWifi")) {
            bleFunctionView.setCurrentView(BleFunctionView.FUN_REMOTE);
            dialog.show();
        } else {
            BleNetDialog bleNetDialog = new BleNetDialog(this, R.style.dialog);
            //TODO
            bleNetDialog.show();
            bleNetDialog.setState(2);
        }
    }

    /**
     * 电子反锁
     */
    public void onLockClicked() {
        bleFunctionView.setCurrentView(BleFunctionView.FUN_FANSUO);
        dialog.show();
    }

    /**
     * 关闭WiFi
     */
    public void onCloseWifi() {
        bleFunctionView.setCurrentView(BleFunctionView.FUN_WIFI);
        dialog.show();
    }

    /**
     * 恢复出厂设置
     */
    public void onRestoreClicked() {
        bleFunctionView.setCurrentView(BleFunctionView.FUN_RESTORE);
        dialog.show();
    }

    /**
     * 所有弹出dialog的点击事件
     */
    private class MyListener implements BleFunctionView.funBleViewListener {

        @Override
        public void weisui_2() {
            if (!BluetoothUtil.isConnected) {
                mProgressDialog.setMessage("正在连接门锁蓝牙...");
                boolean b = BluetoothUtil.openBluetooth();
                if (b) {
                    BluetoothUtil.connectByMac(mac);
                } else {
                    Toast.makeText(BleFunctionActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            type = "B9B902";
            dialog.dismiss();
            currentOrder = null;
            BluetoothUtil.recv_order = null;
            bleFunctionView.setCurrentSec(2);
            mProgressDialog.show();
            DisposableObserver connObserver = getConnObserver();
            Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .take(10).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
            mCompositeDisposable.add(connObserver);
        }

        @Override
        public void weisui_3() {
            if (!BluetoothUtil.isConnected) {
                mProgressDialog.setMessage("正在连接门锁蓝牙...");
                boolean b = BluetoothUtil.openBluetooth();
                if (b) {
                    BluetoothUtil.connectByMac(mac);
                } else {
                    Toast.makeText(BleFunctionActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            type = "B9B903";
            dialog.dismiss();
            currentOrder = null;
            BluetoothUtil.recv_order = null;
            bleFunctionView.setCurrentSec(3);
            mProgressDialog.show();
            DisposableObserver connObserver = getConnObserver();
            Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .take(10).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
            mCompositeDisposable.add(connObserver);
        }

        @Override
        public void weisui_4() {
            if (!BluetoothUtil.isConnected) {
                mProgressDialog.setMessage("正在连接门锁蓝牙...");
                boolean b = BluetoothUtil.openBluetooth();
                if (b) {
                    BluetoothUtil.connectByMac(mac);
                } else {
                    Toast.makeText(BleFunctionActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            type = "B9B904";
            dialog.dismiss();
            currentOrder = null;
            BluetoothUtil.recv_order = null;
            bleFunctionView.setCurrentSec(4);
            mProgressDialog.show();
            DisposableObserver connObserver = getConnObserver();
            Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .take(10).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
            mCompositeDisposable.add(connObserver);
        }

        @Override
        public void weisui_5() {
            if (!BluetoothUtil.isConnected) {
                mProgressDialog.setMessage("正在连接门锁蓝牙...");
                boolean b = BluetoothUtil.openBluetooth();
                if (b) {
                    BluetoothUtil.connectByMac(mac);
                } else {
                    Toast.makeText(BleFunctionActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            type = "B9B905";
            dialog.dismiss();
            currentOrder = null;
            BluetoothUtil.recv_order = null;
            bleFunctionView.setCurrentSec(5);
            mProgressDialog.show();
            DisposableObserver connObserver = getConnObserver();
            Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .take(10).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
            mCompositeDisposable.add(connObserver);
        }

        @Override
        public void remote_start() {
            if (!BluetoothUtil.isConnected) {
                mProgressDialog.setMessage("正在连接门锁蓝牙...");
                boolean b = BluetoothUtil.openBluetooth();
                if (b) {
                    BluetoothUtil.connectByMac(mac);
                } else {
                    Toast.makeText(BleFunctionActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            type = "B7B7A1";
            currentOrder = null;
            BluetoothUtil.recv_order = null;
            dialog.dismiss();
            bleFunctionView.setRemotePW(1);
            mProgressDialog.show();
            DisposableObserver connObserver = getConnObserver();
            Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .take(10).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
            mCompositeDisposable.add(connObserver);
        }

        @Override
        public void remote_close() {
            if (!BluetoothUtil.isConnected) {
                mProgressDialog.setMessage("正在连接门锁蓝牙...");
                boolean b = BluetoothUtil.openBluetooth();
                if (b) {
                    BluetoothUtil.connectByMac(mac);
                } else {
                    Toast.makeText(BleFunctionActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            type = "B7B7A2";
            dialog.dismiss();
            currentOrder = null;
            BluetoothUtil.recv_order = null;
            bleFunctionView.setRemotePW(0);
            mProgressDialog.show();
            DisposableObserver connObserver = getConnObserver();
            Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .take(10).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
            mCompositeDisposable.add(connObserver);
        }

        @Override
        public void fansuo_start() {
            if (!BluetoothUtil.isConnected) {
                mProgressDialog.setMessage("正在连接门锁蓝牙...");
                boolean b = BluetoothUtil.openBluetooth();
                if (b) {
                    BluetoothUtil.connectByMac(mac);
                } else {
                    Toast.makeText(BleFunctionActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            type = "B8B8A1";
            dialog.dismiss();
            currentOrder = null;
            BluetoothUtil.recv_order = null;
            bleFunctionView.setElect(1);
            mProgressDialog.show();
            DisposableObserver connObserver = getConnObserver();
            Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .take(10).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
            mCompositeDisposable.add(connObserver);
        }

        @Override
        public void fansuo_close() {
            if (!BluetoothUtil.isConnected) {
                mProgressDialog.setMessage("正在连接门锁蓝牙...");
                boolean b = BluetoothUtil.openBluetooth();
                if (b) {
                    BluetoothUtil.connectByMac(mac);
                } else {
                    Toast.makeText(BleFunctionActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            type = "B8B8A2";
            dialog.dismiss();
            currentOrder = null;
            BluetoothUtil.recv_order = null;
            bleFunctionView.setElect(0);
            mProgressDialog.show();
            DisposableObserver connObserver = getConnObserver();
            Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .take(10).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
            mCompositeDisposable.add(connObserver);
        }

        //恢复出厂设置
        @Override
        public void restore() {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isRestore", true);
            openView(VerifyPasswordActivity.class, bundle);
        }

        @Override
        public void openWifi() {
            if (!BluetoothUtil.isConnected) {
                mProgressDialog.setMessage("正在连接门锁蓝牙...");
                boolean b = BluetoothUtil.openBluetooth();
                if (b) {
                    BluetoothUtil.connectByMac(mac);
                } else {
                    Toast.makeText(BleFunctionActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            type = "B6B6A1";
            currentOrder = null;
            BluetoothUtil.recv_order = null;
            dialog.dismiss();
            bleFunctionView.setWifiState(1);
            mProgressDialog.show();
            DisposableObserver connObserver = getConnObserver();
            Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .take(10).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
            mCompositeDisposable.add(connObserver);
        }

        @Override
        public void closeWifi() {
            if (!BluetoothUtil.isConnected) {
                mProgressDialog.setMessage("正在连接门锁蓝牙...");
                boolean b = BluetoothUtil.openBluetooth();
                if (b) {
                    BluetoothUtil.connectByMac(mac);
                } else {
                    Toast.makeText(BleFunctionActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            type = "B6B6A2";
            dialog.dismiss();
            currentOrder = null;
            BluetoothUtil.recv_order = null;
            bleFunctionView.setWifiState(0);
            mProgressDialog.show();
            DisposableObserver connObserver = getConnObserver();
            Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .take(10).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
            mCompositeDisposable.add(connObserver);
        }
    }

    //检查蓝牙连接
    private DisposableObserver getConnObserver() {
        return new DisposableObserver() {
            @Override
            public void onNext(Object o) {
                if (BluetoothUtil.isConnected) {
                    dispose();
                    if ("B9B9".equals(type.substring(0, 4))) {
                        requestOrder("B9B9", type.substring(4, 6));
                    } else {
                        requestOrder(type, "");
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                mCompositeDisposable.clear();
                Toast.makeText(BleFunctionActivity.this, "蓝牙连接失败", Toast.LENGTH_SHORT).show();
                BluetoothUtil.closeBluetooth();
                BluetoothUtil.clearInfo();
                mProgressDialog.dismiss();
            }
        };
    }

    //检查是否接收到蓝牙指令
    private DisposableObserver getOrderObserver() {
        return new DisposableObserver() {
            @Override
            public void onNext(Object o) {
                if (BluetoothUtil.recv_order != null) {
                    if (currentOrder == null || !BluetoothUtil.recv_order.equals(currentOrder)) {
                        currentOrder = BluetoothUtil.recv_order;
                        BluetoothUtil.requestResult(currentOrder, BleFunctionActivity.this, mHandle);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                mCompositeDisposable.clear();
                Toast.makeText(BleFunctionActivity.this, "蓝牙通讯失败", Toast.LENGTH_SHORT).show();
                BluetoothUtil.closeBluetooth();
                BluetoothUtil.clearInfo();
                mProgressDialog.dismiss();
            }
        };
    }

    //处理转发接收到的指令
    private class HandleOrder implements BluetoothUtil.BleEvent {

        @Override
        public void success(int code, String message, String content) {
            JSONObject jsonObject = JSONObject.parseObject(content);
            String cmd = jsonObject.getString("cmd");
            String deviceId = jsonObject.getString("devId");
            if (cmd != null) {
                BluetoothUtil.writeCode(cmd);
            }
            if (code != 1) {
                Toast.makeText(BleFunctionActivity.this, message, Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();
                mCompositeDisposable.clear();
                if (code == 12) {
                    sendUnBindResult();
                } else {
                    sendUpdateResult();
                }
            }
        }

        @Override
        public void fail(int code, String message, String content) {
            mCompositeDisposable.clear();
            mProgressDialog.dismiss();
            ToastUtil.toastShort(BleFunctionActivity.this, message);
            JSONObject jsonObject = JSONObject.parseObject(content);
            String cmd = jsonObject.getString("cmd");
            if (cmd != null) {
                BluetoothUtil.writeCode(cmd);
            }
        }
    }

    /**
     * 请求操作指令
     *
     * @param type 指令类型
     * @param sec  秒数
     */
    private void requestOrder(String type, String sec) {
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(this));
        map.put("devId", devId);
        map.put("type", type);
        if ("B9B9".equals(type)) {
            map.put("second", sec);
        }
        RequestUtils.request(RequestUtils.BLE_CMD, this, map).
                enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        String cmd = GsonUtil.getValueByKey("cmd", content);
                        BluetoothUtil.writeCode(cmd);
                        mProgressDialog.setMessage("正在下发指令，请稍候...");
                        DisposableObserver orderObserver = getOrderObserver();
                        Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                                .take(20).observeOn(AndroidSchedulers.mainThread()).subscribe(orderObserver);
                        mCompositeDisposable.add(orderObserver);
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        mProgressDialog.dismiss();
                        ToastUtil.toastShort(BleFunctionActivity.this, message);
                        BluetoothUtil.closeBluetooth();
                        BluetoothUtil.clearInfo();
                    }
                });
    }

    /**
     * 上报门锁修改信息
     */
    private void sendUpdateResult() {
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(this));
        map.put("pid", devId);

        String type_1 = type.substring(0, 4);
        if ("B9B9".equals(type_1)) {
            String type_2 = type.substring(5, 6);
            map.put("isFlowHelp", type_2);
        } else if ("B7B7".equals(type_1)) {
            //远程下发密码
            String remote_type = type.substring(4, 6);
            map.put("isAllowPwd", "A1".equals(remote_type) ? "1" : "0");
        } else if ("B8B8".equals(type_1)) {
            //反锁
            String elect_type = type.substring(4, 6);
            map.put("isElcLock", "A1".equals(elect_type) ? "1" : "0");
        } else if ("B6B6".equals(type_1)) {
            String wifi_type = type.substring(4, 6);
            map.put("isWifiOpen", "A1".equals(wifi_type) ? "1" : "0");
        }
        RequestUtils.request(RequestUtils.BLE_UPDATE, this, map)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        Toast.makeText(BleFunctionActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                        requestDetailInfo();
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        Toast.makeText(BleFunctionActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 上报恢复出厂结果
     */
    private void sendUnBindResult() {
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(this));
        map.put("devId", devId);
        RequestUtils.request(RequestUtils.BLE_UNBIND, this, map).enqueue(new ResponseCallback<BaseResponse>(this) {
            @Override
            protected void onSuccess(int code, String content) {
                super.onSuccess(code, content);
                Toast.makeText(BleFunctionActivity.this, "设备已解除绑定", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(BleFunctionActivity.this,
                        MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                BluetoothUtil.closeBluetooth();
                BluetoothUtil.disConnect();
                BluetoothUtil.clearInfo();
            }
        });
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
                        bleInfo = GsonUtil.json2Bean(content, BleInfo.class);

                        isFlowHelp = bleInfo.getIsFlowHelp();
                        isElcLock = bleInfo.getIsElcLock();
                        isAllowPwd = bleInfo.getIsAllowPwd();
                        hardVersion = bleInfo.getHardVersion();
                        int wifiState = bleInfo.getIsWifiOpen();

                        bleFunctionView.setCurrentSec(Integer.parseInt(isFlowHelp));
                        bleFunctionView.setRemotePW(isAllowPwd);
                        bleFunctionView.setElect(isElcLock);
                        binding.gateLockGate.setText(formatData(hardVersion));
                        binding.funWeisuiState.setText(isFlowHelp + "秒");
                        if (!TextUtils.isEmpty(bleInfo.getSsid())) {
                            binding.funRemoteState.setText(isAllowPwd == 0 ? "关闭" : "开启");
                            binding.funWifiState.setText(wifiState == 0 ? "关闭" : "开启");
                        }
                        binding.funLockState.setText(isElcLock == 0 ? "关闭" : "开启");
                        bleFunctionView.setWifiState(wifiState);
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        Toast.makeText(BleFunctionActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCompositeDisposable.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (!BluetoothUtil.isConnected) {
                mProgressDialog.setMessage("正在连接门锁蓝牙...");
                boolean b = BluetoothUtil.openBluetooth();
                if (b) {
                    BluetoothUtil.connectByMac(mac);
                } else {
                    Toast.makeText(BleFunctionActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            type = "E1E1";
            isRestore = true;
            currentOrder = null;
            BluetoothUtil.recv_order = null;
            dialog.dismiss();
            mProgressDialog.show();
            DisposableObserver connObserver = getConnObserver();
            Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .take(10).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
            mCompositeDisposable.add(connObserver);
        }
    }
}

