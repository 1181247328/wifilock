package com.deelock.wifilock.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.deelock.wifilock.bluetooth.BleActivity;
import com.deelock.wifilock.ui.dialog.DeleteDialog;
import com.deelock.wifilock.utils.BluetoothUtil;
import com.deelock.wifilock.utils.GsonUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.google.gson.Gson;
import com.deelock.wifilock.R;
import com.deelock.wifilock.adapter.UserFPrintAdapter;
import com.deelock.wifilock.databinding.ActivityAllFingerPrintBinding;
import com.deelock.wifilock.entity.FPrintList;
import com.deelock.wifilock.entity.UserFPrint;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.utils.InitRecyclerView;
import com.deelock.wifilock.utils.SPUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by binChuan on 2017\9\21 0021.
 */

public class AllFingerPrintActivity extends BaseActivity {

    ActivityAllFingerPrintBinding binding;
    private UserFPrintAdapter adapter;
    private List<UserFPrint> receivedData;
    private List<UserFPrint> data;
    private String sdlId;
    DeleteDialog dialog;
    int managerCount;
    private String type;
    private ProgressDialog mProgressDialog;
    private String fprintId;
    private String mac;
    private String deleteOrder;
    private String currentOrder;
    private CompositeDisposable mCompositeDisposable;
    private HandleOrder mHandle;
    private int currentPosition = 0;   //临时数据
    private boolean currentGiveUp = false; //临时数据

    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_all_finger_print);
    }

    @Override
    protected void doBusiness() {
        sdlId = getIntent().getStringExtra("sdlId");
        mac = getIntent().getStringExtra("mac");

        mCompositeDisposable = new CompositeDisposable();
        mHandle = new HandleOrder();
        mProgressDialog = new ProgressDialog(this);
        type = sdlId.substring(0, 4);
        if ("A003".equals(type)) {
            if (!SPUtil.getBooleanData(this, sdlId + "wifi")) {
                mProgressDialog.setMessage("正在连网请求指令...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK
                                && event.getAction() == KeyEvent.ACTION_DOWN
                                && mProgressDialog.isShowing()) {
                            Toast.makeText(AllFingerPrintActivity.this, "请等到当前操作完成", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }
                });
            }
        }
        receivedData = new ArrayList<>();
        data = new ArrayList<>();
        data.addAll(SPUtil.getList(this, "UserFPrint", UserFPrint.class));
        adapter = new UserFPrintAdapter(this, data, sdlId);
        binding.rv.setAdapter(adapter);
        InitRecyclerView.initLinearLayoutVERTICAL(AllFingerPrintActivity.this, binding.rv);

        dialog = new DeleteDialog(this, R.style.dialog);
        dialog.setNoticeTitle("是否放弃删除指纹？");
        dialog.setButton("放弃删除");
        dialog.setLeft("继续删除");
    }

    @Override
    protected void requestData() {
        if (!isNetworkAvailable()) {
            return;
        }
        Map params = new HashMap<>();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", sdlId);
        RequestUtils.request(RequestUtils.ALL_FPRINT, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        receivedData.clear();
                        receivedData.addAll(new Gson().fromJson(content, FPrintList.class).getList());
                        managerCount = 0;
                        for (UserFPrint u : receivedData) {
                            if (u.getType() == 1 && u.getState() != 0) {
                                managerCount++;
                            }
                        }
                        sortData();
                        adapter.notifyDataSetChanged();
                    }
                }
        );
    }

    @Override
    protected void setEvent() {
        binding.backIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        adapter.setEvent(new UserFPrintAdapter.Event() {
            @Override
            public void delete(final int position) {
                int state = data.get(position).getState();
                dialog.setNoticeTitle("是删除该指纹？");
                dialog.setButton("确定");
                dialog.setLeft("再想想");
                switch (state) {
                    case -2:
                    case 0:
                        dialog.setNoticeTitle("是否放弃删除指纹？");
                        dialog.setButton("放弃删除");
                        dialog.setLeft("继续删除");
                        break;
                }
                dialog.setEvent(new DeleteDialog.Event() {
                    @Override
                    public void delete() {
                        requestDelete(position, true);
                    }

                    @Override
                    public void cancel() {
                        if (data.get(position).getState() == 0 || data.get(position).getState() == 1) {
                            return;
                        }
                        requestDelete(position, false);
                    }
                });
                dialog.show();
            }
        });
    }

    private void requestDelete(final int p, boolean giveUp) {
        if ("A003".equals(type) && data.get(p).getState() == 1 && BleActivity.CanIUseBluetooth) {
            currentPosition = p;
            currentGiveUp = giveUp;
            requestOrder(data.get(p).getPid());
        } else if (SPUtil.getBooleanData(this, sdlId + "wifi")) {
            deleteByWifi(p, giveUp);
        } else {
            Toast.makeText(this, "请确保已与设备蓝牙连接或设备已联网", Toast.LENGTH_SHORT).show();
        }
    }

    //通过wifi删除
    private void deleteByWifi(int p, boolean giveUp) {
        if (data.get(p).getType() == 1) {
            if (managerCount < 2) {
                ToastUtil.toastShort(getApplicationContext(), "管理员指纹不能全部删除");
                dialog.dismiss();
                return;
            }
            managerCount--;
        }

        if (!isNetworkAvailable()) {
            return;
        }

        String url = RequestUtils.DELETE_PRINT;
        if (giveUp && data.get(p).getState() != 1) {
            url = RequestUtils.PRINT_DEL_CANCEL;
        }

        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(AllFingerPrintActivity.this));
        params.put("sdlId", sdlId);
        params.put("type", data.get(p).getType());
        params.put("pid", data.get(p).getPid());
        params.put("authId", data.get(p).getAuthId());
        RequestUtils.request(url, AllFingerPrintActivity.this, params).enqueue(
                new ResponseCallback<BaseResponse>(AllFingerPrintActivity.this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        requestData();
                    }

                    @Override
                    protected void onFinish() {
                        super.onFinish();
                        dialog.dismiss();
                    }
                }
        );
    }

    //检查蓝牙连接
    private DisposableObserver getConnObserver() {
        return new DisposableObserver() {
            @Override
            public void onNext(Object o) {
                if (BluetoothUtil.isConnected) {
                    dispose();
                    BluetoothUtil.writeCode(deleteOrder);
                    DisposableObserver orderObserver = getOrderObserver();
                    Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                            .take(30).observeOn(AndroidSchedulers.mainThread()).subscribe(orderObserver);
                    mCompositeDisposable.add(orderObserver);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                mCompositeDisposable.clear();
                mProgressDialog.dismiss();
                if (SPUtil.getBooleanData(AllFingerPrintActivity.this, sdlId + "wifi")) {
                    deleteByWifi(currentPosition, currentGiveUp);
                } else {
                    Toast.makeText(AllFingerPrintActivity.this, "蓝牙连接失败", Toast.LENGTH_SHORT).show();
                }
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
                        BluetoothUtil.requestResult(currentOrder, AllFingerPrintActivity.this, mHandle);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                mCompositeDisposable.clear();
                Toast.makeText(AllFingerPrintActivity.this, "蓝牙通讯失败", Toast.LENGTH_SHORT).show();
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
            if (code == 18) {
                sendDeleteResult();
            }

        }

        @Override
        public void fail(int code, String message, String content) {
            JSONObject jsonObject = JSONObject.parseObject(content);
            String cmd = jsonObject.getString("cmd");
            if (cmd != null) {
                BluetoothUtil.writeCode(cmd);
            }
            mProgressDialog.dismiss();
            ToastUtil.toastShort(AllFingerPrintActivity.this, message);
        }
    }

    /**
     * 发送删除结果
     */
    private void sendDeleteResult() {
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(this));
        map.put("devId", sdlId);
        map.put("fprintId", fprintId);
        map.put("authId", data.get(currentPosition).getAuthId());
        RequestUtils.request(RequestUtils.BLE_SEND_DELETE_FPRINT, this, map)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        mProgressDialog.dismiss();
                        Toast.makeText(AllFingerPrintActivity.this, "指纹删除成功", Toast.LENGTH_SHORT).show();
                        requestData();
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        mProgressDialog.dismiss();
                        Toast.makeText(AllFingerPrintActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 请求删除指纹指令
     */
    private void requestOrder(String pid) {
        mProgressDialog.show();
        fprintId = pid;
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(this));
        map.put("devId", sdlId);
        map.put("type", "D1D2");
        map.put("fprintId", pid);
        RequestUtils.request(RequestUtils.BLE_CMD, this, map).enqueue(new ResponseCallback<BaseResponse>(this) {
            @Override
            protected void onSuccess(int code, String content) {
                super.onSuccess(code, content);
                deleteOrder = GsonUtil.getValueByKey("cmd", content);
                boolean b = BluetoothUtil.openBluetooth();
                if (b) {
                    currentOrder = null;
                    BluetoothUtil.recv_order = null;
                    BluetoothUtil.connectByMac(mac);
                    mProgressDialog.setMessage("正在连接设备蓝牙...");
                    DisposableObserver connObserver = getConnObserver();
                    Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                            .take(20).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
                    mCompositeDisposable.add(connObserver);
                } else {
                    mProgressDialog.dismiss();
                    Toast.makeText(AllFingerPrintActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected void onFailure(int code, String message) {
                super.onFailure(code, message);
                mProgressDialog.dismiss();
                Toast.makeText(AllFingerPrintActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortData() {
        data.clear();
        if (receivedData.size() > 0) {
            int localCount = 0;
            int mainCount = 0;
            UserFPrint u;
            for (int i = 0; i < receivedData.size(); i++) {
                if (receivedData.get(i).getAuthId().equals("00000000000000000000000000000000")) {
                    if (localCount == 0) {
                        u = new UserFPrint(receivedData.get(i));
                        u.setUser(1);
                        data.add(0, u);
                        localCount++;
                    }
                    data.add(localCount, receivedData.get(i));
                    localCount++;
                } else if (receivedData.get(i).getAuthId().equals("11111111111111111111111111111111")) {
                    if (mainCount == 0) {
                        u = new UserFPrint(receivedData.get(i));
                        u.setUser(1);
                        data.add(localCount, u);
                        mainCount++;
                    }
                    data.add(localCount + mainCount, receivedData.get(i));
                    mainCount++;
                } else if (i == 0 || !receivedData.get(i).getAuthId().equals(receivedData.get(i - 1).getAuthId())) {
                    u = new UserFPrint(receivedData.get(i));
                    u.setUser(1);
                    data.add(u);
                    data.add(receivedData.get(i));
                } else {
                    data.add(receivedData.get(i));
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SPUtil.setList(AllFingerPrintActivity.this, "UserFPrint", data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }
}
