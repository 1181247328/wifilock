package com.deelock.wifilock.ui.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.deelock.wifilock.bluetooth.BleActivity;
import com.deelock.wifilock.bluetooth.BleAddPwdActivity;
import com.deelock.wifilock.constants.Constants;
import com.deelock.wifilock.entity.PasswordList;
import com.deelock.wifilock.entity.TempPassword;
import com.deelock.wifilock.entity.UserPassword;
import com.deelock.wifilock.utils.BluetoothUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.google.gson.Gson;
import com.deelock.wifilock.R;
import com.deelock.wifilock.adapter.AddByUserAdapter;
import com.deelock.wifilock.databinding.ActivityAddPasswordBinding;
import com.deelock.wifilock.entity.User;
import com.deelock.wifilock.entity.UserList;
import com.deelock.wifilock.event.AddPasswordEvent;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.utils.InitRecyclerView;
import com.deelock.wifilock.utils.SPUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by binChuan on 2017\9\25 0025.
 */

public class AddPasswordActivity extends BaseActivity {

    ActivityAddPasswordBinding binding;

    private AddByUserAdapter adapter;
    private List<User> data;
    private String sdlId;
    private boolean isUserPasswordFull;
    private boolean isTempPasswordFull;
    private String mac;

    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_password);
    }

    /**
     * 初始化数据
     */
    @Override
    protected void doBusiness() {
        mac = getIntent().getStringExtra("mac");
        if (SPUtil.getGesture(this) == null) {
            startActivityForResult(new Intent(this, VerifyPasswordActivity.class), 0);
        } else if (!Constants.isVerifiedGesture) {
            startActivityForResult(new Intent(this, GestureActivity.class), 0);
        }
        sdlId = getIntent().getStringExtra("sdlId");
        data = new ArrayList<>();
        adapter = new AddByUserAdapter(AddPasswordActivity.this, data, 4, sdlId);
        binding.rv.setAdapter(adapter);
        InitRecyclerView.initLinearLayoutVERTICAL(AddPasswordActivity.this, binding.rv);
        binding.rv.setNestedScrollingEnabled(false);
        if ("A003".equals(sdlId.substring(0, 4)) && !SPUtil.getBooleanData(this, sdlId + "wifi")) {
            binding.addTempRl.setVisibility(View.GONE);
        }
    }

    @Override
    protected void requestData() {

    }

    @Override
    protected void setEvent() {
        binding.setEvent(new AddPasswordEvent() {
            @Override
            public void back() {
                finish();
            }

            @Override
            public void addNew() {
                Bundle b = new Bundle();
                b.putInt("flag", 3);
                b.putString("sdlId", sdlId);
                b.putString("mac", mac);
                b.putString("type", "A1A2");
                openActivityForMoment(PasswordStepActivity.class, b);
            }

            @Override
            public void retract() {
                if (data != null && data.size() > 0) {
                    binding.addCurrentEv.retract();
                }
            }

            @Override
            public void addTemp() {
                if (isTempPasswordFull) {
                    ToastUtil.toastShort(getApplicationContext(), "密码个数已达到上限");
                    return;
                }
                Bundle b = new Bundle();
                b.putString("mac", mac);
                b.putInt("flag", 5);
                b.putString("type", "A1A4");
                b.putString("sdlId", sdlId);
                openActivityForMoment(PasswordStepActivity.class, b);
            }
        });

        adapter.setEvent(new AddByUserAdapter.Event() {
            @Override
            public void add(int p) {
                if (isUserPasswordFull) {
                    ToastUtil.toastShort(getApplicationContext(), "密码个数已达到上限");
                    return;
                }
                if ("A003".equals(sdlId.substring(0, 4))) {
                    boolean isBindWifi = SPUtil.getBooleanData(AddPasswordActivity.this, sdlId + "wifi");
                    boolean isRemote = SPUtil.getBooleanData(AddPasswordActivity.this, sdlId + "remote");

                    if (!isBindWifi || !isRemote) {
                        Bundle b = new Bundle();
                        b.putString("authId", data.get(p).getPid());
                        b.putString("deviceId", sdlId);
                        b.putString("mac", mac);
                        b.putString("type", "A1A2");
                        openActivityForMoment(BleAddPwdActivity.class, b);
                    } else {
                        if(BleActivity.CanIUseBluetooth){
                            Bundle b = new Bundle();
                            b.putString("authId", data.get(p).getPid());
                            b.putString("deviceId", sdlId);
                            b.putString("mac", mac);
                            b.putString("type", "A1A2");
                            openActivityForMoment(BleAddPwdActivity.class, b);
                        }else {
                            Bundle b = new Bundle();
                            b.putInt("flag", 4);
                            b.putString("sdlId", sdlId);
                            b.putString("authId", data.get(p).getPid());
                            openActivityForMoment(PasswordStepActivity.class, b);
                        }
                    }
                } else {
                    Bundle b = new Bundle();
                    b.putInt("flag", 4);
                    b.putString("sdlId", sdlId);
                    b.putString("authId", data.get(p).getPid());
                    openActivityForMoment(PasswordStepActivity.class, b);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }

    private void getData() {
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
                        if (data.get(0).getPid().equals("00000000000000000000000000000000")) {
                            data.remove(0);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
        );

        Map params1 = new HashMap<>();
        params1.put("timestamp", TimeUtil.getTime());
        params1.put("uid", SPUtil.getUid(this));
        params1.put("sdlId", sdlId);
        RequestUtils.request(RequestUtils.ALL_PASSWORD, this, params1).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        PasswordList list = new Gson().fromJson(content, PasswordList.class);
                        List<TempPassword> tp = list.getTempPasswords();
                        List<UserPassword> up = list.getUserPasswords();
                        if (tp.size() > 29) {
                            isTempPasswordFull = true;
                        }
                        if (up.size() > 29) {
                            isUserPasswordFull = true;
                        }
                        clickAble = true;
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!Constants.isVerifiedGesture) {
            finish();
        }
    }
}
