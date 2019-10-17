package com.deelock.wifilock.bluetooth;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.deelock.wifilock.R;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.databinding.ActivityBleBinding;
import com.deelock.wifilock.entity.BleInfo;
import com.deelock.wifilock.entity.LockState;
import com.deelock.wifilock.entity.Message;
import com.deelock.wifilock.entity.PushList;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.activity.AddFPrintActivity;
import com.deelock.wifilock.ui.activity.AddPasswordActivity;
import com.deelock.wifilock.ui.activity.EquipmentDetailActivity;
import com.deelock.wifilock.ui.activity.HistoryActivity;
import com.deelock.wifilock.ui.activity.UserManageActivity;
import com.deelock.wifilock.ui.dialog.AddInfoDialog;
import com.deelock.wifilock.ui.dialog.BleNetDialog;
import com.deelock.wifilock.utils.BluetoothUtil;
import com.deelock.wifilock.utils.DensityUtil;
import com.deelock.wifilock.utils.GsonUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

/**
 * 蓝牙门锁主界面
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BleActivity extends BaseActivity<ActivityBleBinding> implements AddInfoDialog.InfoClickListener {

    private LockState lockState;
    private AddInfoDialog addInfoDialog;
    private BleInfo bleInfo;
    private String mac;
    private BleNetDialog bleNetDialog;
    private String openCmd;
    private String currentOrder;  //当前接收到来自门锁的蓝牙指令
    private CompositeDisposable mCompositeDisposable;
    private CompositeDisposable mCompositeDisposable1;
    private HandleOrder mHandle;
    private ProgressDialog mDialog;
    private Disposable disposable;
    private CompositeDisposable comDisposable;
    private boolean isSafe = true;         //是否门锁安全
    private boolean isClickOpen = false;    //是否可以点击开门，开门后延时5秒
    private RotateAnimation animation;
    private boolean CanIClick = false;

    private Intent serviceIntent;
    private ProgressDialog mProgressDialog;

    /**
     * CanIUseBluetooth  是否可通过蓝牙操作
     * <p>
     * 连接wifi状态下：ture->通过蓝牙下发、删除密码、删除指纹    false->通过wifi下发、删除密码、删除指纹
     * 未连接wifi状态下：不考虑该条件判断，默认通过蓝牙操作所有
     * 进入该界面时，默认为false,当蓝牙连接成功后，置为true
     */
    public static boolean CanIUseBluetooth = false;

    @Override
    protected int initLayout() {
        return R.layout.activity_ble;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mProgressDialog = new ProgressDialog(this);
        CanIUseBluetooth = false;
        mCompositeDisposable = new CompositeDisposable();
        mCompositeDisposable1 = new CompositeDisposable();
        comDisposable = new CompositeDisposable();
        mDialog = new ProgressDialog(this);
        mHandle = new HandleOrder();

        bleNetDialog = new BleNetDialog(this, R.style.dialog);

        binding.setClick(this);
        lockState = getIntent().getParcelableExtra("lockState");

        int isManager = lockState.getIsManager();   //1.主用户 2.授权用户
        if (isManager == 2) {

            binding.bleHistory.post(new Runnable() {
                @Override
                public void run() {
                    binding.bleSetting.setVisibility(View.GONE);
                    binding.bleAddLl.setVisibility(View.GONE);
                    binding.bleUserManager.setVisibility(View.GONE);

                    RelativeLayout.LayoutParams layoutParams =
                            new RelativeLayout.LayoutParams(binding.bleHistory.getRight() - binding.bleHistory.getLeft(),
                                    binding.bleHistory.getBottom() - binding.bleHistory.getTop());
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    binding.bleHistory.setLayoutParams(layoutParams);
                }
            });
        }

        addInfoDialog = new AddInfoDialog(this, R.style.mydialog);
        addInfoDialog.setCurrentView("A003");
        addInfoDialog.setInfoListener(this);

        int isOnline = lockState.getIsOnline();
        if (isOnline == 0) {
            binding.bleWater.setNetState(true);
        } else {
            binding.bleWater.setNetState(true);
        }
        int screenWidth = DensityUtil.getScreenWidth(this);
        int daySize = 30 * screenWidth / 750;
        String day = String.valueOf(TimeUtil.getDay(lockState.getTimeBind()));
        String time = new StringBuffer("第  天").insert(2, day).toString();
        SpannableStringBuilder sb = new SpannableStringBuilder(time);
        AbsoluteSizeSpan as = new AbsoluteSizeSpan(daySize);
        sb.setSpan(as, 2, 2 + day.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(0xff4c4c4c), 2, 2 + day.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        binding.bleDay.setText(sb);
        mac = lockState.getMacAddr();

        binding.bleSingleRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleNetDialog.show();
                bleNetDialog.setState(2);
            }
        });

        binding.bleCircleOuter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSafe) {
                    Intent intent = new Intent("com.deelock.wifilock.unSafe");
                    intent.putExtra("type", 1);
                    LocalBroadcastManager.getInstance(BleActivity.this).sendBroadcast(intent);
                    finish();
                }
            }
        });
        binding.bleCircleOuter.setClickable(false);
        isClickOpen = true;

        serviceIntent = new Intent(this, BleService.class);
        serviceIntent.putExtra("mac", mac);
        if (BluetoothUtil.openBluetooth()) {
            linking();
            SystemClock.sleep(800);
            boolean checkPermission = BluetoothUtil.checkPermission();   //检查定位权限
            if (checkPermission) {
                startService(serviceIntent);
                checkConnect();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 300);
            }
        } else {
            linkSuccess();
            CanIClick = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 300) {
            if (permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService(serviceIntent);
                    checkConnect();
                } else {
                    Toast.makeText(this, "权限被拒绝，无法正常使用蓝牙功能", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (BluetoothUtil.openBluetooth()) {
            linking();
            SystemClock.sleep(500);
            startService(serviceIntent);
            checkConnect();
        } else {
            linkSuccess();
            CanIClick = true;
        }
    }

    /**
     * 检查蓝牙连接
     */
    private void checkConnect() {
        DisposableObserver checkObserver = getCheckObserver();
        Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                .take(120).observeOn(AndroidSchedulers.mainThread()).subscribe(checkObserver);
        mCompositeDisposable.add(checkObserver);
    }

    /**
     * 蓝牙连接失败
     */
    private void linkFail() {
        binding.bleStateLl.setClickable(true);
        binding.bleStateText.setText("门锁不在附近");
        binding.bleState.setImageResource(R.mipmap.ble_not_found);
        binding.bleStateText.setTextSize(14);
        binding.bleStateText.setTextColor(Color.rgb(153, 153, 153));
    }

    /**
     * 蓝牙连接中
     */
    private void linking() {
        binding.bleStateLl.setClickable(false);
        binding.bleState.setImageResource(R.mipmap.ble_normal);
        binding.bleStateText.setText("连接设备中");
        binding.bleStateText.setTextSize(13);
        binding.bleStateText.setTextColor(Color.rgb(67, 227, 201));
    }

    /**
     * 蓝牙连接成功
     */
    private void linkSuccess() {
        binding.bleState.setImageResource(R.mipmap.ble_normal);
        binding.bleStateText.setText("点击开锁");
        binding.bleStateText.setTextSize(14);
        binding.bleStateText.setTextColor(Color.rgb(67, 227, 201));
    }

    /**
     * 开启蓝牙(点击开锁后执行)
     */
    private void openBle() {
        isClickOpen = false;
        currentOrder = null;
        BluetoothUtil.recv_order = null;
        boolean b = BluetoothUtil.openBluetooth();
        if (b) {
            BluetoothUtil.connectByMac(mac);
            DisposableObserver connObserver = getConnObserver();
            Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .take(10).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
            mCompositeDisposable.add(connObserver);
        } else {
            isClickOpen = true;
            linkSuccess();
            binding.bleStateLl.setClickable(true);
        }
    }

    private DisposableObserver getCheckObserver() {
        return new DisposableObserver() {

            @Override
            public void onNext(Object o) {
                if (BluetoothUtil.isConnected) {
                    dispose();
                    linkSuccess();
                    CanIClick = true;
                    CanIUseBluetooth = true;
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                CanIClick = true;
                linkFail();
                Toast.makeText(BleActivity.this, "请稍后再试", Toast.LENGTH_LONG).show();
            }
        };
    }

    //蓝牙连接失败后，检查蓝牙连接
    private DisposableObserver getConnObserver() {
        return new DisposableObserver() {
            @Override
            public void onNext(Object o) {
                if (BluetoothUtil.isConnected) {
                    dispose();
                    CanIUseBluetooth = true;
                    mDialog.setMessage("正在请求开门，请稍后...");
                    mDialog.show();
                    binding.bleStateText.setText("正在开门中");
                    binding.bleStateText.setTextSize(14);
                    binding.bleStateText.setTextColor(Color.rgb(67, 227, 201));
                    requestOpenLockOrder();
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                isClickOpen = true;
                CanIUseBluetooth = false;
                mDialog.dismiss();
                mCompositeDisposable.clear();
                binding.bleStateLl.setClickable(true);
                BluetoothUtil.closeBluetooth();
                BluetoothUtil.clearInfo();
                bleNetDialog.show();
                bleNetDialog.setState(1);
                linkFail();
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
                        BluetoothUtil.requestResult(currentOrder, BleActivity.this, mHandle);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                isClickOpen = true;
                mDialog.dismiss();
                mCompositeDisposable.clear();
                binding.bleStateLl.setClickable(true);
                Toast.makeText(BleActivity.this, "蓝牙通讯失败", Toast.LENGTH_SHORT).show();
                BluetoothUtil.closeBluetooth();
                BluetoothUtil.clearInfo();
                linkSuccess();
            }
        };
    }

    //处理转发接收到的指令
    private class HandleOrder implements BluetoothUtil.BleEvent {

        @Override
        public void success(int code, String message, String content) {
            JSONObject jsonObject = JSONObject.parseObject(content);
            String cmd = jsonObject.getString("cmd");
            if (cmd != null) {
                BluetoothUtil.writeCode(cmd);
            }
            if (code == 11) {
                mCompositeDisposable.clear();
                mDialog.dismiss();
                linkSuccess();
                binding.bleStateLl.setClickable(true);
                BluetoothUtil.clearInfo();
                Toast.makeText(BleActivity.this, "门已开启", Toast.LENGTH_SHORT).show();
                DisposableObserver delayObserver = getDelayObserver();
                Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                        .take(5).observeOn(AndroidSchedulers.mainThread()).subscribe(delayObserver);
                mCompositeDisposable.add(delayObserver);
            }
        }

        @Override
        public void fail(int code, String message, String content) {
            mCompositeDisposable.clear();
            mDialog.dismiss();
            isClickOpen = true;
            binding.bleStateLl.setClickable(true);
            ToastUtil.toastShort(BleActivity.this, message);
            JSONObject jsonObject = JSONObject.parseObject(content);
            String cmd = jsonObject.getString("cmd");
            if (cmd != null) {
                BluetoothUtil.writeCode(cmd);
            }
            linkSuccess();
        }
    }

    //开门后延时5秒
    private DisposableObserver getDelayObserver() {
        return new DisposableObserver() {
            @Override
            public void onNext(Object o) {
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                isClickOpen = true;
                binding.bleStateLl.setClickable(true);
                mCompositeDisposable.clear();
            }
        };
    }

    /**
     * 返回
     */
    public void back() {
        finish();
    }

    /**
     * 设置按钮
     */
    public void setting() {
        if (bleInfo == null) {
            Toast.makeText(this, "请稍后", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra("devId", lockState.getPid());
        intent.putExtra("mac", lockState.getMacAddr());
        intent.putExtra("isFlow", Integer.parseInt(bleInfo.getIsFlowHelp()));
        intent.putExtra("nickname", bleInfo.getNickName());
        intent.putExtra("hardVersion", bleInfo.getHardVersion());
        intent.putExtra("softVersion", bleInfo.getSoftVersion());
        intent.putExtra("wifiName", bleInfo.getSsid());
        intent.putExtra("remotePw", bleInfo.getIsAllowPwd());
        intent.putExtra("fansuo", bleInfo.getIsElcLock());
        startActivityForResult(intent, 2);
    }

    /**
     * 开锁
     */
    public void kaisuo() {
        if (!isClickOpen) {
            Toast.makeText(this, "当前操作过于频繁，请5秒后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!CanIClick) {
            Toast.makeText(this, "请稍后", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.bleStateLl.setClickable(false);
        linking();
        openBle();
    }

    /**
     * 请求开锁指令
     */
    private void requestOpenLockOrder() {
        if (bleInfo == null) {
            Toast.makeText(this, "请等待网络配置完成", Toast.LENGTH_SHORT).show();
            return;
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("type", "A3A9");
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(this));
        map.put("devId", lockState.getPid());
        RequestUtils.request(RequestUtils.BLE_CMD, this, map)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        mDialog.setMessage("正在开门中，请稍后...");
                        openCmd = GsonUtil.getValueByKey("cmd", content);
                        BluetoothUtil.writeCode(openCmd);
                        DisposableObserver orderObserver = getOrderObserver();
                        Observable.interval(0, 200, TimeUnit.MILLISECONDS)
                                .take(50).observeOn(AndroidSchedulers.mainThread()).subscribe(orderObserver);
                        mCompositeDisposable.add(orderObserver);
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        Toast.makeText(BleActivity.this, message, Toast.LENGTH_SHORT).show();
                        mCompositeDisposable.clear();
                        isClickOpen = true;
                        binding.bleStateLl.setClickable(true);
                        mDialog.dismiss();
                        linkSuccess();
                    }
                });
    }

    /**
     * 用户管理
     */
    public void manage() {
        Intent intent = new Intent(this, UserManageActivity.class);
        intent.putExtra("sdlId", lockState.getPid());

        startActivity(intent);
    }

    /**
     * 添加
     */
    public void add() {
        addInfoDialog.show();
    }

    /**
     * 历史记录
     */
    public void history() {
        if (bleInfo == null) {
            Toast.makeText(this, "请等待网络完成配置", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(bleInfo.getSsid())) {
            bleNetDialog.show();
            bleNetDialog.setState(2);
            return;
        }
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra("sdlId", lockState.getPid());
        startActivity(intent);
    }

    /**
     * 添加密码
     */
    @Override
    public void addPwd() {
        if (bleInfo == null) {
            Toast.makeText(this, "请等待门锁配置完成", Toast.LENGTH_SHORT).show();
            return;
        }
        addInfoDialog.dismiss();
        Bundle bundle = new Bundle();
        bundle.putString("sdlId", lockState.getPid());
        bundle.putString("mac", lockState.getMacAddr());
        openView(AddPasswordActivity.class, bundle);
    }

    /**
     * 添加指纹
     */
    @Override
    public void addFprint() {
        if (bleInfo == null) {
            Toast.makeText(this, "请等待门锁配置完成", Toast.LENGTH_SHORT).show();
            return;
        }
        addInfoDialog.dismiss();
        Bundle bundle = new Bundle();
        bundle.putString("mac", lockState.getMacAddr());
        bundle.putString("sdlId", lockState.getPid());
        openView(AddFPrintActivity.class, bundle);
    }

    /**
     * 添加卡片
     */
    @Override
    public void addCard() {

    }

    /**
     * 上报蓝牙指令开门
     */
    private void sendOpenHistory() {

    }

    /**
     * 请求门锁详情
     */
    private void requestDetailInfo() {
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(this));
        map.put("pid", lockState.getPid());
        RequestUtils.request(RequestUtils.BLE_INFO, this, map)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        bleInfo = GsonUtil.json2Bean(content, BleInfo.class);
                        boolean canUseWifi = bleInfo.getSsid() != null && bleInfo.getIsOnline() == 1;
                        SPUtil.saveData(BleActivity.this,  lockState.getPid() + "wifi", canUseWifi);
                        SPUtil.saveData(BleActivity.this,  lockState.getPid()+ "onlyWifi", bleInfo.getSsid() != null);
                        SPUtil.saveData(BleActivity.this,  lockState.getPid() + "remote", bleInfo.getIsAllowPwd() == 1);   //为1时远程下发密码开启
                        binding.bleName.setText(bleInfo.getNickName());
                        if (TextUtils.isEmpty(bleInfo.getSsid())) {
                            binding.bleSingle1Rl.setVisibility(View.GONE);
                            binding.bleBatteryLl.setVisibility(View.GONE);
                            binding.bleSingleRl.setVisibility(View.VISIBLE);
                        } else {
                            binding.bleSingleRl.setVisibility(View.GONE);
                            binding.bleSingle1Rl.setVisibility(View.VISIBLE);
                            binding.bleBatteryLl.setVisibility(View.VISIBLE);
                            int signal;
                            if (bleInfo.getSignalStrength() == null) {
                                signal = 100;
                            } else {
                                signal = Integer.parseInt(bleInfo.getSignalStrength(), 16);
                            }
                            if (signal > 30) {
                                binding.bleSingle1Text.setText("强");
                                binding.bleSingle1.setImageResource(R.mipmap.signal_full);
                            } else if (signal > 15) {
                                binding.bleSingle1Text.setText("中");
                                binding.bleSingle1.setImageResource(R.mipmap.signal_common);
                            } else {
                                binding.bleSingle1Text.setText("弱");
                                binding.bleSingle1.setImageResource(R.mipmap.signal_low);
                            }
                            if (!TextUtils.isEmpty(bleInfo.getPower())) {
                                int power = Integer.parseInt(bleInfo.getPower(), 16);
                                binding.bleBatteryText.setText(getString(R.string.power_notify, power));
                                if (power < 70 && power >= 20) {
                                    binding.bleBattery.setImageResource(R.mipmap.power_3);
                                } else if (power < 20 && power >= 10) {
                                    binding.bleBattery.setImageResource(R.mipmap.power_2);
                                } else if (power < 10) {
                                    binding.bleBattery.setImageResource(R.mipmap.power_1);
                                }
                            }
                        }
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        Toast.makeText(BleActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 请求门锁消息
     */
    private void requestMessage() {
        HashMap<String, String> params1 = new HashMap<>();
        params1.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params1.put("uid", SPUtil.getUid(this));
        params1.put("count", String.valueOf(100));
        params1.put("minTime", String.valueOf(-1));
        params1.put("maxTime", String.valueOf(-1));
        RequestUtils.request(RequestUtils.MESSAGE, this, params1).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        PushList pushList = GsonUtil.json2Bean(content, PushList.class);
                        List<Message> messages = pushList.getList();
                        for (Message m : messages) {
                            if (lockState.getPid().equals(m.getDevId())) {
                                isSafe = false;
                                binding.bleCircleOuter.setClickable(true);
                                binding.bleStateLl.setClickable(false);
                                binding.bleStateText.setText("存在风险");
                                binding.bleState.setImageResource(R.mipmap.ble_unsafe);
                                binding.bleStateText.setTextColor(0xffff8239);
                                binding.bleCircleInner.setImageResource(R.mipmap.circle_inner_unsafe);
                                binding.bleCircleOuter.setImageResource(R.mipmap.circle_outer_unsafe);
                                binding.bleWater.setNetState(false);
                                return;
                            }
                        }
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        comDisposable.dispose();
                    }
                }
        );
    }

    /**
     * 转动动画
     */
    private void setAnimation() {
        animation = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(5000);
        binding.bleCircleOuter.startAnimation(animation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setAnimation();
        binding.bleStateLl.setClickable(true);
        isClickOpen = true;
        requestDetailInfo();
        boolean data = SPUtil.getBooleanData(this, lockState.getPid() + "onlyWifi");
        if (data) {
            Observable.interval(0, 5, TimeUnit.SECONDS).subscribe(new Observer<Long>() {
                @Override
                public void onSubscribe(Disposable d) {
                    disposable = d;
                }

                @Override
                public void onNext(Long aLong) {
                    requestMessage();
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });
            comDisposable.add(disposable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        comDisposable.clear();
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mCompositeDisposable.clear();
        if (animation != null) {
            animation.cancel();
            animation = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable = null;
        mCompositeDisposable1.clear();
        comDisposable = null;
        stopService(serviceIntent);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        String name = data.getStringExtra("name");
        if (requestCode == 2) {
            if (!TextUtils.isEmpty(name)) {
                binding.bleName.setText(name);
            }
        }
    }

}
