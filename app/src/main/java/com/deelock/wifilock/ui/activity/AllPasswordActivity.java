package com.deelock.wifilock.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.deelock.wifilock.bluetooth.BleActivity;
import com.deelock.wifilock.constants.Constants;
import com.deelock.wifilock.ui.dialog.DeleteDialog;
import com.deelock.wifilock.utils.BluetoothUtil;
import com.deelock.wifilock.utils.GsonUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.google.gson.Gson;
import com.deelock.wifilock.R;
import com.deelock.wifilock.adapter.TempPasswordAdapter;
import com.deelock.wifilock.adapter.UserPasswordAdapter;
import com.deelock.wifilock.databinding.ActivityAllPasswordBinding;
import com.deelock.wifilock.entity.PasswordList;
import com.deelock.wifilock.entity.TempPassword;
import com.deelock.wifilock.entity.UserPassword;
import com.deelock.wifilock.event.ManagePasswordEvent;
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
 * Created by binChuan on 2017\9\20 0020.
 */

public class AllPasswordActivity extends BaseActivity {

    ActivityAllPasswordBinding binding;

    private TempPasswordAdapter tempAdapter;
    private UserPasswordAdapter userAdapter;
    private List<TempPassword> tempPasswords;
    private List<UserPassword> userPasswords;
    private List<UserPassword> receivedUser;
    private String sdlId;
    private String type;
    private ProgressDialog mProgressDialog;
    private String pwId;  //密码id
    private String mac;
    private String deleteOrder;  //删除指令

    DeleteDialog dialog;
    int nextState;

    private String current_type;   //当前待处理密码类型
    private int current_position;   //当前待处理密码位置
    private boolean current_giveUp;
    private String currentOrder;   //当前蓝牙锁指令

