package com.deelock.wifilock.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.deelock.wifilock.bluetooth.BleActivity;
import com.deelock.wifilock.bluetooth.BleAddPwdActivity;
import com.deelock.wifilock.constants.CommonUtils;
import com.deelock.wifilock.entity.LockState;
import com.deelock.wifilock.entity.PasswordList;
import com.deelock.wifilock.entity.UserPassword;
import com.deelock.wifilock.ui.dialog.PasswordDialog;
import com.deelock.wifilock.utils.BluetoothUtil;
import com.deelock.wifilock.utils.KeyboardUtil;
import com.deelock.wifilock.utils.StatusBarUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.google.gson.Gson;
import com.deelock.wifilock.R;
import com.deelock.wifilock.adapter.ViewPagerFragmentAdapter;
import com.deelock.wifilock.databinding.ActivityStepBinding;
import com.deelock.wifilock.entity.Pid;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.overwrite.FixedSpeedScroller;
import com.deelock.wifilock.ui.fragment.PasswordSucceedFragment;
import com.deelock.wifilock.ui.fragment.InputInfoFragment;
import com.deelock.wifilock.ui.fragment.InputPasswordFragment;
import com.deelock.wifilock.ui.fragment.InputTimeFragment;
import com.deelock.wifilock.utils.SPUtil;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;

/**
 * Created by binChuan on 2017\9\27 0027.
 */

public class PasswordStepActivity extends AppActivity {

    ActivityStepBinding binding;
    ViewPagerFragmentAdapter adapter;
    List<Fragment> fragments;

    private List<UserPassword> userPasswords;

    FixedSpeedScroller scroller;
    int flag;
    String sdlId;
    String authId;
    String password;
    String nickname;
    String phoneNumber;
    long start, end;
    String name;
    int type;
    boolean isFirst;
    LockState lockState;
    private boolean share;
    private SimpleDateFormat sf;
    private SimpleDateFormat sdf;
    private String ble_type;
    private String ble_mac;

    private boolean needEvaluate;

    private PasswordDialog dialog;

