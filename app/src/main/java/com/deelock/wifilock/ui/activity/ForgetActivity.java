package com.deelock.wifilock.ui.activity;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.text.TextUtils;

import com.deelock.wifilock.entity.Check;
import com.deelock.wifilock.utils.StatusBarUtil;
import com.google.gson.Gson;
import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.ActivityForgetBinding;
import com.deelock.wifilock.event.ForgetEvent;
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
 * Created by binChuan on 2017\9\13 0013.
 */

public class ForgetActivity extends BaseActivity {

    ActivityForgetBinding binding;
    private RegisterRequest forget;

    private boolean canSendMessage = true;
    private boolean commitAble = true;

    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forget);
        StatusBarUtil.StatusBarLightMode(this);
    }

    @Override
    protected void doBusiness() {
        forget = new RegisterRequest();
        binding.setForget(forget);
    }

    @Override
    protected void requestData() {

    }

    @Override
    protected void setEvent() {
        binding.setEvent(new ForgetEvent() {
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

    private void checkUser() {
        if (!canSendMessage) {
            return;
        }

        if (!InputUtil.checkPhoneNumber(this, forget.getPhoneNumber())) {
            canSendMessage = true;
            return;
        }

        if (!isNetworkAvailable()) {
            return;
        }

        canSendMessage = false;
        Map params = new HashMap<>();
        params.put("phoneNumber", forget.getPhoneNumber());
        RequestUtils.requestUnLogged(RequestUtils.CHECK, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        Check check = new Gson().fromJson(content, Check.class);
                        if (check.getIsExist() == 0) {
                            ToastUtil.toastShort(getApplicationContext(), "用户不存在");
                            canSendMessage = true;
                        } else {
                            requestMessage();
                        }
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        canSendMessage = true;
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
        params.put("token", SPUtil.getToken(this));
        params.put("timestamp", TimeUtil.getTime());
        params.put("type", 11);
        params.put("phoneNumber", forget.getPhoneNumber());
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
                        binding.sendBtn.setTextColor(0xffaaaaaa);
                        binding.sendBtn.setText("发送验证码");
                        canSendMessage = true;
                    }
                });
    }

    private void checkInput() {
        if (!commitAble) {
            return;
        }

        if (!InputUtil.checkPhoneNumber(this, forget.getPhoneNumber())) {
            return;
        }

        if (TextUtils.isEmpty(forget.getMsgCode())) {
            ToastUtil.toastShort(getApplicationContext(), "请输入短信验证码");
            return;
        }

        if (forget.getMsgCode().length() < 4) {
            ToastUtil.toastShort(getApplicationContext(), "请输入完整短信验证码");
            return;
        }

        if (TextUtils.isEmpty(forget.getPassword())) {
            ToastUtil.toastShort(getApplicationContext(), "请输入密码");
            return;
        }

        if (forget.getPassword().length() < 6) {
            ToastUtil.toastShort(getApplicationContext(), "请输入完整密码");
            return;
        }

        commit();
    }

    private void commit() {
        if (!isNetworkAvailable()) {
            return;
        }

        commitAble = false;
        Map params = new HashMap();
        params.put("phoneNumber", forget.getPhoneNumber());
        params.put("pwd", forget.getPassword());
        params.put("msgCode", forget.getMsgCode());
        RequestUtils.requestUnLogged(RequestUtils.FORGET, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onFinish() {
                        commitAble = true;
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        ToastUtil.toastShort(getApplicationContext(), message);
                    }

                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        ToastUtil.toastShort(getApplicationContext(), "修改成功");
                        finish();
                    }
                }
        );
    }
}
