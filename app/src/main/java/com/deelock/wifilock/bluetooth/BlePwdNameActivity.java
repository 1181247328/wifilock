package com.deelock.wifilock.bluetooth;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.deelock.wifilock.R;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.databinding.ActivityBlePwdNameBinding;
import com.deelock.wifilock.entity.PasswordList;
import com.deelock.wifilock.entity.UserPassword;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.activity.MainActivity;
import com.deelock.wifilock.ui.dialog.PasswordDialog;
import com.deelock.wifilock.utils.BluetoothUtil;
import com.deelock.wifilock.utils.GsonUtil;
import com.deelock.wifilock.utils.InputUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.StatusBarUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BlePwdNameActivity extends BaseActivity<ActivityBlePwdNameBinding> {

    private boolean isShortPw;
    private boolean isFromBind;
    private String pwd;
    private String deviceId;
    private String type;
    private List<UserPassword> userPasswords;
    private String authId;
    private String mac;
    private String shareName;
    private long start_time, end_time;
    private SimpleDateFormat sf;
    private SimpleDateFormat sdf;
    private PasswordDialog dialog;
    private ProgressDialog mProgressDialog;
    private int send_type = 0;  //选择下发类型，1：短信下发，2：微信分享，3：直接下发
    private String pid;   //密码id
    private boolean isFollow;  //是否防尾随，true是，上传为1；false否，上传为0
    private String openName;   //密码名字
    private String currentOrder = null;
    private CompositeDisposable mCompositeDisposable;
    private HandleOrder mHandle;
    private boolean isWrongName = false;    //由于名字错误

    @Override
    protected int initLayout() {
        return R.layout.activity_ble_pwd_name;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        StatusBarUtil.StatusBarLightMode(this);
        binding.setClick(this);
        BluetoothUtil.recv_order = null;

        mCompositeDisposable = new CompositeDisposable();
        mHandle = new HandleOrder();

        sf = new SimpleDateFormat("yyyy年MM月dd日");
        sdf = new SimpleDateFormat("yyyy年MM月dd日HH点");
        dialog = new PasswordDialog(this, R.style.dialog);

        isShortPw = getIntent().getBooleanExtra("isShortPw", false);
        isFromBind = getIntent().getBooleanExtra("isFromBind", false);
        deviceId = getIntent().getStringExtra("deviceId");
        authId = getIntent().getStringExtra("authId");
        start_time = getIntent().getLongExtra("start_time", 0L);
        end_time = getIntent().getLongExtra("end_time", 0L);
        shareName = getIntent().getStringExtra("shareName");
        type = getIntent().getStringExtra("type");
        pwd = getIntent().getStringExtra("pwd");
        mac = getIntent().getStringExtra("mac");
        pid = getIntent().getStringExtra("pid");
        isFollow = getIntent().getBooleanExtra("isFollow", false);
        openName = getIntent().getStringExtra("openName");

        if (isFromBind) {
            binding.blePwdMessage.setVisibility(View.GONE);
            binding.blePwdWeixin.setVisibility(View.GONE);
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("正在连接门锁蓝牙，请稍候...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && mProgressDialog.isShowing()) {
                    Toast.makeText(BlePwdNameActivity.this, "请等待当前操作完成", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        binding.blePwdPw.setText(pwd);

        if (isShortPw) {
            binding.blePwdSelect.setVisibility(View.VISIBLE);
            binding.blePwdName.setHint("请输入手机号（选填）");
        } else {
            if (isFromBind && openName == null) {
                binding.blePwdName.setText("一家之主");
            }
            if (isFromBind && openName != null) {
                binding.blePwdName.setText(openName);
            }
            binding.blePwdMessage.setVisibility(View.GONE);
            binding.blePwdSelect.setVisibility(View.GONE);
        }

        binding.blePwdName.setFilters(new InputFilter[]{
                new InputFilter() {
                    public CharSequence filter(CharSequence source, int start,
                                               int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (!Character.isLetterOrDigit(source.charAt(i)) &&
                                    !Character.toString(source.charAt(i)).equals("_")) {
                                return "";
                            }
                        }
                        return null;
                    }
                }, new InputFilter.LengthFilter(16)
        });
        getAllPw();
    }

    /**
     * 选择手机中的联系人获取手机号码
     */
    public void selectPhone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                        20);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, 20);
            }
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setData(ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, 20);
        }
    }

    /**
     * 下发密码并通过短信分享
     */
    public void shareByMessage() {
        if (userPasswords == null) {
            Toast.makeText(this, "请等待网络配置完成", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean checkName = checkName(binding.blePwdName.getText().toString());
        if (checkName) {
            if (isWrongName) {
                sendResult();
                return;
            }
            boolean b = BluetoothUtil.openBluetooth();
            if (b) {
                currentOrder = null;
                BluetoothUtil.recv_order = null;
                mProgressDialog.show();
                BluetoothUtil.connectByMac(mac);
                send_type = 1;
                DisposableObserver connObserver = getConnObserver();
                Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                        .take(10).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
                mCompositeDisposable.add(connObserver);
            } else {
                Toast.makeText(this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 下发密码并通过微信分享
     */
    public void shareByWeixin() {
        if (userPasswords == null) {
            Toast.makeText(this, "请等待网络配置完成", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isWrongName) {
            if (openName == null) {
                sendResult();
            } else {
                updatePw();
            }
            return;
        }
        boolean b = BluetoothUtil.openBluetooth();
        if (b) {
            currentOrder = null;
            BluetoothUtil.recv_order = null;
            mProgressDialog.show();
            BluetoothUtil.connectByMac(mac);
            send_type = 2;
            DisposableObserver connObserver = getConnObserver();
            Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .take(10).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
            mCompositeDisposable.add(connObserver);
        } else {
            Toast.makeText(this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 下发密码
     */
    public void savePwd() {
        if (userPasswords == null) {
            Toast.makeText(this, "请等待网络配置完成", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isWrongName) {
            if (openName == null) {
                sendResult();
            } else {
                updatePw();
            }
            return;
        }
        boolean b = BluetoothUtil.openBluetooth();
        if (b) {
            currentOrder = null;
            BluetoothUtil.recv_order = null;
            mProgressDialog.setMessage("正在连接门锁蓝牙，请稍候...");
            mProgressDialog.show();
            BluetoothUtil.connectByMac(mac);
            send_type = 3;
            DisposableObserver connObserver = getConnObserver();
            Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .take(30).observeOn(AndroidSchedulers.mainThread()).subscribe(connObserver);
            mCompositeDisposable.add(connObserver);
        } else {
            Toast.makeText(this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
        }
    }

    //检查蓝牙连接
    private DisposableObserver getConnObserver() {
        return new DisposableObserver() {
            @Override
            public void onNext(Object o) {
                Log.e("main", "---进度条onNext---" + o.toString() + "---" + BluetoothUtil.isConnected);
                if (BluetoothUtil.isConnected) {
                    dispose();
                    getPwOrder();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e("main", "---onError---");
            }

            @Override
            public void onComplete() {
                Log.e("main", "---onComplete---");
                mCompositeDisposable.clear();
                BluetoothUtil.closeBluetooth();
                BluetoothUtil.clearInfo();
                mProgressDialog.dismiss();
                Toast.makeText(BlePwdNameActivity.this, "蓝牙连接失败", Toast.LENGTH_SHORT).show();
            }
        };
    }

    //检查是否接收到蓝牙指令
    private DisposableObserver getOrderObserver() {
        DisposableObserver orderObserver = new DisposableObserver() {
            @Override
            public void onNext(Object o) {
                if (BluetoothUtil.recv_order != null) {
                    if (currentOrder == null || !BluetoothUtil.recv_order.equals(currentOrder)) {
                        currentOrder = BluetoothUtil.recv_order;
                        BluetoothUtil.requestResult(currentOrder, BlePwdNameActivity.this, mHandle);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                mCompositeDisposable.clear();
                Toast.makeText(BlePwdNameActivity.this, "蓝牙通讯失败", Toast.LENGTH_SHORT).show();
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
            if (code != 1) {
                if (openName == null) {
                    sendResult();
                } else {
                    updatePw();
                }
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
            ToastUtil.toastShort(BlePwdNameActivity.this, message);
        }
    }

    /**
     * 返回
     */
    public void goBack() {
        finish();
    }

    /**
     * 获取蓝牙密码下发指令
     */
    private void getPwOrder() {
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(this));
        map.put("devId", deviceId);
        map.put("pwd", pwd);
        if (isFromBind) {
            map.put("type", "A1A1");
        } else {
            map.put("type", type);
            if (isShortPw) {
                map.put("timeBegin", String.valueOf(start_time));
                map.put("timeEnd", String.valueOf(end_time));
            }
        }
        RequestUtils.request(RequestUtils.BLE_CMD, this, map).
                enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        String cmd = GsonUtil.getValueByKey("cmd", content);
                        BluetoothUtil.writeCode(cmd);
                        mProgressDialog.setMessage("正在下发密码");
                        DisposableObserver orderObserver = getOrderObserver();
                        Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                                .take(20).observeOn(AndroidSchedulers.mainThread()).subscribe(orderObserver);
                        mCompositeDisposable.add(orderObserver);
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        mProgressDialog.dismiss();
                        ToastUtil.toastShort(BlePwdNameActivity.this, message);
                        BluetoothUtil.closeBluetooth();
                        BluetoothUtil.clearInfo();
                    }
                });
    }

    /**
     * 修改密码
     */
    private void updatePw() {
        mCompositeDisposable.clear();
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("sdlId", deviceId);
        map.put("uid", SPUtil.getUid(this));
        map.put("pid", pid);
        map.put("authId", authId);
        map.put("openName", openName);
        map.put("state", "1");
        map.put("isFollow", isFollow ? "1" : "0");
        RequestUtils.request(RequestUtils.BLE_UPDATE_PWD, this, map)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        mProgressDialog.dismiss();
                        dialog.setEvent(new PasswordDialog.Event() {
                            @Override
                            public void ensure() {
                                dialog.dismiss();
                                if (send_type != 2) {
                                    Intent intent = new Intent(BlePwdNameActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    shareWeChat();
                                }
                            }
                        });
                        dialog.setPassword(binding.blePwdPw.getText().toString());
                        dialog.show();
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        mProgressDialog.dismiss();
                        Toast.makeText(BlePwdNameActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 上报结果
     */
    private void sendResult() {
        mCompositeDisposable.clear();
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(this));
        map.put("sdlId", deviceId);
        map.put("authId", isFromBind ? "11111111111111111111111111111111" : authId);
        map.put("pwd", binding.blePwdPw.getText().toString());
        map.put("masterPhone", SPUtil.getPhoneNumber(this));
        String url;
        if ("A1A3".equals(type) || "A1A4".equals(type)) {
            //时段密码
            String phone = binding.blePwdName.getText().toString();
            map.put("openName", shareName);
            boolean canUseWifi = SPUtil.getBooleanData(this, deviceId + "wifi");

            if (!canUseWifi) {
                url = RequestUtils.BLE_PWD_ADD_T;
            } else {
                if (BleActivity.CanIUseBluetooth) {
                    url = RequestUtils.BLE_PWD_ADD_T;
                } else {
                    url = RequestUtils.ADD_TEMP_PASSWORD;
                }
            }
            if (!TextUtils.isEmpty(phone)) {
                map.put("phoneNumber", phone);
            }
            map.put("name", shareName);
            map.put("timeBegin", String.valueOf(start_time));
            map.put("timeEnd", String.valueOf(end_time));
            map.put("type", "A1A3".equals(type) ? String.valueOf(1) : String.valueOf(0));
        } else {
            url = RequestUtils.BLE_PWD_ADD_U;
            map.put("openName", binding.blePwdName.getText().toString());
            map.put("timeBegin", String.valueOf(TimeUtil.getTime()));
            map.put("type", String.valueOf(isFromBind ? 1 : 0));
        }

        RequestUtils.request(url, this, map).enqueue(new ResponseCallback<BaseResponse>(this) {
            @Override
            protected void onSuccess(int code, String content) {
                super.onSuccess(code, content);
                mProgressDialog.dismiss();
                if (isFromBind) {
                    Intent intent = new Intent(BlePwdNameActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    return;
                }
                dialog.setEvent(new PasswordDialog.Event() {
                    @Override
                    public void ensure() {
                        dialog.dismiss();
                        if (send_type != 2) {
                            finish();
                        } else {
                            shareWeChat();
                        }
                    }
                });
                dialog.setPassword(binding.blePwdPw.getText().toString());
                dialog.show();
            }

            @Override
            protected void onFailure(BaseResponse response) {
                super.onFailure(response);
                mProgressDialog.dismiss();
                if (response.code == -304) {
                    isWrongName = true;
                }
            }
        });
    }

    /**
     * 微信分享
     */
    private void shareWeChat() {
        Platform.ShareParams params = new Platform.ShareParams();
        params.setShareType(Platform.SHARE_TEXT);
        StringBuilder builder = new StringBuilder("[Deelock]");
        builder.append(SPUtil.getPhoneNumber(this))
                .replace(12, 16, "****").append("于")
                .append(sf.format(System.currentTimeMillis()));
        if ("A1A4".equals(type)) {
            builder.append("授权给您一次性开锁口令 #");
        } else {
            builder.append("授权给您开锁口令 #");
        }
        builder.append(pwd).append("#");
        if ("A1A3".equals(type)) {
            builder.append("，有效时间段为")
                    .append(sdf.format(start_time * 1000))
                    .append("至")
                    .append(sdf.format(end_time * 1000));
        }
        params.setText(builder.toString());
        params.setTitle("Deelock");
        Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
        wechat.share(params);
    }

    /**
     * 检查用户输入的密码昵称是否与当前账号的昵称冲突
     *
     * @param name 当前用户昵称
     */
    private boolean checkName(String name) {
        if (!isShortPw) {
            if (TextUtils.isEmpty(binding.blePwdName.getText().toString())) {
                ToastUtil.toastShort(this, "请输入昵称");
                return false;
            }
            if (userPasswords == null) {
                ToastUtil.toastShort(this, "当前网络连接不稳定，请重试");
                return false;
            }
            if (isFromBind) {
                return true;
            }
            int a = 0;
            for (UserPassword u : userPasswords) {
                a++;
                if (u.getAuthId().equals(authId) && u.getOpenName().equals(name)) {
                    ToastUtil.toastLong(this, "该昵称已存在，请重新输入！");
                    break;
                }
            }
            return userPasswords.size() == a;
        } else {
            return InputUtil.checkPhoneNumber(this, name);
        }
    }

    /**
     * 获取当前锁上的所有密码信息
     */
    private void getAllPw() {
        HashMap<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", deviceId);
        RequestUtils.request(RequestUtils.ALL_PASSWORD, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        PasswordList passwordList = GsonUtil.json2Bean(content, PasswordList.class);
                        userPasswords = passwordList.getUserPasswords();
                        if (!isShortPw && !isFromBind) {
                            binding.blePwdName.setText("密码" + (userPasswords.size() + 1));
                        }
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        Toast.makeText(BlePwdNameActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 20) {
            if (permissions[0].equals(Manifest.permission.READ_CONTACTS)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setData(ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(intent, requestCode);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        Uri contactData = data.getData();
        Cursor c = managedQuery(contactData, null, null, null, null);
        if (c.moveToFirst()) {
            String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
            String phoneNumber = null;
            if (hasPhone.equalsIgnoreCase("1")) {
                hasPhone = "true";
            } else {
                hasPhone = "false";
            }
            if (Boolean.parseBoolean(hasPhone)) {
                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
                                + contactId,
                        null,
                        null);
                while (phones.moveToNext()) {
                    phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
                phones.close();
                StringBuilder sb = new StringBuilder("");
                for (int i = 0; i < phoneNumber.length(); i++) {
                    char cc = phoneNumber.charAt(i);
                    if (cc > 47 && cc < 58) {
                        if (sb.length() < 1 && cc != 49) {
                            continue;
                        }
                        sb.append(cc);
                    }
                }
                if (requestCode == 20) {
                    binding.blePwdName.setText(sb.toString());
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isWrongName = false;
        mCompositeDisposable.dispose();
    }
}
