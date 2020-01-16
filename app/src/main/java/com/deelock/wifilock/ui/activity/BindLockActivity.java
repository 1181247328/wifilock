package com.deelock.wifilock.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.deelock.state.BleListActivity;
import com.deelock.state.LockState;
import com.deelock.wifilock.R;
import com.deelock.wifilock.entity.Bind;
import com.deelock.wifilock.entity.FPrintList;
import com.deelock.wifilock.entity.User;
import com.deelock.wifilock.entity.UserList;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.dialog.NoticeDialog;
import com.deelock.wifilock.utils.BluetoothUtil;
import com.deelock.wifilock.utils.DensityUtil;
import com.deelock.wifilock.utils.GsonUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.StatusBarUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.google.gson.Gson;
import com.xuhong.xsmartconfiglib.EsptouchTask;
import com.xuhong.xsmartconfiglib.IEsptouchResult;
import com.xuhong.xsmartconfiglib.IEsptouchTask;
import com.xuhong.xsmartconfiglib.task.__IEsptouchTask;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by binChuan on 2017\9\19 0019.
 */
@SuppressLint("NewApi")
public class BindLockActivity extends BaseActivity {

    String type;
    TranslateAnimation animation;
    ImageView hand_iv;
    private Context context = null;
    ImageView mag_iv;
    public static double latitude = 0;
    public static double longitude = 0;
    boolean sendAble;
    NoticeDialog dialog;
    private ProgressDialog mProgressDialog;
    String wifi;
    int count;
    Disposable disposable;
    String mac;
    String deviceId;
    String bssid;
    String password;
    private int isEvaluate;
    private boolean isAnimated;

    private CompositeDisposable comDisposable;
    private String currentOrder;
    private HandleOrder mHandle;

    //中间逻辑处理层
    private LockState lockState = LockState.getLockState();

    //监听器
    private static CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    //发送WIFI到蓝牙的绑定指令
    private String wifiCode;

    @Override
    protected void bindActivity() {
        setContentView(R.layout.activity_bind_lock);
    }

    @Override
    protected void setCallBack() {
        super.setCallBack();
    }

    @Override
    protected void doBusiness() {
        mProgressDialog = new ProgressDialog(this);
        StatusBarUtil.StatusBarLightMode(this);
        hand_iv = findViewById(R.id.hand_iv);
        mag_iv = findViewById(R.id.mag_iv);
        TextView tv = findViewById(R.id.title_tv);
        TextView notice_tv = findViewById(R.id.notice_tv);
        ImageView image_iv = findViewById(R.id.image_iv);
        context = this;
        mac = getIntent().getStringExtra("mac");
        deviceId = getIntent().getStringExtra("deviceId");
        wifi = getIntent().getStringExtra("wifi");
        bssid = getIntent().getStringExtra("bssid");
        password = getIntent().getStringExtra("password");
        mHandle = new HandleOrder();
        type = getIntent().getStringExtra("type");


        if (type != null && "A00".equals(type.substring(0, 3))) {
            tv.setText("连接门锁");
            if (!type.substring(0, 4).equals("A003")) {
                notice_tv.setText("长按“联网”键5秒后,\n待门锁发出提示音后,\n点击“开始”。");
            } else {
                if (mac != null) {
                    notice_tv.setText("请将手机靠近门锁\n点击开始\n");
                    tv.setText("配置网络");
                    image_iv.setVisibility(View.GONE);
                    hand_iv.setVisibility(View.GONE);
                }
            }
        } else {
            if ("000".equals(type)) {
                tv.setText("网关配置");
                notice_tv.setText("用顶针插入网关背后的小孔3秒后，\n待指示灯亮起后，松开顶针\n点击开始。");
            } else if ("C00".equals(type)) {
                tv.setText("门磁配置");
                notice_tv.setText("用顶针插入门磁背后的小孔3秒后，\n待指示灯亮起后，松开顶针\n点击开始。");
            } else if ("B002".equals(type)) {
                tv.setText("红外配置");
                notice_tv.setText("用顶针插入红外背后的小孔3秒后，\n待指示灯亮起后，松开顶针\n点击开始。");
            } else {
                tv.setText("门磁配置");
                notice_tv.setText("用顶针插入门磁背后的小孔3秒后，\n待指示灯亮起后，松开顶针\n点击开始。");
            }
            mag_iv.setVisibility(View.VISIBLE);
            image_iv.setVisibility(View.GONE);
            hand_iv.setVisibility(View.GONE);
        }

        dialog = new NoticeDialog(this, R.style.dialog);
        comDisposable = new CompositeDisposable();
    }

