package com.deelock.wifilock.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.deelock.wifilock.R;
import com.deelock.wifilock.bluetooth.BleActivity;
import com.deelock.wifilock.bluetooth.BleAddPwdActivity;
import com.deelock.wifilock.entity.User;
import com.deelock.wifilock.entity.UserFPrint;
import com.deelock.wifilock.entity.UserList;
import com.deelock.wifilock.entity.UserPassword;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.presenter.PasswordDetailPresenter;
import com.deelock.wifilock.ui.dialog.BleNetDialog;
import com.deelock.wifilock.ui.dialog.DeleteDialog;
import com.deelock.wifilock.ui.dialog.MoveDialog;
import com.deelock.wifilock.ui.dialog.NickNameDialog;
import com.deelock.wifilock.utils.BluetoothUtil;
import com.deelock.wifilock.utils.GsonUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.deelock.wifilock.view.IPasswordDetailView;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by binChuan on 2017\9\25 0025.
 */

public class PasswordDetailActivity extends BaseActivity implements IPasswordDetailView {

    //widget
    private TextView title_tv;
    private TextView type_tv;
    private Button delete_btn;
    private RelativeLayout move_rl;
    private ImageButton back_ib;
    private RelativeLayout nick_name_rl;
    private TextView nick_name_tv;
    private TextView id_tv;
    private TextView time_tv;
    private TextView state_tv;
    private TextView move_to;
    private RelativeLayout security_rl;
    private TextView security_state_tv;
    private boolean isSecurityHelp = false;
    private ProgressDialog mProgressDialog;

    private PasswordDetailPresenter presenter;
    private int flag;
    private SimpleDateFormat sf;
    private String sdlId;
    UserPassword password;
    UserFPrint print;
    String name, pid, authId;
    int type;
    int state;
    long timeBegin;
    MoveDialog moveDialog;
    private List<User> data;
    private boolean isOpen;

    private BleNetDialog bleNetDialog;

    private RelativeLayout stateRl;

    private List<UserPassword> userPasswords;
    private DeleteDialog dialog;
    private String currentOrder;

    private CompositeDisposable mCompositeDisposable;
    private HandleOrder mHandle;

    @Override
    protected void bindActivity() {
        setContentView(R.layout.activity_password_detail);
    }

    @Override
    protected void findView() {
        title_tv = f(R.id.title_tv);
        type_tv = f(R.id.type_tv);
        delete_btn = f(R.id.delete_btn);
        move_rl = f(R.id.move_rl);
        back_ib = f(R.id.back_ib);
        nick_name_rl = f(R.id.nick_name_rl);
        nick_name_tv = f(R.id.nick_name_tv);
        id_tv = f(R.id.id_tv);
        time_tv = f(R.id.time_tv);
        state_tv = f(R.id.state_tv);
        security_rl = f(R.id.security_rl);
        security_state_tv = f(R.id.security_state_tv);
        move_to = f(R.id.move_to);
        stateRl = f(R.id.state_rl);

        bleNetDialog = new BleNetDialog(this, R.style.dialog);
    }

