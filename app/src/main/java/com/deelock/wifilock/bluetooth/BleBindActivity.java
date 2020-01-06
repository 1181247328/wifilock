package com.deelock.wifilock.bluetooth;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSONObject;
import com.deelock.wifilock.R;
import com.deelock.wifilock.application.CustomApplication;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.databinding.ActivityBleBindBinding;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.activity.BindLockActivity;
import com.deelock.wifilock.ui.dialog.NoticeDialog;
import com.deelock.wifilock.utils.BluetoothUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.StatusBarUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

/**
 * 绑定蓝牙锁(仅支持Android5.0及以上)
 * <p>
 * 蓝牙锁扫描时间12秒，12秒后开始检测BluetoothUtil.scanResults是否有设备，选择信号强度最好的设备进行连接
 * 建立连接到可以进行通讯时间为15秒，超过15秒即蓝牙连接失败(保证蓝牙服务完整)
 * 蓝牙连接成功至绑定成功时间为15秒，超过15秒蓝牙通讯失败(为门锁服务)
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BleBindActivity extends BaseActivity<ActivityBleBindBinding> {

    private int REQUEST_LOCATION = 100;   //定位权限请求码
    private int RESULT_LOCATION = 102;    //定位按钮结果码

    private String currentOrder = null;  //临时指令
    private CompositeDisposable mCompositeDisposable;
    private RotateAnimation rotateAnimation;  //旋转动画

    private HandleOrder mHandle;
    private String finalDevId;   //最终得到的设备Id

    private NoticeDialog dialog;

    //判断当扫描不到附近可以匹配的设备时，我们重新扫描两次
    private int results = 0;

    @Override
    protected int initLayout() {
        return R.layout.activity_ble_bind;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        StatusBarUtil.StatusBarLightMode(this);

        rotateAnimation = new RotateAnimation(-90, 270,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setInterpolator(new LinearInterpolator());   //匀速插值器
        rotateAnimation.setDuration(3000);

        dialog = new NoticeDialog(this, R.style.dialog);

        mCompositeDisposable = new CompositeDisposable();
        mHandle = new HandleOrder();

        binding.setClick(this);
        binding.bleBindTips.setClickable(false);
        binding();

        boolean checkPermission = BluetoothUtil.checkPermission();   //检查定位权限
        if (checkPermission) {
            openBluetooth();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        }
    }

    /**
     * 开启蓝牙搜索
     */
    private void openBluetooth() {
        boolean isOpen = BluetoothUtil.openBluetooth();    //检查蓝牙是否开启，没开启则开启蓝牙
        Log.e("main", "---" + isOpen);
        if (isOpen) {
            Log.e("main", "---1");
            //TODO 通过名字去查找
            BluetoothUtil.connectByName("Deelock");  //通过名字连接门锁设备
            Log.e("main", "---2");
            DisposableObserver scanResultObserver = getScanResultObserver();
            Log.e("main", "---3");
            Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .take(15).observeOn(AndroidSchedulers.mainThread()).subscribe(scanResultObserver);
            Log.e("main", "---4");
            boolean isDis = mCompositeDisposable.add(scanResultObserver);
            Log.e("main", "---5---" + isDis);
        } else {
            bindFail();
            Toast.makeText(this, "蓝牙开启失败，设备搜索失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 返回
     */
    public void onBack() {
        BluetoothUtil.stopBluetooth();
        finish();
    }

    /**
     * 门锁连接失败，重新连接
     */
    public void onReconnect() {
        currentOrder = null;
        binding();
        openBluetooth();
    }

    /**
     * 绑定失败界面
     */
    private void bindFail() {
        currentOrder = null;
        rotateAnimation.cancel();

        binding.bleBindingText.setVisibility(View.GONE);
        binding.bleBindState.setVisibility(View.VISIBLE);
        binding.bleBindTips.setClickable(true);
        binding.bleBindProgress.setImageResource(R.mipmap.ble_bind_fail);
        binding.bleBindStateImg.setImageResource(R.mipmap.ble_bind_state_fail);
        binding.bleBindStateText.setText("门锁蓝牙连接失败");
        SpannableStringBuilder sb = new SpannableStringBuilder("请点击重新连接");
        sb.setSpan(new UnderlineSpan(), 3, 7, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(0xff74d9f7), 3, 7, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        binding.bleBindTips.setText(sb);

        BluetoothUtil.scanResults.clear();
        BluetoothUtil.disConnect();
    }

    /**
     * 绑定成功界面
     */
    private void bindSuccess(String devId) {

        BluetoothUtil.scanResults.clear();
        rotateAnimation.cancel();
        finalDevId = devId;
        binding.bleBindingText.setVisibility(View.GONE);
        binding.bleBindState.setVisibility(View.VISIBLE);
        binding.bleBindTips.setVisibility(View.GONE);
        binding.bleBindProgress.setImageResource(R.mipmap.ble_bind_success);
        binding.bleBindStateImg.setImageResource(R.mipmap.ble_bind_state_success);
        binding.bleBindStateText.setText("门锁连接成功");

        BluetoothUtil.disConnect();

        DisposableObserver waitObserver = waitObserver();
        Observable.interval(2000, 1000, TimeUnit.MILLISECONDS)
                .take(15).observeOn(AndroidSchedulers.mainThread()).subscribe(waitObserver);
        mCompositeDisposable.add(waitObserver);
    }

    /**
     * 设备已连接正在注册
     */
    private void bindConnection() {
        binding.bleBindingText.setText("蓝牙已连接正在注册");
    }

    private DisposableObserver waitObserver() {
        return new DisposableObserver() {

            @Override
            protected void onStart() {
                super.onStart();
//                binding.bleBindStateText.setText("正在配置门锁请稍后");
                binding.bleBindStateText.setText("蓝牙初始化请稍候...");
            }

            @Override
            public void onNext(Object o) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                mCompositeDisposable.clear();
                BluetoothUtil.needOpenLocatopn = true;
                Bundle bundle = new Bundle();
                bundle.putString("authId", "11111111111111111111111111111111");
                bundle.putString("mac", BluetoothUtil.mac);
                bundle.putString("deviceId", finalDevId);
                bundle.putBoolean("isFromBind", true);
                openView(BleAddPwdActivity.class, bundle);
                finish();
            }
        };
    }

    /**
     * 绑定中
     */
    private void binding() {
        binding.bleBindingText.setText("正在连接门锁蓝牙");
        binding.bleBindState.setVisibility(View.GONE);
        binding.bleBindingText.setVisibility(View.VISIBLE);
        binding.bleBindTips.setText("请将手机靠近门锁");
        binding.bleBindTips.setTextColor(Color.rgb(161, 161, 161));
        binding.bleBindProgress.setImageResource(R.mipmap.ble_binding);

        binding.bleBindProgress.setAnimation(rotateAnimation);
        rotateAnimation.startNow();
    }

    /**
     * 检查蓝牙扫描结果
     *
     * @return
     */
    private DisposableObserver getScanResultObserver() {
        return new DisposableObserver() {
            @Override
            public void onNext(Object o) {
                Log.e("main", "onNext");
            }

            @Override
            public void onError(Throwable e) {
                Log.e("main", "onError");
            }

            @Override
            public void onComplete() {
                Log.e("main", "onComplete---" + BluetoothUtil.needOpenLocatopn);
                BluetoothUtil.stopScan();
                mCompositeDisposable.clear();
                if (BluetoothUtil.needOpenLocatopn) {
                    if (!isLocationEnable(BleBindActivity.this)) {
                        showLocationDialog();
                    }
                } else {
                    int size = BluetoothUtil.scanResults.size();
                    Log.e("main", "onComplete---获得的蓝牙数量---" + size);
                    if (size == 0) {
                        results++;
                        if (results == 3) {
                            results = 0;
                            Toast.makeText(BleBindActivity.this, "蓝牙未扫描到任何设备，请重试", Toast.LENGTH_SHORT).show();
                            bindFail();
                        } else {
                            openBluetooth();
                        }
                    } else {
                        results = 0;
                        //TODO 修改了将device改为String
                        BluetoothUtil.startConnect(BluetoothUtil.scanResults.get(size - 1).getDevice().getAddress());
                        DisposableObserver connObserver = getConnObserver();
                        Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                                .take(50).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
                        mCompositeDisposable.add(connObserver);
                    }
                }
            }
        };
    }

    /**
     * 检查是否成功连接至蓝牙门锁
     *
     * @return
     */
    private DisposableObserver getConnObserver() {
        return new DisposableObserver() {

            @Override
            public void onNext(Object o) {
                Log.e("main", "---观察者2onNext---" + BluetoothUtil.isConnected);
                if (BluetoothUtil.isConnected) {
                    dispose();
                    bindConnection();
                    DisposableObserver orderObserver = getOrderObserver();
                    Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                            .take(20).observeOn(AndroidSchedulers.mainThread()).subscribe(orderObserver);
                    mCompositeDisposable.add(orderObserver);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e("main", "---观察者2onError---");
            }

            @Override
            public void onComplete() {
                Log.e("main", "---观察者2onComplete---" + BluetoothUtil.needOpenLocatopn);
                mCompositeDisposable.clear();
                if (BluetoothUtil.needOpenLocatopn) {
                    if (!isLocationEnable(BleBindActivity.this)) {
                        showLocationDialog();
                    }
                } else {
                    Toast.makeText(BleBindActivity.this, "蓝牙连接失败，请重试", Toast.LENGTH_SHORT).show();
                    bindFail();
                }
            }
        };
    }

    /**
     * 负责检测是否接收到蓝牙指令
     *
     * @return
     */
    private DisposableObserver getOrderObserver() {
        return new DisposableObserver() {

            @Override
            public void onNext(Object o) {
                Log.e("main_onNext", o.toString());
                if (BluetoothUtil.recv_order != null) {
                    if (currentOrder == null || !BluetoothUtil.recv_order.equals(currentOrder)) {
                        currentOrder = BluetoothUtil.recv_order;
                        BluetoothUtil.requestResult(currentOrder, BleBindActivity.this, mHandle);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e("main_Throwable", e.toString());
            }

            @Override
            public void onComplete() {
                Log.e("main_onComplete", "onComplete");
                mCompositeDisposable.clear();
                Toast.makeText(BleBindActivity.this, "蓝牙通讯超时", Toast.LENGTH_SHORT).show();
                bindFail();
            }
        };
    }

    /**
     * 处理服务器返回的数据
     */
    private class HandleOrder implements BluetoothUtil.BleEvent {

        @Override
        public void success(int code, String message, String content) {
            Log.e("main", code + "---" + content);
            currentOrder = null;
            JSONObject jsonObject = JSONObject.parseObject(content);
            String cmd = jsonObject.getString("cmd");
            String devId = jsonObject.getString("devId");
            if (cmd != null) {
                BluetoothUtil.writeCode(cmd);
            }
            if (code == 4) {   //绑定成功
                mCompositeDisposable.clear();
                sendBindResult(devId);
            } else if (code == 26) {
                mCompositeDisposable.dispose();
                showErrorMessage("门锁已被绑定，请恢复出厂设置");
                SystemClock.sleep(800);
                bindFail();
            }
        }

        @Override
        public void fail(int code, String message, String content) {
            currentOrder = null;
            JSONObject jsonObject = JSONObject.parseObject(content);
            String cmd = jsonObject.getString("cmd");
            if (cmd != null) {
                BluetoothUtil.writeCode(cmd);
            }
            mCompositeDisposable.dispose();
            if (code == -1005) {
                showErrorMessage("该设备未注册或非法！");
            } else {
                Toast.makeText(BleBindActivity.this, message, Toast.LENGTH_SHORT).show();
            }
            SystemClock.sleep(800);
            bindFail();
        }
    }

    /**
     * 显示绑定错误信息（设备未生产入库，设备已被绑定）
     *
     * @param error 错误信息
     */
    private void showErrorMessage(String error) {
        if (dialog != null) {
            dialog.setNotice(error);
            dialog.show();
        }
    }

    /**
     * 发送绑定结果
     *
     * @param devId 需要绑定的蓝牙设备Id
     */
    private void sendBindResult(final String devId) {
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(CustomApplication.mContext));
        map.put("pid", devId);
        map.put("longitude", String.valueOf(BindLockActivity.longitude));
        map.put("latitude", String.valueOf(BindLockActivity.latitude));
        map.put("phoneNumber", SPUtil.getPhoneNumber(CustomApplication.mContext));
        map.put("type", "A003");
        map.put("code", SPUtil.getStringData(this, devId + "key"));
        map.put("macAddr", BluetoothUtil.mac);
        RequestUtils.request(RequestUtils.BLE_BIND, CustomApplication.mContext, map)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        bindSuccess(devId);
                    }

                    @Override
                    protected void onFailure(BaseResponse response) {
                        super.onFailure(response);
                        SPUtil.saveData(BleBindActivity.this, devId, "");
                        bindFail();
                        Toast.makeText(BleBindActivity.this, "服务器繁忙，请重试", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 提示用户需要打开定位按钮对话框
     */
    private void showLocationDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content("蓝牙搜索未搜索到任何蓝牙设备，需要开启定位服务，否则将导致蓝牙绑定失败")
                .canceledOnTouchOutside(false)
                .positiveText("开启定位")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, RESULT_LOCATION);
                    }
                })
                .negativeText("取消")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Toast.makeText(BleBindActivity.this, "蓝牙搜索失败", Toast.LENGTH_SHORT).show();
                        bindFail();
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOCATION) {
            boolean locationEnable = isLocationEnable(this);
            if (locationEnable) {
                BluetoothUtil.needOpenLocatopn = false;
                ToastUtil.toastLong(this, "绑定成功后请手动关闭定位");
                DisposableObserver scanResultObserver = getScanResultObserver();
                Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                        .take(8).observeOn(AndroidSchedulers.mainThread()).subscribe(scanResultObserver);
                mCompositeDisposable.add(scanResultObserver);
            } else {
                bindFail();
                Toast.makeText(this, "定位开启失败，蓝牙搜索失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 检测是否开启定位
     *
     * @param context 上下文
     * @return 开启定位成功与否
     */
    private boolean isLocationEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }
        boolean networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return networkProvider || gpsProvider;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openBluetooth();
                } else {
                    binding.bleBindPermission.setVisibility(View.VISIBLE);
                    bindFail();
                    Toast.makeText(this, "权限被拒绝，无法搜索蓝牙设备", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        BluetoothUtil.needOpenLocatopn = true;
        BluetoothUtil.stopBluetooth();
        BluetoothUtil.clearInfo();
    }
}
