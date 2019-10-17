package com.deelock.wifilock.ui.activity;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.text.TextUtils;

import com.deelock.wifilock.utils.StatusBarUtil;
import com.google.gson.Gson;
import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.ActivityRegisterBinding;
import com.deelock.wifilock.entity.Check;
import com.deelock.wifilock.entity.Pid;
import com.deelock.wifilock.event.RegisterEvent;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.entity.RegisterRequest;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.utils.InputUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by binChuan on 2017\9\14 0014.
 */

public class RegisterActivity extends BaseActivity {

    private ActivityRegisterBinding binding;
    private RegisterRequest register;

    private boolean canSendMessage = true;
    private boolean canRegister = true;

    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);
    }

    @Override
    protected void doBusiness() {
        StatusBarUtil.StatusBarLightMode(this);
        register = new RegisterRequest();
        binding.setRegister(register);
        setNickNameInputFilter(binding.nickNameEt);
    }

    @Override
    protected void requestData() {

    }

    @Override
    protected void setEvent() {
        binding.setEvent(new RegisterEvent() {
            @Override
            public void back() {
                finish();
            }

            @Override
            public void sendMessage() {
                checkUser();
            }

            @Override
            public void commit() {
                checkInput();
            }
        });
    }

    /**
     * 发送短信验证
     */
    private void requestMessage() {
        if (!isNetworkAvailable()) {
            return;
        }

        Map params = new HashMap<>();
        params.put("timestamp", TimeUtil.getTime());
        params.put("type", 10);
        params.put("phoneNumber", register.getPhoneNumber());
        RequestUtils.requestUnLogged(RequestUtils.GET_MESSAGE, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        countDown();
                        ToastUtil.toastShort(getApplicationContext(), "发送成功");
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        canSendMessage = true;
                    }
                }
        );
    }

    private void countDown() {
        final int count = 90;

        Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(count + 1)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(@NonNull Long aLong) throws Exception {
                        return count - aLong;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        binding.sendBtn.setEnabled(false);
                        binding.sendBtn.setTextColor(Color.GRAY);
                    }
                })
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Long aLong) {
                        binding.sendBtn.setText(aLong + "秒后重发");
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        binding.sendBtn.setEnabled(true);
                        binding.sendBtn.setTextColor(0xff333333);
                        binding.sendBtn.setText("发送验证码");
                        canSendMessage = true;
                    }
                });
    }

    private void checkUser() {
        if (!canSendMessage) {
            return;
        }

        if (!InputUtil.checkPhoneNumber(this, register.getPhoneNumber())) {
            return;
        }

        if (!isNetworkAvailable()) {
            return;
        }

        canSendMessage = false;
        Map params = new HashMap<>();
        params.put("phoneNumber", register.getPhoneNumber());
        RequestUtils.requestUnLogged(RequestUtils.CHECK, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        Check check = new Gson().fromJson(content, Check.class);
                        if (check.getIsExist() == 1) {
                            ToastUtil.toastShort(getApplicationContext(), "用户已注册");
                            canSendMessage = true;
                        } else {
                            requestMessage();
                            countDown();
                        }
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        canSendMessage = true;
                    }
                });
    }

    private void checkInput() {
        if (!canRegister) {
            return;
        }
        canRegister = false;

        if (!InputUtil.checkPhoneNumber(this, register.getPhoneNumber())) {
            canRegister = true;
            return;
        }

        if (TextUtils.isEmpty(register.getMsgCode())) {
            canRegister = true;
            ToastUtil.toastShort(getApplicationContext(), "请输入短信验证码");
            return;
        }

        if (register.getMsgCode().length() < 4) {
            canRegister = true;
            ToastUtil.toastShort(getApplicationContext(), "请输入完整短信验证码");
            return;
        }

        if (TextUtils.isEmpty(register.getPassword())) {
            canRegister = true;
            ToastUtil.toastShort(getApplicationContext(), "请输入密码");
            return;
        }

        if (register.getPassword().length() < 6) {
            canRegister = true;
            ToastUtil.toastShort(getApplicationContext(), "请输入完整密码");
            return;
        }

        if (TextUtils.isEmpty(register.getNickName())) {
            canRegister = true;
            ToastUtil.toastShort(getApplicationContext(), "请输入昵称");
            return;
        }

        if (register.getNickName().length() < 2) {
            canRegister = true;
            ToastUtil.toastShort(getApplicationContext(), "昵称必须大于2位");
            return;
        }
        register();
    }

    private void register() {
        if (!isNetworkAvailable()) {
            return;
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("phoneNumber", register.getPhoneNumber());
        params.put("pwd", register.getPassword());
        params.put("msgCode", register.getMsgCode());
        params.put("nickName", register.getNickName());
        RequestUtils.requestUnLogged(RequestUtils.REGISTER, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onFinish() {
                        canRegister = true;
                    }

                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        ToastUtil.toastShort(getApplicationContext(), "注册成功");
                        Pid pid = new Gson().fromJson(content, Pid.class);
                        SPUtil.setUid(RegisterActivity.this, pid);
                        finish();
                    }
                });
    }
}