    /**
     * flag : 1 指纹 2 密码
     */
    @SuppressLint("SimpleDateFormat")
    @Override
    protected void doBusiness() {

        mCompositeDisposable = new CompositeDisposable();
        mHandle = new HandleOrder();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("正在连网请求指令...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && mProgressDialog.isShowing()) {
                    Toast.makeText(PasswordDetailActivity.this, "请等待当前操作完成", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        presenter = new PasswordDetailPresenter(this, this);
        flag = getIntent().getIntExtra("flag", 0);
        sdlId = getIntent().getStringExtra("sdlId");
        sf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        isOpen = getIntent().getBooleanExtra("isOpen", false);
        if (flag == 1) {
            print = getIntent().getParcelableExtra("UserFPrint");
            if (print.getIsSecurityHelp() == 1) {
                security_state_tv.setText("开启");
                isSecurityHelp = true;
            }
            name = print.getName();
            pid = print.getPid();
            authId = print.getAuthId();
            type = print.getType();
            state = print.getState();
            timeBegin = print.getTimeCreate();
            type_tv.setText("指纹ID");
            move_to.setText("指纹移动到");
            if (pid.equals("0100")) {
                delete_btn.setVisibility(View.GONE);
            } else {
                delete_btn.setVisibility(View.VISIBLE);
            }
        } else {
            password = getIntent().getParcelableExtra("UserPassword");
            if (password.getIsSecurityHelp() == 1) {
                security_state_tv.setText("开启");
                isSecurityHelp = true;
            }
            userPasswords = getIntent().getParcelableArrayListExtra("UserPasswords");
            name = password.getOpenName();
            pid = password.getPid();
            authId = password.getAuthId();
            type = password.getType();
            state = password.getState();
            timeBegin = password.getTimeBegin();
            type_tv.setText("密码ID");
            move_to.setText("密码移动到");
        }
        move_rl.setVisibility(View.VISIBLE);
        moveDialog = new MoveDialog(PasswordDetailActivity.this);

        data = new ArrayList<>();

        setUI();

        title_tv.setText(name);
        nick_name_tv.setText(name);
        id_tv.setText(pid);
        time_tv.setText(sf.format(timeBegin * 1000l));

        dialog = new DeleteDialog(this, R.style.dialog);
        dialog.setNoticeTitle("您确定要删除该密码?");

        if (flag == 2 && type == 1) {
            delete_btn.setText("修改密码");
            move_rl.setVisibility(View.GONE);
        }

        if(state!=1){
            //未正常使用
            stateRl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bleNetDialog.show();
                    bleNetDialog.setState(3);
                }
            });
        }
    }

    @Override
    protected void requestData() {
        if (!isNetworkAvailable()) {
            return;
        }

        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", sdlId);
        RequestUtils.request(RequestUtils.USER_LIST, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        data.clear();
                        data.addAll(new Gson().fromJson(content, UserList.class).getList());
                        Collections.sort(data);
                        for (User u : data) {
                            if (u.getPid().equals(authId)) {
                                data.remove(u);
                                break;
                            }
                        }
                        moveDialog.setData(data);
                    }
                }
        );
    }

    private void deleteByBle() {
        if (flag == 2) {
            if (type == 1) {
                //修改管理员密码,只能蓝牙修改
                Intent intent = new Intent(this, BleAddPwdActivity.class);
                intent.putExtra("deviceId", sdlId);
                intent.putExtra("authId", "11111111111111111111111111111111");
                intent.putExtra("isFromBind", true);
                intent.putExtra("isUpdate", true);
                intent.putExtra("pid", pid);
                intent.putExtra("openName", name);
                intent.putExtra("isFollow", isSecurityHelp);
                intent.putExtra("mac", SPUtil.getStringData(this, sdlId + "mac"));
                startActivity(intent);
                return;
            }
        }

        dialog.setLeft("再想想");
        dialog.setNoticeTitle(flag == 1 ? "是否删除该指纹" : "是否删除该密码");
        dialog.setButton("删除");
        dialog.setEvent(new DeleteDialog.Event() {
            @Override
            public void delete() {
                dialog.dismiss();
                boolean b = BluetoothUtil.openBluetooth();
                if (b) {
                    currentOrder = null;
                    BluetoothUtil.recv_order = null;
                    mProgressDialog.setMessage("正在连接门锁蓝牙...");
                    mProgressDialog.show();
                    String mac = SPUtil.getStringData(PasswordDetailActivity.this, sdlId + "mac");
                    BluetoothUtil.connectByMac(mac);
                    DisposableObserver connObserver = getConnObserver();
                    Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                            .take(5).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
                    mCompositeDisposable.add(connObserver);
                } else {
                    Toast.makeText(PasswordDetailActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void cancel() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //检查蓝牙连接
    private DisposableObserver getConnObserver() {
        return new DisposableObserver() {
            @Override
            public void onNext(Object o) {
                if (BluetoothUtil.isConnected) {
                    dispose();
                    requestDeleteOrder();
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
                if (SPUtil.getBooleanData(PasswordDetailActivity.this, sdlId + "wifi")) {
                    deleteByWifi();
                } else {
                    Toast.makeText(PasswordDetailActivity.this, "蓝牙连接失败", Toast.LENGTH_SHORT).show();
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
                        BluetoothUtil.requestResult(currentOrder, PasswordDetailActivity.this, mHandle);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                mCompositeDisposable.clear();
                Toast.makeText(PasswordDetailActivity.this, "蓝牙通讯失败", Toast.LENGTH_SHORT).show();
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
            if (code == 18 || code == 20) {
                mProgressDialog.dismiss();
                mCompositeDisposable.dispose();
                sendResult();
            }
        }

        @Override
        public void fail(int code, String message, String content) {
            mCompositeDisposable.clear();
            mProgressDialog.dismiss();
            ToastUtil.toastShort(PasswordDetailActivity.this, message);
            JSONObject jsonObject = JSONObject.parseObject(content);
            String cmd = jsonObject.getString("cmd");
            if (cmd != null) {
                BluetoothUtil.writeCode(cmd);
            }
        }
    }

    //上报删除结果
    private void sendResult() {
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(this));
        map.put("sdlId", sdlId);
        map.put("type", String.valueOf(type));
        String url;
        if (flag == 1) {
            map.put("fprintId", pid);
            url = RequestUtils.BLE_SEND_DELETE_FPRINT;
        } else {
            url = RequestUtils.BLE_SEND_N_PW;
            map.put("pwdId", pid);
        }
        map.put("devId", sdlId);
        map.put("pid", pid);
        map.put("authId", authId);
        RequestUtils.request(url, this, map)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        Toast.makeText(PasswordDetailActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        Toast.makeText(PasswordDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //请求删除指令
    private void requestDeleteOrder() {
        mProgressDialog.setMessage("正在请求删除指令...");
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(this));
        map.put("devId", sdlId);
        if (flag == 1) {
            map.put("fprintId", pid);
            map.put("type", "D1D2");
        } else {
            map.put("pwdId", pid);
            if (type == 0) {
                map.put("type", "C1C2");
            }
        }
        RequestUtils.request(RequestUtils.BLE_CMD, this, map).enqueue(new ResponseCallback<BaseResponse>(this) {
            @Override
            protected void onSuccess(int code, String content) {
                super.onSuccess(code, content);
                mProgressDialog.setMessage("正在下发指令...");
                String cmd = GsonUtil.getValueByKey("cmd", content);
                BluetoothUtil.writeCode(cmd);
                DisposableObserver orderObserver = getOrderObserver();
                Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                        .take(20).observeOn(AndroidSchedulers.mainThread()).subscribe(orderObserver);
                mCompositeDisposable.add(orderObserver);
            }

            @Override
            protected void onFailure(int code, String message) {
                super.onFailure(code, message);
                mCompositeDisposable.dispose();
                Toast.makeText(PasswordDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //普通删除密码
    private void deleteByWifi() {
        String url = null;
        String leftUrl = null;
        if (flag == 2) {
            url = RequestUtils.DELETE_PASSWORD;
            dialog.setLeft("再想想");
            dialog.setNoticeTitle("是否要删除该密码?");
            dialog.setButton("删除");
            switch (state) {
                case 2:
                    dialog.setNoticeTitle("是否放弃添加密码？");
                    dialog.setButton("放弃添加");
                    url = RequestUtils.DELETE_PASSWORD;
                    break;
                case 0:
                    dialog.setNoticeTitle("是否放弃删除密码？");
                    dialog.setButton("放弃删除");
                    url = RequestUtils.USER_DEL_CANCEL;
                    break;
                case -1:
                    dialog.setNoticeTitle("是否继续添加密码？");
                    dialog.setButton("放弃添加");
                    dialog.setLeft("继续添加");
                    url = RequestUtils.DELETE_PASSWORD;
                    break;
                case -2:
                    dialog.setNoticeTitle("是否重新删除密码？");
                    dialog.setButton("放弃删除");
                    dialog.setLeft("继续删除");
                    url = RequestUtils.USER_DEL_CANCEL;
                    leftUrl = RequestUtils.DELETE_PASSWORD;
                    break;
            }
            final String finalUrl = url;
            final String finalLeftUrl = leftUrl;
            dialog.setEvent(new DeleteDialog.Event() {
                @Override
                public void delete() {
                    deleteItem(finalUrl);
                }

                @Override
                public void cancel() {
                    if (state == -1) {
                        Intent intent = new Intent(PasswordDetailActivity.this, PasswordStepActivity.class);
                        intent.putExtra("flag", 4);
                        intent.putExtra("sdlId", sdlId);
                        intent.putExtra("authId", authId);
                        startActivity(intent);
                        return;
                    }
                    deleteItem(finalLeftUrl);
                }
            });
        } else {
            dialog.setNoticeTitle("是否删除该指纹？");
            dialog.setButton("确定");
            dialog.setLeft("再想想");
            url = RequestUtils.DELETE_PRINT;
            switch (state) {
                case -2:
                    leftUrl = RequestUtils.PRINT_DEL_CANCEL;
                case 0:
                    dialog.setNoticeTitle("是否放弃删除指纹？");
                    dialog.setButton("放弃删除");
                    dialog.setLeft("继续删除");
                    url = RequestUtils.PRINT_DEL_CANCEL;
                    break;
            }
            final String finalUrl = url;
            final String finalLeftUrl = leftUrl;
            dialog.setEvent(new DeleteDialog.Event() {
                @Override
                public void delete() {
                    deleteItem(finalUrl);
                }

                @Override
                public void cancel() {
                    deleteItem(finalLeftUrl);
                }
            });
        }
        dialog.show();
    }

    @Override
    protected void setEvent() {
        back_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        nick_name_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateName();
            }
        });

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //蓝牙锁未绑定wifi时通过蓝牙删除修改密码
                if ("A003".equals(sdlId.substring(0, 4)) && state == 1) {
                    boolean data = SPUtil.getBooleanData(PasswordDetailActivity.this, sdlId + "onlyWifi");
                    if(data){
                        if(BleActivity.CanIUseBluetooth){
                            deleteByBle();
                        }else {
                            deleteByWifi();
                        }
                    }else {
                        deleteByBle();
                    }
                } else {
                    deleteByWifi();
                }
            }
        });

        security_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen) {
                    if (flag == 1) {
                        ToastUtil.toastShort(getApplicationContext(), "只能拥有一枚安全指纹，请勿重复开启");
                    } else {
                        ToastUtil.toastShort(getApplicationContext(), "只能拥有一条安全密码，请勿重复开启");
                    }
                    return;
                }
                Intent intent = new Intent(PasswordDetailActivity.this, SecurityActivity.class);
                intent.putExtra("flag", flag);
                intent.putExtra("sdlId", sdlId);
                intent.putExtra("pid", pid);
                intent.putExtra("authId", authId);
                startActivityForResult(intent, 0);
            }
        });

        move_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSecurityHelp) {
                    ToastUtil.toastShort(getApplicationContext(), "已开启安全求助，无法移动");
                    return;
                }
                moveDialog.show();
            }
        });

        moveDialog.setEvent(new MoveDialog.Event() {
            @Override
            public void select(int p) {
                String url;
                Map params = new HashMap();
                params.put("timestamp", TimeUtil.getTime());
                params.put("uid", SPUtil.getUid(PasswordDetailActivity.this));
                params.put("sdlId", sdlId);
                params.put("pid", pid);
                params.put("authId", data.get(p).getPid());
                params.put("type", type);
                if (flag == 1) {
                    url = RequestUtils.UPDATE_USER_PRINT_NAME;
                } else {
                    url = RequestUtils.UPDATE_USER_PASSWORD_NAME;
                }
                RequestUtils.request(url, PasswordDetailActivity.this, params).enqueue(
                        new ResponseCallback<BaseResponse>(PasswordDetailActivity.this) {
                            @Override
                            protected void onSuccess(int code, String content) {
                                super.onSuccess(code, content);
                                ToastUtil.toastShort(getApplicationContext(), "移动成功");
                                finish();
                            }
                        }
                );
            }
        });
    }

    private void updateName() {
        final NickNameDialog dialog = new NickNameDialog(PasswordDetailActivity.this, R.style.dialog);
        dialog.setEvent(new NickNameDialog.Event() {
            @Override
            public void ensure(final String name) {
                if (flag == 2 && userPasswords != null) {
                    for (UserPassword u : userPasswords) {
                        if (u.getOpenName().equals(name)) {
                            ToastUtil.toastLong(PasswordDetailActivity.this, "该昵称已存在，请重新输入！");
                            return;
                        }
                    }
                }

                if (!isNetworkAvailable()) {
                    return;
                }

                String url;
                Map params = new HashMap();
                params.put("timestamp", TimeUtil.getTime());
                params.put("uid", SPUtil.getUid(PasswordDetailActivity.this));
                params.put("sdlId", sdlId);
                params.put("pid", pid);
                params.put("authId", authId);
                params.put("openName", name);
                params.put("type", type);
                if (flag == 1) {
                    url = RequestUtils.UPDATE_USER_PRINT_NAME;
                } else {
                    url = RequestUtils.UPDATE_USER_PASSWORD_NAME;
                }
                RequestUtils.request(url, PasswordDetailActivity.this, params).enqueue(
                        new ResponseCallback<BaseResponse>(PasswordDetailActivity.this) {
                            @Override
                            protected void onSuccess(int code, String content) {
                                super.onSuccess(code, content);
                                ToastUtil.toastShort(getApplicationContext(), "修改成功");
                                nick_name_tv.setText(name);
                            }

                            @Override
                            protected void onFinish() {
                                super.onFinish();
                            }
                        }
                );
            }
        });
        dialog.show();
    }

    private void deleteItem(final String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        if (!isNetworkAvailable()) {
            return;
        }

        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(PasswordDetailActivity.this));
        params.put("sdlId", sdlId);
        params.put("type", type);
        params.put("pid", pid);
        params.put("authId", authId);
        RequestUtils.request(url, PasswordDetailActivity.this, params).enqueue(
                new ResponseCallback<BaseResponse>(PasswordDetailActivity.this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        if (url.equals(RequestUtils.DELETE_PASSWORD) || url.equals(RequestUtils.DELETE_PRINT)) {
                            state = 0;
                        } else if (url.equals(RequestUtils.USER_DEL_CANCEL) || url.equals(RequestUtils.PRINT_DEL_CANCEL)) {
                            state = 1;
                        }
//                        setUI();
                    }

                    @Override
                    protected void onFinish() {
                        super.onFinish();
                        finish();
                    }
                }
        );
    }

    private void setUI() {
        switch (state) {
            case -2:
                state_tv.setText("删除失败");
                delete_btn.setText("继续删除");
                break;
            case -1:
                state_tv.setText("添加失败");
                delete_btn.setText("删除");
                break;
            case 0:
                state_tv.setText("正在删除");
                delete_btn.setText("放弃删除");
                break;
            case 1:
                state_tv.setText("正常使用");
                delete_btn.setText("删除");
                break;
            case 2:
                state_tv.setText("添加中");
                delete_btn.setText("放弃添加");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (data == null) {
                return;
            }
            int isSecurityHelp = data.getIntExtra("isSecurityHelp", 0);
            if (isSecurityHelp < 0) {
                return;
            }
            if (isSecurityHelp == 0) {
                security_state_tv.setText("关闭");
            } else {
                security_state_tv.setText("开启");
            }
        }
    }
}
