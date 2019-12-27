package com.deelock.wifilock.network;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.deelock.wifilock.ui.activity.LoginActivity;
import com.deelock.wifilock.utils.BluetoothUtil;
import com.deelock.wifilock.utils.Logger;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by binChuan on 2017\9\12 0012.
 */

@SuppressWarnings("all")
public abstract class ResponseCallback<Body extends BaseResponse> implements Callback<Body> {
    private final Logger mLogger = Logger.getLogger(ResponseCallback.class);
    private final WeakReference<Activity> mActivityRef;

    public ResponseCallback(Activity context) {
        mActivityRef = new WeakReference<Activity>(context);
    }

    public ResponseCallback() {
        mActivityRef = null;
    }

    @Override
    public final void onResponse(Call<Body> call, Response<Body> response) {
        int code = response.body().code;
        if (mActivityRef != null && mActivityRef.get() != null) {
            String token = SPUtil.getKey(mActivityRef.get());
            Log.e("main","---"+code+"---"+token);

//            Log.e("main_网络返回数据",
//                    response.body().code + "--" + response.body().msg + "  ---- " + AES7P256.getData((String) response.body().content, token));
            switch (code) {
                case -300:
                case -201:
                case -202:
                    BluetoothUtil.closeBluetooth();
                    BluetoothUtil.stopBluetooth();
                    BluetoothUtil.clearInfo();
                    Intent intent = new Intent(mActivityRef.get(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    SPUtil.logout(mActivityRef.get());
                    mActivityRef.get().startActivity(intent);
                    ToastUtil.toastLong(mActivityRef.get().getApplicationContext(), "登录信息异常，请重新登录");
                    return;
                case -304:
                    ToastUtil.toastLong(mActivityRef.get().getApplicationContext(), response.body().msg);
                    break;
                case -305:
                case -400:
                    ToastUtil.toastLong(mActivityRef.get().getApplicationContext(), "获取短信验证码失败");
                    break;
                case -401:
                    ToastUtil.toastLong(mActivityRef.get().getApplicationContext(), "短信验证码超过次数");
                    break;
                case -402:
                    ToastUtil.toastLong(mActivityRef.get().getApplicationContext(), "短信验证码校验失败");
                    break;
                case -403:
                    ToastUtil.toastLong(mActivityRef.get().getApplicationContext(), "短信验证码已过期");
                    break;
                case -100:
                    ToastUtil.toastLong(mActivityRef.get().getApplicationContext(), "未登录");
                    break;
                case -101:
                    ToastUtil.toastLong(mActivityRef.get().getApplicationContext(), "用户不存在");
                    break;
                case -102:
                    ToastUtil.toastLong(mActivityRef.get().getApplicationContext(), "密码错误");
                    break;
                case -103:
                    ToastUtil.toastLong(mActivityRef.get().getApplicationContext(), "新旧密码不一致");
                    break;
                case -200:
                    ToastUtil.toastLong(mActivityRef.get().getApplicationContext(), "没有操作权限");
                    break;
                case -203:
                    ToastUtil.toastLong(mActivityRef.get().getApplicationContext(), "请求指令已过期或超时");
                    break;
                case -301:
                    ToastUtil.toastLong(mActivityRef.get().getApplicationContext(), "参数格式问题");
                    break;
                case -302:
                    ToastUtil.toastLong(mActivityRef.get().getApplicationContext(), "参数类型问题");
                    break;
                case -303:
                    ToastUtil.toastLong(mActivityRef.get().getApplicationContext(), "参数长度问题");
                    break;
                case -350:
                    ToastUtil.toastLong(mActivityRef.get().getApplicationContext(), "参数错误问题");
                    break;
                case -351:
                    ToastUtil.toastLong(mActivityRef.get().getApplicationContext(), "参数不能为空");
                    break;
            }
        }
        if (isSuccessful(response)) {
            notifySuccess(response.body(), response.headers());
        } else {
            notifyFailure(response.code(), getFailureResponse(response));
            notifyFailure(response.body(), response.headers());
        }
    }

    private boolean isSuccessful(Response<Body> response) {
        Body body = response.body();
        return response.isSuccessful() && body != null
                && body.code >= ErrorCode.Success.getValue();
    }

    private String getFailureResponse(Response<Body> response) {
        if (response.isSuccessful()) {
            return getServerSideFailureResponse(response);
        }
        return getClientSideFailureResponse(response);
    }

    private String getClientSideFailureResponse(Response<Body> response) {
        try {
            ResponseBody responseBody = response.errorBody();
            if (responseBody != null) {
                return responseBody.string();
            }
            return "";
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private String getServerSideFailureResponse(Response<Body> response) {
        Body body = response.body();
        if (body != null) {
            return body.msg;
        }
        return "";
    }

    @Override
    public final void onFailure(Call<Body> call, Throwable t) {
        mLogger.error(t, "onFailure:");
        String fm = getFailureMessage(t);
        notifyFailure(ErrorCode.Exception.getValue(), fm);
    }

    private String getFailureMessage(Throwable t) {
        if (t instanceof ConnectException) {
            return "连接服务器失败，请检查网络！";
        } else if (t instanceof SocketTimeoutException) {
            return "连接服务器超时，请检查网络！";
        }
        return t.getMessage();
    }

    private void notifySuccess(Body response, Headers headers) {
        mLogger.debug("notifySuccess:");
        onBeforeFinish();
        onSuccess(response.code, response.getContent(mActivityRef.get()));
        onSuccess(response, headers);
        onFinish();
    }

    private void notifyFailure(Body response, Headers headers) {
        onBeforeFinish();
        onFailure(response);
        onFinish();
    }

    private void notifyFailure(int code, String failureResponse) {
        onBeforeFinish();
        onFailure(code, failureResponse);
        onFinish();
    }

    protected void onBeforeFinish() {
        // Called before result
    }

    protected void onFinish() {
        // Called after result
    }

    protected void onSuccess(Body response, Headers headers) {
        // Same as onSuccess(RequestBody) but with header
    }

    protected void onSuccess(int code, String content) {

    }

    protected void onFailure(int code, String message) {

    }

    protected void onFailure(Body response) {

    }
}