    InputInfoFragment inputInfoFragment;
    InputPasswordFragment inputPasswordFragment;
    PasswordSucceedFragment passwordSucceedFragment;
    InputTimeFragment inputTimeFragment;
    private String mimaName;   //默认密码名字

    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_step);
        StatusBarUtil.StatusBarLightMode(this);
    }

    /**
     * 3 添加新密码  4 添加现有密码 5 添加临时密码
     */
    @Override
    protected void doBusiness() {
        sf = new SimpleDateFormat("yyyy年MM月dd日");
        sdf = new SimpleDateFormat("yyyy年MM月dd日HH点");
        dialog = new PasswordDialog(this, R.style.dialog);

        ble_type = getIntent().getStringExtra("type");
        ble_mac = getIntent().getStringExtra("mac");

        flag = getIntent().getIntExtra("flag", 3);
        sdlId = getIntent().getStringExtra("sdlId");
        authId = getIntent().getStringExtra("authId");
        isFirst = getIntent().getBooleanExtra("isFirst", false);
        needEvaluate = getIntent().getBooleanExtra("needEvaluate", false);
        fragments = new ArrayList<>();

        inputInfoFragment = new InputInfoFragment();
        inputPasswordFragment = new InputPasswordFragment();
        passwordSucceedFragment = new PasswordSucceedFragment(flag);
        inputTimeFragment = new InputTimeFragment();

        if (flag == 5) {
            fragments.add(inputTimeFragment);
            fragments.add(inputPasswordFragment);
            fragments.add(passwordSucceedFragment);
        } else {
            fragments.add(inputPasswordFragment);
            fragments.add(passwordSucceedFragment);
            if (flag == 3) {
                fragments.add(0, inputInfoFragment);
            }
        }

        adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragments);
        binding.vp.setAdapter(adapter);
        setViewPagerScrollSpeed();
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
        RequestUtils.request(RequestUtils.ALL_PASSWORD, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        PasswordList list = new Gson().fromJson(content, PasswordList.class);
                        userPasswords = list.getUserPasswords();
                        if (userPasswords.size() == 0) {
                            mimaName = "一家之主";
                            passwordSucceedFragment.hideShareView();
                        } else {
                            mimaName = "密码" + (userPasswords.size() + 1);
                        }
                    }
                }
        );
    }

    @Override
    protected void setEvent() {
        if (flag == 3) {
            inputInfoFragment.setEvent(new InputInfoFragment.Event() {
                @Override
                public void next(String name, String number) {
                    authUser(name, number);
                }
            });
        }

        inputPasswordFragment.setEvent(new InputPasswordFragment.Event() {
            @Override
            public void next(String password) {
                PasswordStepActivity.this.password = password;
                binding.vp.setCurrentItem(binding.vp.getCurrentItem() + 1);
                if (isFirst) {
                    passwordSucceedFragment.setName("一家之主");
                    passwordSucceedFragment.hideShareView();
                }
                if (mimaName != null) {
                    passwordSucceedFragment.setName(mimaName);
                }
                passwordSucceedFragment.setPassword(password);
            }
        });

        if (flag == 5) {
            inputTimeFragment.setEvent(new InputTimeFragment.Event() {
                @Override
                public void next(long start, long end, String nickname, boolean once) {
                    if ("A003".equals(sdlId.substring(0, 4))) {
                        boolean isBindWifi = SPUtil.getBooleanData(PasswordStepActivity.this, sdlId + "wifi");
                        boolean isRemote = SPUtil.getBooleanData(PasswordStepActivity.this, sdlId + "remote");
                        if (!isBindWifi || !isRemote) {
                            Intent intent = new Intent(PasswordStepActivity.this, BleAddPwdActivity.class);
                            intent.putExtra("mac", ble_mac);
                            intent.putExtra("isShortPw", true);
                            intent.putExtra("start", start);
                            intent.putExtra("end", end);
                            intent.putExtra("deviceId", sdlId);
                            intent.putExtra("shareName", nickname);
                            intent.putExtra("authId", authId);
                            intent.putExtra("type", once ? "A1A4" : "A1A3");
                            startActivity(intent);
                        } else {
                            if (BleActivity.CanIUseBluetooth) {
                                Intent intent = new Intent(PasswordStepActivity.this, BleAddPwdActivity.class);
                                intent.putExtra("mac", ble_mac);
                                intent.putExtra("isShortPw", true);
                                intent.putExtra("start", start);
                                intent.putExtra("end", end);
                                intent.putExtra("deviceId", sdlId);
                                intent.putExtra("shareName", nickname);
                                intent.putExtra("authId", authId);
                                intent.putExtra("type", once ? "A1A4" : "A1A3");
                                startActivity(intent);
                            } else {
                                PasswordStepActivity.this.start = start;
                                PasswordStepActivity.this.end = end;
                                PasswordStepActivity.this.nickname = nickname;
                                if (once) {
                                    type = 0;
                                    passwordSucceedFragment.setType(1, start, end);
                                } else {
                                    passwordSucceedFragment.setType(2, start, end);
                                    type = 1;
                                }
                                binding.vp.setCurrentItem(1);
                            }
                        }
                    } else {
                        PasswordStepActivity.this.start = start;
                        PasswordStepActivity.this.end = end;
                        PasswordStepActivity.this.nickname = nickname;
                        if (once) {
                            type = 0;
                            passwordSucceedFragment.setType(1, start, end);
                        } else {
                            passwordSucceedFragment.setType(2, start, end);
                            type = 1;
                        }
                        binding.vp.setCurrentItem(1);
                    }
                }
            });
        }
        passwordSucceedFragment.setEvent(new PasswordSucceedFragment.Event() {
            @Override
            public void back() {
                binding.vp.setCurrentItem(binding.vp.getCurrentItem() - 1);
            }

            @Override
            public void save(String name, boolean isFollow) {
                addPassword(name, isFollow);
            }

            @Override
            public void share(String name) {
                share = true;
                addPassword(name, false);
            }

            @Override
            public void saveAndMessage(String number) {
                addPasswordAndMessage(number);
            }
        });

        dialog.setEvent(new PasswordDialog.Event() {
            @Override
            public void ensure() {
                if (isFirst) {
                    if (!CommonUtils.isGestureSet(PasswordStepActivity.this)) {
                        Intent intent = new Intent(PasswordStepActivity.this, GestureActivity.class);
                        intent.putExtra("isSetGesture", true);
                        intent.putExtra("isFirst", true);
                        intent.putExtra("needEvaluate", needEvaluate);
                        startActivity(intent);
                    } /*else if (needEvaluate) {
                        Intent intent = new Intent(PasswordStepActivity.this, EvaluateActivity.class);
                        intent.putExtra("sdlId", sdlId);
                        startActivity(intent);
                    } */ else {
                        Intent intent = new Intent(PasswordStepActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
                dialog.dismiss();
                finish();
            }
        });
    }

    /**
     * 请求增加授权用户
     *
     * @param name
     * @param number
     */
    private void authUser(String name, String number) {
        if (!isNetworkAvailable()) {
            return;
        }
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", sdlId);
        params.put("type", 0);
        params.put("name", name);
        params.put("isPush", 1);
        if (!TextUtils.isEmpty(number)) {
            params.put("phoneNumber", number);
        }
        String url;
//        if ("A003".equals(sdlId.substring(0, 4))) {
//            url = RequestUtils.ADD_AUTH_USER_1;
//        } else {
//            url = RequestUtils.ADD_AUTH_USER;
//        }
        url = RequestUtils.ADD_AUTH_USER;
        RequestUtils.request(url, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        authId = new Gson().fromJson(content, Pid.class).getPid();
                        boolean isBindWifi = SPUtil.getBooleanData(PasswordStepActivity.this, sdlId + "wifi");
                        boolean isRemote = SPUtil.getBooleanData(PasswordStepActivity.this, sdlId + "remote");
                        if (ble_mac != null) {
                            if (!isBindWifi || !isRemote) {
                                Intent intent = new Intent(PasswordStepActivity.this, BleAddPwdActivity.class);
                                intent.putExtra("deviceId", sdlId);
                                intent.putExtra("authId", authId);
                                intent.putExtra("type", ble_type);
                                intent.putExtra("mac", ble_mac);
                                startActivity(intent);
                                finish();
                            } else {
                                if (BleActivity.CanIUseBluetooth) {
                                    Intent intent = new Intent(PasswordStepActivity.this, BleAddPwdActivity.class);
                                    intent.putExtra("deviceId", sdlId);
                                    intent.putExtra("authId", authId);
                                    intent.putExtra("type", ble_type);
                                    intent.putExtra("mac", ble_mac);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    binding.vp.setCurrentItem(binding.vp.getCurrentItem() + 1);
                                    hideInputKeyboard();
                                }
                            }
                        } else {
                            binding.vp.setCurrentItem(binding.vp.getCurrentItem() + 1);
                            hideInputKeyboard();
                        }
                    }
                });
    }

    private void addPasswordAndMessage(String number) {
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", sdlId);
        params.put("type", type);
        params.put("pwd", password);
        params.put("masterPhone", SPUtil.getPhoneNumber(this));
        final String url = RequestUtils.ADD_TEMP_PASSWORD;
        params.put("openName", nickname);
        if (!TextUtils.isEmpty(number)) {
            params.put("phoneNumber", number);
        }
        params.put("timeBegin", start);
        params.put("timeEnd", end);
        RequestUtils.request(url, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        dialog.setPassword(password);
                        dialog.show();
                    }

                    @Override
                    protected void onFailure(BaseResponse response) {
                        super.onFailure(response);
                        if (response.code == -1007) {
                            ToastUtil.toastShort(getApplicationContext(), "密码相似");
                        }
                    }
                });
    }

    /**
     * 添加密码
     *
     * @param name
     */
    private void addPassword(String name, boolean isFollow) {
        if (userPasswords != null) {
            for (UserPassword u : userPasswords) {
                if (u.getAuthId().equals(authId) && u.getOpenName().equals(name)) {
                    ToastUtil.toastLong(this, "该昵称已存在，请重新输入！");
                    return;
                }
            }
        }

        if (!isNetworkAvailable()) {
            return;
        }

        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", sdlId);
        params.put("type", type);
        params.put("pwd", password);
        params.put("authId", authId);
        params.put("masterPhone", SPUtil.getPhoneNumber(this));
        if (isFollow) {
            params.put("isFollow", 1);
        }
        final String url;
        if (flag == 5) {
            url = RequestUtils.ADD_TEMP_PASSWORD;
            params.put("openName", nickname);
            if (!TextUtils.isEmpty(name)) {
                params.put("phoneNumber", name);
            }
            params.put("timeBegin", start);
            params.put("timeEnd", end);
        } else {
            params.put("openName", name);
            url = RequestUtils.ADD_USER_PASSWORD;
            params.put("timeBegin", TimeUtil.getTime());
        }
        RequestUtils.request(url, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        dialog.setPassword(password);
                        dialog.show();
                        if (share) {
                            shareWeChat();
                        }
                    }

                    @Override
                    protected void onFailure(BaseResponse response) {
                        super.onFailure(response);
                        if (response.code == -1007) {
                            ToastUtil.toastShort(getApplicationContext(), "密码相似");
                        }
                    }
                });
    }

    private void shareWeChat() {
        Platform.ShareParams params = new Platform.ShareParams();
        params.setShareType(Platform.SHARE_TEXT);
        StringBuilder builder = new StringBuilder("[Deelock]");
        builder.append(SPUtil.getPhoneNumber(this))
                .replace(12, 16, "****").append("于")
                .append(sf.format(System.currentTimeMillis()));
        if (type == 1) {
            builder.append("授权给您一次性开锁口令 #");
        } else {
            builder.append("授权给您开锁口令 #");
        }
        builder.append(password).append("#");
        if (flag == 5) {
            builder.append("，有效时间段为")
                    .append(sdf.format(start * 1000))
                    .append("至")
                    .append(sdf.format(end * 1000));
        }
        params.setText(builder.toString());
        params.setTitle("Deelock");
        Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
        wechat.share(params);
        share = false;
    }

    private void setViewPagerScrollSpeed() {
        try {
            Field mScroller = null;
            scroller = new FixedSpeedScroller(this);
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mScroller.set(binding.vp, scroller);
        } catch (NoSuchFieldException e) {

        } catch (IllegalArgumentException e) {

        } catch (IllegalAccessException e) {

        }
    }

    private void hideInputKeyboard() {
        if (KeyboardUtil.isSoftInputShow(PasswordStepActivity.this)) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
//                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 20 || requestCode == 30) {
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
            String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
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
                    passwordSucceedFragment.setPhoneNumber(sb.toString());
                } else if (requestCode == 30) {
                    inputInfoFragment.setPhoneNumber(sb.toString());
                }
            }
        }
    }
}