    @Override
    protected void setEvent() {
        findViewById(R.id.back_ib).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.configure_ib).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                if ("C00".equals(type)) {
                    bindZigBee();
                } else if ("C01".equals(type)) {
                    bindinfra();
                } else if ("A003".equals(type)) {
                    if (mac != null) {
                        boolean b = BluetoothUtil.openBluetooth();
                        if (b) {
                            currentOrder = null;
                            BluetoothUtil.recv_order = null;
                            BluetoothUtil.connectByMac(mac);
                            mProgressDialog.setMessage("正在连接门锁蓝牙...");
                            mProgressDialog.show();
                            DisposableObserver connObserver = getConnObserver();
                            Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                                    .take(15).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
                            comDisposable.add(connObserver);

                        } else {
                            Toast.makeText(BindLockActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                        }
                    } else {
//                        Intent intent = new Intent(BindLockActivity.this, BleBindActivity.class);
//                        startActivity(intent);
                        Intent intent = new Intent(BindLockActivity.this, BleListActivity.class);
                        startActivity(intent);
                    }
                } else {
                    Log.e("main_bind", "---wifi---" + wifi + "---bssid---" + bssid + "---password---" + password);
                    new EsptouchAsyncTask3().execute(wifi, bssid, password, "1");
                    requestBind();
                }
            }
        });
    }

    //检查蓝牙连接
    private DisposableObserver getConnObserver() {
        return new DisposableObserver() {
            @SuppressLint("NewApi")
            @Override
            public void onNext(Object o) {
                Log.e("main_bind", "---连接中---" + BluetoothUtil.isConnected);
                if (BluetoothUtil.isConnected) {
                    dispose();
                    bleBindingWifi();
                    //TODO 绑定wifi
//                   requestBindBleWifi();
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @SuppressLint("NewApi")
            @Override
            public void onComplete() {
                comDisposable.clear();
                Toast.makeText(BindLockActivity.this, "蓝牙连接失败", Toast.LENGTH_SHORT).show();
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
                        BluetoothUtil.requestResult(currentOrder, BindLockActivity.this, mHandle);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                comDisposable.clear();
                Toast.makeText(BindLockActivity.this, "蓝牙通讯失败", Toast.LENGTH_SHORT).show();
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
            if (cmd != null) {
                BluetoothUtil.writeCode(cmd);
            }
            if (code == 25) {
                mProgressDialog.dismiss();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new EsptouchAsyncTask3().execute(wifi, bssid, password, "1");
            }
            if (code == 5) {
                requestBindResult();
            }
        }

        @Override
        public void fail(int code, String message, String content) {
            comDisposable.clear();
            mProgressDialog.dismiss();
            ToastUtil.toastShort(BindLockActivity.this, message);
            JSONObject jsonObject = JSONObject.parseObject(content);
            String cmd = jsonObject.getString("cmd");
            if (cmd != null) {
                BluetoothUtil.writeCode(cmd);
            }
        }
    }

    /**
     * 上报蓝牙门锁绑定wifi结果
     */
    private void requestBindResult() {
        final Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        if (latitude - 4.9E-324 == 0 || longitude - 4.9E-324 == 0) {
            params.put("longitude", 0);
            params.put("latitude", 0);
        } else {
            DecimalFormat df = new DecimalFormat("0.00000");
            params.put("longitude", df.format(longitude));
            params.put("latitude", df.format(latitude));
        }
        params.put("ssid", wifi);
        params.put("phoneNumber", SPUtil.getPhoneNumber(this));
        params.put("type", type.substring(0, 3));
        params.put("devId", deviceId);
        RequestUtils.request(RequestUtils.BLE_BIND_RESULT, this, params).enqueue(new ResponseCallback<BaseResponse>(this) {
            @Override
            protected void onSuccess(int code, String content) {
                super.onSuccess(code, content);
                if (code == 1) {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                    comDisposable.dispose();
                    ToastUtil.toastShort(BindLockActivity.this, "WiFi配置成功");
                    finish();
                }
            }

            @Override
            protected void onFailure(int code, String message) {
                super.onFailure(code, message);
                ToastUtil.toastShort(BindLockActivity.this, message);
                comDisposable.dispose();
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    /**
     * 通过蓝牙进行绑定wifi
     */
    private void bleBindingWifi() {
        //将通过蓝牙将WIFI密码、名称全部发送给蓝牙
        mProgressDialog.setMessage("正在请求绑定指令...");
        HashMap<String, Object> params = new HashMap<>();
        //用户id
        params.put("uid", SPUtil.getUid(BindLockActivity.this));
        //设备id
        params.put("devId", deviceId);
        //时间
        params.put("timestamp", TimeUtil.getTime());
        //发送指令
        params.put("type", "C3C3");
        //wifi名称
        params.put("wifiName", wifi);
        //wifi密码
        params.put("wifiWord", password);
        RequestUtils.request(RequestUtils.BLE_WIFI, BindLockActivity.this, params).enqueue(
                new ResponseCallback<BaseResponse>(BindLockActivity.this) {
                    @Override
                    protected void onSuccess(int code, final String content) {
                        super.onSuccess(code, content);
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }
                        Log.e("BindLockActivity", "---onSuccess---" + content);
                        wifiCode = GsonUtil.getValueByKey("cmd", content);
                        Log.e("BindLockActivity", "---" + wifiCode);
                        mProgressDialog.setMessage("下发绑定指令...");
                        mProgressDialog.show();
                        BluetoothUtil.writeCode(wifiCode);
                        DisposableObserver orderObserver = getWifiObserver();
                        Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                                .take(30).observeOn(AndroidSchedulers.mainThread()).subscribe(orderObserver);
                        mCompositeDisposable.add(orderObserver);
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        Log.e("BindLockActivity", "---onFailure---" + message);
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }
                        Toast.makeText(BindLockActivity.this, "获取绑定指令失败,请重新请求", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 上传从蓝牙得到的数据
     */
    private void bleUploadWifi(String word) {
        //将通过蓝牙将WIFI密码、名称全部发送给蓝牙
        mProgressDialog.setMessage("指令上传中...");
        mProgressDialog.show();
        HashMap<String, Object> params = new HashMap<>();
        //用户id
        params.put("uid", SPUtil.getUid(BindLockActivity.this));
        //时间
        params.put("timestamp", TimeUtil.getTime());
        //发送指令
        params.put("type", "C3C3");
        //获得的数据
        Log.e("BindLockActivity", "---" + word);
        params.put("lockCmd", word);
        RequestUtils.request(RequestUtils.BLE_WIFI_UPLOAD, BindLockActivity.this, params).enqueue(
                new ResponseCallback<BaseResponse>(BindLockActivity.this) {
                    @Override
                    protected void onSuccess(int code, final String content) {
                        super.onSuccess(code, content);
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }
                        Log.e("BindLockActivity", "---" + code + "---" + content);
                        if (code == 27) {
                            Toast.makeText(context, "wifi配置成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(context, "wifi配置失败,请重新配置", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        Log.e("BindLockActivity", "---onFailure---" + message);
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }
                        Toast.makeText(BindLockActivity.this, "获取绑定指令失败,请重新请求", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 请求绑定蓝牙门锁wifi指令并下发
     */
    private void requestBindBleWifi() {
        mProgressDialog.setMessage("正在联网请求指令...");
        HashMap<String, String> map = new HashMap<>();
        map.put("type", "C2");
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(this));
        map.put("devId", deviceId);
        RequestUtils.request(RequestUtils.BLE_CMD, this, map).enqueue(new ResponseCallback<BaseResponse>(this) {
            @Override
            protected void onSuccess(int code, String content) {
                super.onSuccess(code, content);
                String cmd = GsonUtil.getValueByKey("cmd", content);
                mProgressDialog.setMessage("正在下发绑定指令...");
                BluetoothUtil.writeCode(cmd);
                DisposableObserver orderObserver = getOrderObserver();
                Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                        .take(60).observeOn(AndroidSchedulers.mainThread()).subscribe(orderObserver);
                comDisposable.add(orderObserver);
            }

            @Override
            protected void onFailure(int code, String message) {
                super.onFailure(code, message);
                mProgressDialog.dismiss();
                Toast.makeText(BindLockActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 绑定红外
     */
    private void bindinfra() {
        final int[] a = {0};
        final String devId = getIntent().getStringExtra("devId");
        mProgressDialog = new ProgressDialog(BindLockActivity.this);
        mProgressDialog.setMessage("设备绑定中...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
        Observer<Long> observer = new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(Long aLong) {
                a[0]++;
                HashMap<String, String> params = new HashMap<>();
                params.put("timestamp", String.valueOf(TimeUtil.getTime()));
                params.put("uid", SPUtil.getUid(BindLockActivity.this));
                params.put("gtwId", devId);
                params.put("devType", "C010");
                RequestUtils.request(RequestUtils.INFRARED_BIND, BindLockActivity.this, params)
                        .enqueue(new ResponseCallback<BaseResponse>(BindLockActivity.this) {
                            @Override
                            protected void onSuccess(int code, String content) {
                                super.onSuccess(code, content);
                                if (code == 1) {
                                    disposable.dispose();
                                    mProgressDialog.dismiss();
                                    Toast.makeText(BindLockActivity.this, "红外已绑定", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(BindLockActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else if (code == 2) {
                                    if (a[0] == 60) {
                                        disposable.dispose();
                                        mProgressDialog.dismiss();
                                        Toast.makeText(BindLockActivity.this, "绑定超时", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            protected void onFailure(int code, String message) {
                                super.onFailure(code, message);
                                Toast.makeText(BindLockActivity.this, message, Toast.LENGTH_LONG).show();
                                disposable.dispose();
                                mProgressDialog.dismiss();
                            }
                        });
            }

            @Override
            public void onError(Throwable e) {
                disposable.dispose();
            }

            @Override
            public void onComplete() {

            }
        };
        Observable.interval(0, 5, TimeUnit.SECONDS).subscribe(observer);
        comDisposable.add(disposable);
    }

    /**
     * 绑定zigbee门磁
     */
    private void bindZigBee() {
        final int[] a = {0};
        final String devId = getIntent().getStringExtra("devId");
        mProgressDialog = new ProgressDialog(BindLockActivity.this);
        mProgressDialog.setMessage("设备绑定中...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
        Observer<Long> observer = new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(Long aLong) {
                a[0]++;
                HashMap<String, String> params = new HashMap<>();
                params.put("timestamp", String.valueOf(TimeUtil.getTime()));
                params.put("uid", SPUtil.getUid(BindLockActivity.this));
                params.put("gtwId", devId);
                params.put("devType", "C00");
                RequestUtils.request(RequestUtils.ZIGBEE_BIND, BindLockActivity.this, params)
                        .enqueue(new ResponseCallback<BaseResponse>(BindLockActivity.this) {
                            @Override
                            protected void onSuccess(int code, String content) {
                                super.onSuccess(code, content);
                                if (code == 1) {
                                    disposable.dispose();
                                    mProgressDialog.dismiss();
                                    Toast.makeText(BindLockActivity.this, "Zigbee门磁已绑定", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(BindLockActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else if (code == 2) {
                                    if (a[0] == 30) {
                                        disposable.dispose();
                                        mProgressDialog.dismiss();
                                        Toast.makeText(BindLockActivity.this, "绑定超时", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            protected void onFailure(int code, String message) {
                                super.onFailure(code, message);
                                mProgressDialog.dismiss();
                                Toast.makeText(BindLockActivity.this, message, Toast.LENGTH_LONG).show();
                                disposable.dispose();
                            }
                        });
            }

            @Override
            public void onError(Throwable e) {
                disposable.dispose();
            }

            @Override
            public void onComplete() {

            }
        };
        Observable.interval(0, 5, TimeUnit.SECONDS).subscribe(observer);
        comDisposable.add(disposable);
    }

    private void requestBind() {
        final Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        if (latitude - 4.9E-324 == 0 || longitude - 4.9E-324 == 0) {
            params.put("longitude", 0);
            params.put("latitude", 0);
        } else {
            DecimalFormat df = new DecimalFormat("0.00000");
            params.put("longitude", df.format(longitude));
            params.put("latitude", df.format(latitude));
        }

        params.put("ssid", wifi);
        params.put("phoneNumber", SPUtil.getPhoneNumber(this));
        params.put("type", type.substring(0, 3));

        count = 0;
        Observer observer = new Observer<Long>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(@NonNull Long aLong) {
                if (!isNetworkAvailable()) {
                    comDisposable.clear();
                    mProgressDialog.dismiss();
                    return;
                }

                count++;

                if (count > 45) {

                }

                RequestUtils.request(RequestUtils.BIND, BindLockActivity.this, params).enqueue(
                        new ResponseCallback<BaseResponse>(BindLockActivity.this) {
                            @Override
                            protected void onSuccess(int code, String content) {
                                super.onSuccess(code, content);
                                if (code == 1) {
                                    comDisposable.clear();
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                    }
                                    ToastUtil.toastShort(getApplicationContext(), "配置成功");
                                    comDisposable.clear();
                                    if (type.substring(0, 3).equals("B00")) {
                                        Intent intent = new Intent(BindLockActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        return;
                                    }

//                                    if (type.equals("A002")) {
//                                        Bind bind = new Gson().fromJson(content, Bind.class);
//                                        getAuthId(bind.getPid());
//                                        return;
//                                    }
                                    getPrintNumber(content);
                                } else if (code == 2 && count > 30) {
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                        ToastUtil.toastShort(getApplicationContext(), "配置超时");
                                    }
                                    comDisposable.clear();
                                }
                            }

                            @Override
                            protected void onFailure(BaseResponse response) {
                                super.onFailure(response);
                                ToastUtil.toastShort(BindLockActivity.this, response.msg);
                                if (mProgressDialog != null) {
                                    mProgressDialog.dismiss();
                                }
                                processLog(response);
                                if (disposable != null) {
                                    comDisposable.clear();
                                }
                            }
                        }
                );
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        Observable.interval(0, 3, TimeUnit.SECONDS).subscribe(observer);
        comDisposable.add(disposable);
    }

    private void processLog(BaseResponse response) {
        Bind bind = new Gson().fromJson(response.getContent(this), Bind.class);
        if (response.code == -1006) {
            String number = bind.getPhoneNumber();
            if (!TextUtils.isEmpty(number)) {
                dialog.setNotice("该设备已被手机号为" + number + "的用户绑定");
                dialog.show();
                return;
            }

            String uid = bind.getUid();
            if (!TextUtils.isEmpty(uid)) {
                ToastUtil.toastLong(getApplicationContext(), "设备已被用户" + uid + "绑定");
                dialog.setNotice("设备已被用户" + uid + "绑定");
                dialog.show();
            }
        } else if (response.code == -1005) {
            dialog.setNotice("该设备未注册或非法！");
            dialog.show();
        }
    }

    @Override
    protected void requestData() {

    }

    private void getPrintNumber(String content) {
        if (!isNetworkAvailable()) {
            return;
        }

        final Bind bind = new Gson().fromJson(content, Bind.class);
        Map params = new HashMap<>();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", bind.getPid());
        RequestUtils.request(RequestUtils.ALL_FPRINT, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        if (new Gson().fromJson(content, FPrintList.class).getList().size() == 0) {
                            isEvaluate = bind.getIsEvaluate();
                            getAuthId(bind.getPid());
                        } else {
                            Intent intent = new Intent(BindLockActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        finish();
                    }
                }
        );
    }

    private void getAuthId(final String sdlId) {
        if (!isNetworkAvailable()) {
            return;
        }

        Map params = new HashMap<>();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", sdlId);
        RequestUtils.request(RequestUtils.USER_LIST, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        List<User> u = new Gson().fromJson(content, UserList.class).getList();
                        for (User user : u) {
                            if (!user.getPid().equals("00000000000000000000000000000000")) {
                                if (sdlId.substring(0, 3).equals("A00")) {
                                    Intent intent = new Intent(BindLockActivity.this, FPrintStepActivity.class);
                                    intent.putExtra("flag", 2);
                                    intent.putExtra("sdlId", sdlId);
                                    intent.putExtra("isFromBind", true);
                                    intent.putExtra("authId", user.getPid());
                                    if (isEvaluate == 0) {
                                        intent.putExtra("needEvaluate", true);
                                    }
                                    startActivity(intent);
                                }
//                                    return;
//                                } else {
//                                    Intent intent = new Intent(BindLockActivity.this, PasswordStepActivity.class);
//                                    intent.putExtra("flag", 4);
//                                    intent.putExtra("sdlId", sdlId);
//                                    intent.putExtra("authId", user.getPid());
//                                    intent.putExtra("isFirst", true);
//                                    startActivity(intent);
//                                    return;
//                                }
                            }
                        }
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (type != null && "A00".equals(type.substring(0, 3)) && !isAnimated && !"A003".equals(type.substring(0, 4))) {
            int width = DensityUtil.getScreenWidth(this);
            int height = DensityUtil.getScreenHeight(this);
            float offX = 484 * width / 750;
            float offY = 418 * height / 1334;
            animation = new TranslateAnimation(offX, 0, offY, 0);
            animation.setDuration(800);
            try {
                hand_iv.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hand_iv.setVisibility(View.VISIBLE);
                        hand_iv.startAnimation(animation);
                    }
                }, 300);
            } catch (Exception e) {

            }
            isAnimated = true;
        }
    }

    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {

        private IEsptouchTask mEsptouchTask;
        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
            Log.e("main", "---onPreExecute---1");
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage("正在连接WiFi...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                        if (disposable != null) {
                            comDisposable.clear();
                        }
                    }
                }
            });
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (__IEsptouchTask.DEBUG) {
                            Log.i("TAG", "progress dialog is canceled");
                        }
                        if (disposable != null) {
                            comDisposable.clear();
                        }
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                    }
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    "Waiting...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            mProgressDialog.show();
            Log.e("main", "---onPreExecute---2");
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            Log.e("main", "---doInBackground---" + params);
            int taskResultCount = -1;
            synchronized (mLock) {
                // !!!NOTICE
                String apSsid = params[0];
                String apBssid = params[1];
                String apPassword = params[2];
                String taskResultCountStr = params[3];
//                taskResultCount = Integer.parseInt(taskResultCountStr);
                taskResultCount = 1;
                Log.e("main",
                        "---apSsid---" + apSsid +
                                "---apBssid---" + apBssid +
                                "---apPassword---" + apPassword +
                                "---taskResultCountStr---" + taskResultCountStr);

                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, BindLockActivity.this);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onCancelled(List<IEsptouchResult> iEsptouchResults) {
            super.onCancelled(iEsptouchResults);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
//            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
//            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Confirm");
            IEsptouchResult firstResult = result.get(0);
            boolean isCancelled = firstResult.isCancelled();
            // check whether the task is cancelled and no results received
            Log.e("main", "---onPostExecute---" + isCancelled);
            if (!isCancelled) {
                int count = 0;
                // max results to be displayed, if it is more than maxDisplayCount,
                // just show the count of redundant ones
                final int maxDisplayCount = 5;
                // the task received some results including cancelled while
                // executing before receiving enough results
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    for (IEsptouchResult resultInList : result) {
                        sb.append("Esptouch success, bssid = "
                                + resultInList.getBssid()
                                + ",InetAddress = "
                                + resultInList.getInetAddress()
                                .getHostAddress() + "\n");
                        count++;
                        if (count >= maxDisplayCount) {
                            break;
                        }
                    }
                    if (count < result.size()) {
                        sb.append("\nthere's " + (result.size() - count) + " more result(s) without showing\n");
                    }
                    mProgressDialog.setMessage("配置WiFi成功，正在连接服务器...");
//                    mProgressDialog.dismiss();
                } else {
//                    mProgressDialog.setMessage("配置WiFi失败");
                    mProgressDialog.dismiss();
                    ToastUtil.toastShort(getApplicationContext(), "配置WiFi失败");
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            comDisposable.clear();
        }
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    /**
     * 下发WIFI绑定指令的监听，并处理返回来的数据
     *
     * @return
     */
    private DisposableObserver getWifiObserver() {
        return new DisposableObserver() {

            @Override
            public void onNext(Object o) {
                Log.e("BindLockActivity", "---onNext---" + BluetoothUtil.isConnected);
                if (BluetoothUtil.recv_order != null) {
                    if (currentOrder == null || !BluetoothUtil.recv_order.equals(currentOrder)) {
                        currentOrder = BluetoothUtil.recv_order;
                        //处理蓝牙返回的数据
                        bleUploadWifi(currentOrder);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e("BindLockActivity", "---onError---");
            }

            @Override
            public void onComplete() {
                Log.e("BindLockActivity", "---onComplete---" + BluetoothUtil.isConnected);
                mCompositeDisposable.clear();
                BluetoothUtil.closeBluetooth();
                BluetoothUtil.clearInfo();
                Toast.makeText(BindLockActivity.this, "下发指令失败,请重新发送", Toast.LENGTH_SHORT).show();
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }
        };
    }
}
