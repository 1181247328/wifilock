package com.deelock.wifilock.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import com.deelock.wifilock.R;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.databinding.ActivityLoginBinding;
import com.deelock.wifilock.entity.Login;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.utils.DensityUtil;
import com.deelock.wifilock.utils.GsonUtil;
import com.deelock.wifilock.utils.InputUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.util.HashMap;

import cn.jpush.android.api.JPushInterface;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> {

    private String phone;
    private String pwd;
    private String did;

    @Override
    protected int initLayout() {
        return R.layout.activity_login;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        binding.setClick(this);
    }

    /**
     * 点击登录
     *
     * @param phone 手机号
     * @param pwd   密码
     */
    public void onSubmit(String phone, String pwd) {
        if (!InputUtil.checkLogin(this, phone, pwd)) {
            return;
        }
        this.phone = phone;
        this.pwd = pwd;
        binding.loginSubmit.setClickable(false);
        did = JPushInterface.getRegistrationID(getApplicationContext());
        login();
    }

    /**
     * 忘记密码
     */
    public void onForget() {
        openView(ForgetActivity.class, null);
    }

    /**
     * 注册新账号
     */
    public void onRegister() {
        openView(RegisterActivity.class, null);
    }

    private void login() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("phoneNumber", phone);
        params.put("pwd", pwd);
        if (!TextUtils.isEmpty(did)) {
            params.put("did", "10" + did);
        } else {
            SPUtil.saveData(this, "main_test", 1);
            SPUtil.saveData(this, "main_key", pwd + "AES256134654");
        }
        params.put("height", DensityUtil.getScreenHeight(this));
        params.put("width", DensityUtil.getScreenWidth(this));
        params.put("model", android.os.Build.MODEL);
        RequestUtils.requestUnLogged(RequestUtils.LOGIN, LoginActivity.this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onFinish() {
                        binding.loginSubmit.setClickable(true);
                    }

                    @Override
                    protected void onSuccess(int code, final String content) {
                        super.onSuccess(code, content);
                        ToastUtil.toastShort(LoginActivity.this, "登录成功");
                        Login login = GsonUtil.json2Bean(content, Login.class);
                        SPUtil.setLoginInfo(LoginActivity.this, login);
                        openView(MainActivity.class, null);
                        finish();
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            System.exit(0);
            System.gc();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
