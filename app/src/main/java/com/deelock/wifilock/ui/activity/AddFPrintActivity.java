package com.deelock.wifilock.ui.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.deelock.wifilock.bluetooth.BleAddFprintActivity;
import com.deelock.wifilock.constants.Constants;
import com.deelock.wifilock.entity.FPrintList;
import com.deelock.wifilock.entity.UserFPrint;
import com.deelock.wifilock.utils.BluetoothUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.google.gson.Gson;
import com.deelock.wifilock.R;
import com.deelock.wifilock.adapter.AddByUserAdapter;
import com.deelock.wifilock.databinding.ActivityAddPrintBinding;
import com.deelock.wifilock.entity.User;
import com.deelock.wifilock.entity.UserList;
import com.deelock.wifilock.event.AddPrintEvent;
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

public class AddFPrintActivity extends BaseActivity {

    ActivityAddPrintBinding binding;

    private AddByUserAdapter adapter;
    private List<User> data;
    private String sdlId;
    private String mac;

    int position;
    boolean isPrintFull;

    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_print);
    }

    @Override
    protected void doBusiness() {
        if (!Constants.isVerifiedGesture) {
            checkGesture();
        }

        clickAble = false;
        mac = getIntent().getStringExtra("mac");
        sdlId = getIntent().getStringExtra("sdlId");
        data = new ArrayList<>();
        adapter = new AddByUserAdapter(AddFPrintActivity.this, data, 2, sdlId);
        binding.rv.setAdapter(adapter);
        InitRecyclerView.initLinearLayoutVERTICAL(AddFPrintActivity.this, binding.rv);
    }

    /**
     * 获取用户列表
     */
    @Override
    protected void requestData() {

    }

    @Override
    protected void setEvent() {
        binding.setEvent(new AddPrintEvent() {
            @Override
            public void back() {
                finish();
            }

            @Override
            public void addNew() {
                if (!clickAble) {
                    return;
                }
                Bundle b = new Bundle();
                b.putInt("flag", 1);
                b.putString("sdlId", sdlId);
                b.putBoolean("isFull", isPrintFull);
                b.putString("mac", mac);
                openActivityForMoment(FPrintStepActivity.class, b);
            }

            @Override
            public void retract() {
                if (data.size() > 0) {
                    binding.addCurrentEv.retract();
                }
            }
        });

        adapter.setEvent(new AddByUserAdapter.Event() {
            @Override
            public void add(int p) {
                if (isPrintFull) {
                    ToastUtil.toastShort(getApplicationContext(), "指纹个数已达到上限");
                    return;
                }
                position = p;
                configure(p, 2);
            }
        });
    }

    /**
     * 检查是否已验证手势密码
     */
    private void checkGesture() {
        if (SPUtil.getGesture(this) == null) {
            startActivityForResult(new Intent(this, VerifyPasswordActivity.class), 0);
        } else {
            startActivityForResult(new Intent(this, GestureActivity.class), 0);
        }
    }

    /**
     * 进入配置界面
     *
     * @param p
     * @param flag 1添加到新用户  2添加到现有用户
     */
    private void configure(int p, int flag) {

        if (mac != null) {
            Bundle b = new Bundle();
            b.putString("authId", data.get(p).getPid());
            b.putString("mac", mac);
            b.putString("deviceId", sdlId);
            openActivityForMoment(BleAddFprintActivity.class, b);
        } else {
            Bundle b = new Bundle();
            b.putInt("flag", flag);
            b.putString("sdlId", sdlId);
            b.putString("authId", data.get(p).getPid());
            b.putString("name", data.get(p).getName());
            openActivityForMoment(FPrintStepActivity.class, b);
        }
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

        //获取用户列表
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
                        if (data.size() == 0) {
                            return;
                        }
                        Collections.sort(data);
                        if (data.get(0).getPid().equals("00000000000000000000000000000000")) {
                            data.remove(0);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
        );

        //获取所有指纹
        Map params1 = new HashMap<>();
        params1.put("timestamp", TimeUtil.getTime());
        params1.put("uid", SPUtil.getUid(this));
        params1.put("sdlId", sdlId);
        RequestUtils.request(RequestUtils.ALL_FPRINT, this, params1).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        List<UserFPrint> p = new Gson().fromJson(content, FPrintList.class).getList();
                        if (p.size() > 36) {
                            isPrintFull = true;
                        }
                    }

                    @Override
                    protected void onFinish() {
                        super.onFinish();
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