    private CompositeDisposable mCompositeDisposable;
    private HandleOrder mHandle;


    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_all_password);
    }

    @Override
    protected void doBusiness() {
        mCompositeDisposable = new CompositeDisposable();
        mHandle = new HandleOrder();

        clickAble = false;
        sdlId = getIntent().getStringExtra("sdlId");
        mac = getIntent().getStringExtra("mac");
        type = sdlId.substring(0, 4);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("正在连网请求指令...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && mProgressDialog.isShowing()) {
                    Toast.makeText(AllPasswordActivity.this, "请等待当前操作完成", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        tempPasswords = new ArrayList<>();
        userPasswords = new ArrayList<>();
        receivedUser = new ArrayList<>();
        userPasswords.addAll(SPUtil.getList(this, "userPassword", UserPassword.class));
        tempPasswords.addAll(SPUtil.getList(this, "tempPassword", TempPassword.class));

        tempAdapter = new TempPasswordAdapter(tempPasswords);
        userAdapter = new UserPasswordAdapter(this, userPasswords, sdlId);

        binding.tempPasswordRv.setAdapter(tempAdapter);
        binding.userPasswordRv.setAdapter(userAdapter);
        InitRecyclerView.initLinearLayoutVERTICAL(this, binding.tempPasswordRv);
        InitRecyclerView.initLinearLayoutVERTICAL(this, binding.userPasswordRv);
        binding.tempPasswordRv.setNestedScrollingEnabled(false);
        binding.userPasswordRv.setNestedScrollingEnabled(false);

        dialog = new DeleteDialog(this, R.style.dialog);
    }

    @Override
    protected void requestData() {

    }

    /**
     * 获取当前用户所有密码
     */
    private void getData() {
        if (!isNetworkAvailable()) {
            return;
        }

        Map params = new HashMap<>();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", sdlId);
        RequestUtils.request(RequestUtils.ALL_PASSWORD, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        PasswordList list = new Gson().fromJson(content, PasswordList.class);
                        tempPasswords.clear();
                        receivedUser.clear();
                        tempPasswords.addAll(list.getTempPasswords());
                        receivedUser.addAll(list.getUserPasswords());
                        sortData();
                        tempAdapter.notifyDataSetChanged();
                        userAdapter.notifyDataSetChanged();
                        clickAble = true;
                    }
                }
        );
    }

    @Override
    protected void setEvent() {
        binding.setEvent(new ManagePasswordEvent() {
            @Override
            public void back() {
                finish();
            }

            @Override
            public void tempPassword() {
                if (tempPasswords.size() > 0) {
                    binding.tempPasswordEv.retract();
                }
            }

            @Override
            public void userPassword() {
                if (userPasswords.size() > 0) {
                    binding.userPasswordEv.retract();
                }
            }
        });

        tempAdapter.setEvent(new TempPasswordAdapter.Event() {
            @Override
            public void delete(final int p) {
                final int state = tempPasswords.get(p).getState();
                final long timeEnd = tempPasswords.get(p).getTimeEnd();
                dialog.setNoticeTitle("您确定要删除该密码?");
                dialog.setButton("确定");
                dialog.setLeft("再想想");
                switch (state) {
                    case 2:
                        dialog.setNoticeTitle("是否放弃添加密码？");
                        dialog.setButton("放弃添加");
                        dialog.setLeft("继续添加");
                        break;
                    case 0:
                        dialog.setNoticeTitle("是否放弃删除密码？");
                        dialog.setButton("放弃删除");
                        dialog.setLeft("继续删除");
                        break;
                    case -1:
                        dialog.setNoticeTitle("是否继续添加密码？");
                        dialog.setButton("放弃添加");
                        dialog.setLeft("继续添加");
                        break;
                    case -2:
                        dialog.setNoticeTitle("是否重新删除密码？");
                        dialog.setButton("放弃删除");
                        dialog.setLeft("继续删除");
                        break;
                }
                dialog.setEvent(new DeleteDialog.Event() {
                    @Override
                    public void delete() {
                        requestTempDelete(p, true);
                    }

                    @Override
                    public void cancel() {
                        requestTempDelete(p, false);
                    }
                });
                dialog.show();
            }
        });

        userAdapter.setEvent(new UserPasswordAdapter.Event() {
            @Override
            public void deleteUser(final int p) {
                int state = userPasswords.get(p).getState();
                dialog.setNoticeTitle("您确定要删除该密码?");
                dialog.setButton("确定");
                dialog.setLeft("再想想");
                switch (state) {
                    case 2:
                        dialog.setNoticeTitle("是否放弃添加密码？");
                        dialog.setButton("放弃添加");
                        dialog.setLeft("继续添加");
                        break;
                    case 0:
                        dialog.setNoticeTitle("是否放弃删除密码？");
                        dialog.setButton("放弃删除");
                        dialog.setLeft("继续删除");
                        break;
                    case -1:
                        dialog.setNoticeTitle("是否继续添加密码？");
                        dialog.setButton("放弃添加");
                        dialog.setLeft("继续添加");
                        break;
                    case -2:
                        dialog.setNoticeTitle("是否重新删除密码？");
                        dialog.setButton("放弃删除");
                        dialog.setLeft("继续删除");
                        break;
                }
                dialog.setEvent(new DeleteDialog.Event() {
                    @Override
                    public void delete() {
                        requestUserDelete(p, true);
                    }

                    @Override
                    public void cancel() {
                        requestUserDelete(p, false);
                    }
                });
                dialog.show();
            }
        });
    }

    private void sortData() {
        userPasswords.clear();
        if (receivedUser.size() > 0) {
            int localCount = 0;
            int mainCount = 0;
            UserPassword u;
            for (int i = 0; i < receivedUser.size(); i++) {
                if (receivedUser.get(i).getAuthId().equals("00000000000000000000000000000000")) {
                    if (localCount == 0) {
                        u = new UserPassword(receivedUser.get(i));
                        u.setUser(1);
                        userPasswords.add(0, u);
                        localCount++;
                    }
                    userPasswords.add(localCount, receivedUser.get(i));
                    localCount++;
                } else if (receivedUser.get(i).getAuthId().equals("11111111111111111111111111111111")) {
                    if (mainCount == 0) {
                        u = new UserPassword(receivedUser.get(i));
                        u.setUser(1);
                        userPasswords.add(localCount, u);
                        mainCount++;
                    }
                    userPasswords.add(localCount + mainCount, receivedUser.get(i));
                    mainCount++;
                } else if (i == 0 || !receivedUser.get(i).getAuthId().equals(receivedUser.get(i - 1).getAuthId())) {
                    u = new UserPassword(receivedUser.get(i));
                    u.setUser(1);
                    userPasswords.add(u);
                    userPasswords.add(receivedUser.get(i));
                } else {
                    userPasswords.add(receivedUser.get(i));
                }
            }
        }
    }

    private void requestTempDelete(final int p, final boolean giveUp) {
        String url = RequestUtils.DELETE_TEMP_PASSWORD;
        nextState = 1;
        if (!giveUp) {
            if (tempPasswords.get(p).getState() >= 0) {
                return;
            }
            nextState = 0;
        } else if (tempPasswords.get(p).getState() == -1) {

        } else {
            if (tempPasswords.get(p).getState() != 1) {
                url = RequestUtils.TEMP_DEL_CANCEL;
                nextState = 1;
            } else {
                nextState = 0;
            }
        }

        final int state = tempPasswords.get(p).getState();
        final String authId = tempPasswords.get(p).getAuthId();

        if (!clickAble) {
            return;
        }

        if (!isNetworkAvailable()) {
            return;
        }

        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(AllPasswordActivity.this));
        params.put("sdlId", sdlId);
        params.put("type", tempPasswords.get(p).getType());
        params.put("pid", tempPasswords.get(p).getPid());
        RequestUtils.request(url, AllPasswordActivity.this, params).enqueue(
                new ResponseCallback<BaseResponse>(AllPasswordActivity.this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        if (state == -1) {
                            if (!giveUp) {
                                Intent intent = new Intent(AllPasswordActivity.this, PasswordStepActivity.class);
                                intent.putExtra("flag", 5);
                                intent.putExtra("sdlId", sdlId);
                                intent.putExtra("authId", authId);
                                startActivity(intent);
                            } else {
//                                tempPasswords.remove(p);
//                                tempAdapter.notifyDataSetChanged();
//                                return;
                                tempPasswords.get(p).setState(nextState);
                                tempAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
        );
    }

    /**
     * 请求删除密码指令
     * current_type:  00一次性密码，10时段密码，01普通密码
     *
     * @param pid     密码id
     * @param pw_type 密码类型。（临时密码时，0表示一次性密码，1表示时段密码）（永久密码时，0普通密码，1管理员密码）
     *                来自蓝牙锁的永久密码中管理员密码不可删除
     * @param from    密码来自临时密码（0）还是永久密码（1）
     */
    private void requestOrder(String pid, int pw_type, int from) {
        boolean b = BluetoothUtil.openBluetooth();
        if (!b) {
            Toast.makeText(this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        currentOrder = null;
        BluetoothUtil.recv_order = null;
        mProgressDialog.show();
        BluetoothUtil.connectByMac(mac);
        pwId = pid;
        current_type = String.valueOf(pw_type) + String.valueOf(from);
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(this));
        map.put("devId", sdlId);
        map.put("pwdId", pid);
        if (from == 1) {
            map.put("type", "C1C2");
        } else {
            if (pw_type == 0) {
                map.put("type", "C1C4");
            } else {
                map.put("type", "C1C3");
            }
        }
        RequestUtils.request(RequestUtils.BLE_CMD, this, map).enqueue(new ResponseCallback<BaseResponse>(this) {
            @Override
            protected void onSuccess(int code, String content) {
                super.onSuccess(code, content);
                deleteOrder = GsonUtil.getValueByKey("cmd", content);
                mProgressDialog.setMessage("正在下发指令...");
                DisposableObserver connObserver = getConnObserver();
                Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                        .take(8).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
                mCompositeDisposable.add(connObserver);
            }

            @Override
            protected void onFailure(int code, String message) {
                super.onFailure(code, message);
                mProgressDialog.dismiss();
                Toast.makeText(AllPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
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
                            .take(20).observeOn(AndroidSchedulers.mainThread()).subscribe(orderObserver);
                    mCompositeDisposable.add(orderObserver);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                mCompositeDisposable.clear();
                BluetoothUtil.closeBluetooth();
                BluetoothUtil.clearInfo();
                mProgressDialog.dismiss();
                if (SPUtil.getBooleanData(AllPasswordActivity.this, sdlId + "wifi")) {
                    deleteUserPwByWifi(current_position, current_giveUp);
                } else {
                    Toast.makeText(AllPasswordActivity.this, "蓝牙连接失败", Toast.LENGTH_SHORT).show();
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
                        BluetoothUtil.requestResult(currentOrder, AllPasswordActivity.this, mHandle);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                mCompositeDisposable.clear();
                Toast.makeText(AllPasswordActivity.this, "蓝牙通讯失败", Toast.LENGTH_SHORT).show();
                BluetoothUtil.closeBluetooth();
                BluetoothUtil.clearInfo();
                mProgressDialog.dismiss();
                if (SPUtil.getBooleanData(AllPasswordActivity.this, sdlId + "wifi")) {
                    deleteUserPwByWifi(current_position, current_giveUp);
                } else {
                    Toast.makeText(AllPasswordActivity.this, "蓝牙通讯失败", Toast.LENGTH_SHORT).show();
                }
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
                mCompositeDisposable.clear();
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
            mCompositeDisposable.clear();
            mProgressDialog.dismiss();
            ToastUtil.toastShort(AllPasswordActivity.this, message);
        }
    }

    /**
     * 上报密码删除结果
     */
    private void sendDeleteResult() {
        mCompositeDisposable.clear();
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(this));
        map.put("devId", sdlId);
        map.put("pwdId", pwId);
        String url;
        if ("01".equals(current_type)) {
            //普通密码
            map.put("authId",userPasswords.get(current_position).getAuthId());
            url = RequestUtils.BLE_SEND_N_PW;
        } else {
            //临时密码
            url = RequestUtils.BLE_SEND_T_PW;
        }
        RequestUtils.request(url, this, map).enqueue(new ResponseCallback<BaseResponse>(this) {
            @Override
            protected void onSuccess(int code, String content) {
                super.onSuccess(code, content);
                mProgressDialog.dismiss();
                Toast.makeText(AllPasswordActivity.this, "密码删除成功", Toast.LENGTH_SHORT).show();
                if ("01".equals(current_type)) {
                    //普通密码
                    userPasswords.remove(current_position);
                    userAdapter.notifyDataSetChanged();
                } else {
                    //临时密码
                    tempPasswords.remove(current_position);
                    tempAdapter.notifyDataSetChanged();
                }
            }

            @Override
            protected void onFailure(int code, String message) {
                super.onFailure(code, message);
                mProgressDialog.dismiss();
                Toast.makeText(AllPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestUserDelete(final int p, final boolean giveUp) {

        if ("A003".equals(type) && userPasswords.get(p).getState() == 1) {
            boolean data = SPUtil.getBooleanData(AllPasswordActivity.this, sdlId + "onlyWifi");
            if (data) {
                if (BleActivity.CanIUseBluetooth && userPasswords.get(p).getState() == 1) {
                    current_giveUp = giveUp;
                    current_position = p;
                    requestOrder(userPasswords.get(p).getPid(), userPasswords.get(p).getType(), 1);
                    return;
                } else {
                    deleteUserPwByWifi(p, giveUp);
                    return;
                }
            } else {
                if (giveUp) {
                    current_giveUp = giveUp;
                    current_position = p;
                    requestOrder(userPasswords.get(p).getPid(), userPasswords.get(p).getType(), 1);
                    return;
                }
            }
        }
        deleteUserPwByWifi(p, giveUp);
    }

    private void deleteUserPwByWifi(final int p, final boolean giveUp) {
        if (!clickAble) {
            return;
        }

        if (!isNetworkAvailable()) {
            return;
        }

        String url = RequestUtils.DELETE_PASSWORD;
        nextState = 1;
        if (!giveUp) {
            if (userPasswords.get(p).getState() >= 0) {
                return;
            }
            nextState = 0;
        } else if (userPasswords.get(p).getState() == -1) {

        } else {
            if (userPasswords.get(p).getState() == 0) {
                url = RequestUtils.USER_DEL_CANCEL;
                nextState = 1;
            } else {
                nextState = 0;
            }
        }

        final int state = userPasswords.get(p).getState();
        final String authId = userPasswords.get(p).getAuthId();
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(AllPasswordActivity.this));
        params.put("sdlId", sdlId);
        params.put("type", userPasswords.get(p).getType());
        params.put("pid", userPasswords.get(p).getPid());
        params.put("authId", userPasswords.get(p).getAuthId());
        RequestUtils.request(url, AllPasswordActivity.this, params).enqueue(
                new ResponseCallback<BaseResponse>(AllPasswordActivity.this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);

                        if (!giveUp) {
                            Intent intent = new Intent(AllPasswordActivity.this, PasswordStepActivity.class);
                            intent.putExtra("flag", 4);
                            intent.putExtra("sdlId", sdlId);
                            intent.putExtra("authId", authId);
                            startActivity(intent);
                        } else {
                            userPasswords.get(p).setState(nextState);
                            userAdapter.notifyDataSetChanged();
//                            userPasswords.remove(p);
//                            if (userPasswords.get(p - 1).getUser() == 1) {
//                                userPasswords.remove(p - 1);
//                            }
//                            userAdapter.notifyDataSetChanged();
//                            return;
                        }
//                        userPasswords.get(p).setState(nextState);
//                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        Toast.makeText(AllPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        SPUtil.setList(AllPasswordActivity.this, "userPassword", userPasswords);
        SPUtil.setList(AllPasswordActivity.this, "tempPassword", tempPasswords);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }
}
