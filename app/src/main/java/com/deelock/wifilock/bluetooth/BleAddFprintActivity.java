package com.deelock.wifilock.bluetooth;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.deelock.wifilock.R;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.activity.BaseActivity;
import com.deelock.wifilock.utils.BluetoothUtil;
import com.deelock.wifilock.utils.GsonUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.StatusBarUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BleAddFprintActivity extends BaseActivity {

    private String authId;
    private String mac;
    private String deviceId;
    private String fPrintId;
    private ProgressDialog mProgressDialog;
    private String currentOrder;
    private CompositeDisposable mCompositeDisposable;
    private HandleOrder mHandle;

    @Override
    protected void bindActivity() {
        setContentView(R.layout.activity_ble_add_fprint);
    }

    @Override
    protected void doBusiness() {
        StatusBarUtil.StatusBarLightMode(this);

        mCompositeDisposable = new CompositeDisposable();
        mHandle = new HandleOrder();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("正在请求指令，请稍后...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && mProgressDialog.isShowing()) {
                    Toast.makeText(BleAddFprintActivity.this, "请等待当前操作完成", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        authId = getIntent().getStringExtra("authId");
        mac = getIntent().getStringExtra("mac");
        deviceId = getIntent().getStringExtra("deviceId");
    }

    @Override
    protected void setEvent() {
        findViewById(R.id.ble_add_fprint_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.ble_add_fprint_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = BluetoothUtil.openBluetooth();
                if (b) {
                    currentOrder = null;
                    BluetoothUtil.recv_order = null;
                    BluetoothUtil.connectByMac(mac);
                    mProgressDialog.show();
                    DisposableObserver connObserver = getConnObserver();
                    Observable.interval(0, 200, TimeUnit.MILLISECONDS)
                            .take(25).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
                    mCompositeDisposable.add(connObserver);
                } else {
                    Toast.makeText(BleAddFprintActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //检查蓝牙连接
    private DisposableObserver getConnObserver() {
        DisposableObserver connObserver = new DisposableObserver() {
            @Override
            public void onNext(Object o) {
                if (BluetoothUtil.isConnected) {
                    dispose();
                    requestAddFprintM();
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                mCompositeDisposable.clear();
                Toast.makeText(BleAddFprintActivity.this, "蓝牙连接失败", Toast.LENGTH_SHORT).show();
                BluetoothUtil.closeBluetooth();
                BluetoothUtil.clearInfo();
                mProgressDialog.dismiss();
            }
        };
        return connObserver;
    }

    //检查是否接收到蓝牙指令
    private DisposableObserver getOrderObserver() {
        DisposableObserver orderObserver = new DisposableObserver() {
            @Override
            public void onNext(Object o) {
                if (BluetoothUtil.recv_order != null) {
                    if (currentOrder == null || !BluetoothUtil.recv_order.equals(currentOrder)) {
                        currentOrder = BluetoothUtil.recv_order;
                        BluetoothUtil.requestResult(currentOrder, BleAddFprintActivity.this, mHandle);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                mCompositeDisposable.clear();
                Toast.makeText(BleAddFprintActivity.this, "蓝牙通讯失败", Toast.LENGTH_SHORT).show();
                BluetoothUtil.closeBluetooth();
                BluetoothUtil.clearInfo();
                mProgressDialog.dismiss();
            }
        };
        return orderObserver;
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
            if (code == 10) {
                mProgressDialog.dismiss();
                Intent intent = new Intent(BleAddFprintActivity.this, BleFprintNameActivity.class);
                intent.putExtra("mac", mac);
                intent.putExtra("authId", authId);
                intent.putExtra("deviceId", deviceId);

                startActivity(intent);
                finish();
            }
        }

        @Override
        public void fail(int code, String message, String content) {
            mCompositeDisposable.clear();
            mProgressDialog.dismiss();
            ToastUtil.toastShort(BleAddFprintActivity.this, message);
            JSONObject jsonObject = JSONObject.parseObject(content);
            String cmd = jsonObject.getString("cmd");
            if (cmd != null) {
                BluetoothUtil.writeCode(cmd);
            }
        }
    }

    /**
     * 请求添加指纹指令
     */
    private void requestAddFprintM() {
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(this));
        map.put("devId", deviceId);
        map.put("type", "B1B2");
        RequestUtils.request(RequestUtils.BLE_CMD, this, map).
                enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        String cmd = GsonUtil.getValueByKey("cmd", content);
                        BluetoothUtil.writeCode(cmd);
                        mProgressDialog.setMessage("正在下发指令，请根据语音提示录入新指纹");
                        DisposableObserver orderObserver = getOrderObserver();
                        Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                                .take(60).observeOn(AndroidSchedulers.mainThread()).subscribe(orderObserver);
                        mCompositeDisposable.add(orderObserver);
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        mProgressDialog.dismiss();
                        ToastUtil.toastShort(BleAddFprintActivity.this, message);
                        BluetoothUtil.closeBluetooth();
                        BluetoothUtil.clearInfo();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }
}
