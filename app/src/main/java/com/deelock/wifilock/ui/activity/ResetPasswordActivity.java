package com.deelock.wifilock.ui.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.text.TextUtils;

import com.deelock.wifilock.utils.StatusBarUtil;
import com.google.gson.Gson;
import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.ActivityResetPasswordBinding;
import com.deelock.wifilock.entity.Pid;
import com.deelock.wifilock.event.ResetPasswordEvent;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by binChuan on 2017\10\18 0018.
 */

public class ResetPasswordActivity extends BaseActivity {

    ActivityResetPasswordBinding binding;

    private boolean commitAble = true;

    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset_password);
        StatusBarUtil.StatusBarLightMode(this);
    }

    @Override
    protected void doBusiness() {

    }

    @Override
    protected void requestData() {

    }

    @Override
    protected void setEvent() {
        binding.setEvent(new ResetPasswordEvent() {
            @Override
            public void back() {
                finish();
            }

            @Override
            public void commit() {
                checkInput();
            }

            @Override
            public void forget() {
                startActivity(new Intent(ResetPasswordActivity.this, ForgetActivity.class));
            }
        });
    }

    private void checkInput() {
        if (!commitAble) {
            return;
        }

        String old = binding.oldPasswordEt.getText().toString();
        String ne = binding.newPasswordEt.getText().toString();
        String re = binding.rePasswordEt.getText().toString();

        if (TextUtils.isEmpty(old)) {
            ToastUtil.toastShort(getApplicationContext(), "请输入旧密码");
            return;
        }

        if (old.length() < 6) {
            ToastUtil.toastShort(getApplicationContext(), "请输入完整旧密码");
            return;
        }

        if (TextUtils.isEmpty(ne)) {
            ToastUtil.toastShort(getApplicationContext(), "请输入密码");
            return;
        }

        if (ne.length() < 6) {
            ToastUtil.toastShort(getApplicationContext(), "请输入完整密码");
            return;
        }

        if (!ne.equals(re)) {
            ToastUtil.toastShort(getApplicationContext(), "两次密码不一致");
            return;
        }

        if (old.equals(ne)) {
            ToastUtil.toastShort(getApplicationContext(), "新旧密码不能一致");
            return;
        }

        commit(old, ne);
    }

    private void commit(String old, String ne) {
        if (!isNetworkAvailable()) {
            return;
        }

        commitAble = false;
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("pid", SPUtil.getUid(this));
        params.put("pwdOld", old);
        params.put("pwdNew", ne);
        RequestUtils.request(RequestUtils.UPDATE_PASSWORD, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onFinish() {
                        commitAble = true;
                    }

                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        ToastUtil.toastShort(getApplicationContext(), "修改成功");
                        Pid pid = new Gson().fromJson(content, Pid.class);
                        SPUtil.setUid(ResetPasswordActivity.this, pid);
                        finish();
                    }
                }
        );
    }
}
