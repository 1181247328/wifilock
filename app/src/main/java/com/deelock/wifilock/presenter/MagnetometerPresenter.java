package com.deelock.wifilock.presenter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.deelock.wifilock.entity.MagDetail;
import com.deelock.wifilock.entity.PushMessage;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.utils.GsonUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.deelock.wifilock.view.IMagnetometerView;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by forgive for on 2018\5\17 0017.
 */
public class MagnetometerPresenter {

    private Context context;
    private IMagnetometerView view;
    private MagDetail detail;
    private LocalBroadcastManager localBroadcastManager;
    private CompositeDisposable disposable;
    private String deviceId;
    private String lastState;  //设备上一次状态
    private int lastWifiState = -1;

    public MagnetometerPresenter(Context context, IMagnetometerView view, String pid) {
        this.context = context;
        this.view = view;
        deviceId = pid;
        disposable = new CompositeDisposable();
    }

    public MagDetail getDetail() {
        return detail;
    }

    /**
     * 首次进入设备界面，wifi设备采用轮询
     * 设备为红外时，只有采用广播监听
     */
    public void getDeviceInfo() {
        HashMap<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params.put("uid", SPUtil.getUid(context));
        params.put("pid", deviceId);
        RequestUtils.request(RequestUtils.MAGNETOMETER_DETAIL, context, params)
                .enqueue(new ResponseCallback<BaseResponse>((Activity) context) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        detail = GsonUtil.json2Bean(content, MagDetail.class);
                        view.initViewData(detail);
                        boolean isWifiMag = "B001".equals(deviceId.substring(0, 4));
                        if (!isWifiMag) {
                            registerDeviceState();   //红外
                        } else {
                            loopRequest();   //门磁
                        }
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        ToastUtil.toastShort(context, message);
                        ((Activity) context).finish();
                    }
                });
    }

    /**
     * 注册广播检测红外状态
     */
    private void registerDeviceState() {
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        IntentFilter intentFilter = new IntentFilter("com.deelock.wifilock.LOCALBROADCAST");
        localBroadcastManager.registerReceiver(receiver, intentFilter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PushMessage message = intent.getParcelableExtra("push");
            if (!"B00".equals(message.getDeviceId().substring(0, 3))) {
                return;
            }
            String devId = message.getDeviceId();
            String content = message.getData();
            if (devId.equals(deviceId)) {
                if ("B002".equals(deviceId.substring(0, 4))) {
                    view.onTouch();
                } else {
                    boolean contains = content.contains("打开");
                    if (lastState == null) {
                        lastState = contains ? "打开" : "关闭";
                        if (contains) {
                            view.onWifiOpen();
                        } else {
                            view.onWifiClose();
                        }
                    } else {
                        if (contains && lastState.equals("关闭")) {
                            lastState = "打开";
                            view.onWifiOpen();
                        }
                        if (!contains && lastState.equals("打开")) {
                            lastState = "关闭";
                            view.onWifiClose();
                        }
                    }
                }
            }
        }
    };

    /**
     * 轮询设备状态，仅针对wifi门磁
     */
    private void loopRequest() {
        Observable.interval(0, 5, TimeUnit.SECONDS).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable.add(d);
            }

            @Override
            public void onNext(Long aLong) {
                HashMap<String, String> params = new HashMap<>();
                params.put("timestamp", String.valueOf(TimeUtil.getTime()));
                params.put("uid", SPUtil.getUid(context));
                params.put("pid", deviceId);
                RequestUtils.request(RequestUtils.MAGNETOMETER_DETAIL, context, params)
                        .enqueue(new ResponseCallback<BaseResponse>((Activity) context) {
                            @Override
                            protected void onSuccess(int code, String content) {
                                super.onSuccess(code, content);
                                detail = GsonUtil.json2Bean(content, MagDetail.class);
                                view.initViewData(detail);
                                boolean isWifiMag = "B001".equals(deviceId.substring(0, 4));
                                if (isWifiMag) {
                                    if (lastWifiState == -1) {
                                        lastWifiState = detail.getState();
                                        if (detail.getState() == 1) {
                                            view.onWifiOpen();
                                        } else {
                                            view.onWifiClose();
                                        }
                                    } else {
                                        if (detail.getState() == 1 && lastWifiState == 0) {
                                            lastWifiState = 1;
                                            view.onWifiOpen();
                                        }
                                        if (detail.getState() == 0 && lastWifiState == 1) {
                                            lastWifiState = 0;
                                            view.onWifiClose();
                                        }
                                    }
                                }
                            }

                            @Override
                            protected void onFailure(int code, String message) {
                                super.onFailure(code, message);
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }


    public void onDestroy() {
        if (localBroadcastManager != null) {
            localBroadcastManager.unregisterReceiver(receiver);
        }
        if (disposable != null) {
            disposable.clear();
        }
    }
}
