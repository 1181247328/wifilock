package com.deelock.wifilock.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.deelock.wifilock.entity.LockDetail;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.activity.MainActivity;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.google.gson.Gson;

import java.lang.annotation.ElementType;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by forgive for on 2018\5\18 0018.
 */
public class MagnetometerDetailModel {

    private MagnetometerDetailModelImpl model;
    private String pid;

    public MagnetometerDetailModel(MagnetometerDetailModelImpl model, String pid) {
        this.model = model;
        this.pid = pid;
    }

    public void modifyName(Context context, String name) {
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(context));
        String url = null;
        if ("C00".equals(pid.substring(0, 3))) {
            url = RequestUtils.ZIGBEE_UPDATE;
            params.put("devId", pid);
            params.put("devName", name);
        } else if ("C01".equals(pid.substring(0, 3))) {
            url = RequestUtils.INFRARED_UPDATE;
            params.put("devId", pid);
            params.put("devName", name);
        } else {
            url = RequestUtils.MAGNETOMETER_UPDATE;
            params.put("pid", pid);
            params.put("nickName", name);
        }
        RequestUtils.request(url, context, params).enqueue(new ResponseCallback<BaseResponse>((Activity) context) {
            @Override
            protected void onSuccess(int code, String content) {
                if (code == 1) {
                    model.onModifyName();
                }
            }
        });
    }

    public void deleteEquipment(final Context context) {
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(context));
        params.put("pid", pid);
        params.put("type", "B00");
        RequestUtils.request(RequestUtils.UNBIND, context, params).enqueue(new ResponseCallback<BaseResponse>((Activity) context) {
            @Override
            protected void onSuccess(int code, String content) {
                if (code == 1) {
                    ToastUtil.toastShort(context, "已解绑该门磁");
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else {
                    ToastUtil.toastShort(context, content);
                }
            }

            @Override
            protected void onFailure(int code, String message) {
                super.onFailure(code, message);
                ToastUtil.toastShort(context, message);
            }
        });
    }

    public void warning(final Context context, boolean isCheck) {
        int isCall = 0;
        if (isCheck) {
            isCall = 1;
        }
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(context));
        String url = null;
        if ("C00".equals(pid.substring(0, 3))) {
            url = RequestUtils.ZIGBEE_UPDATE;
            params.put("devId", pid);
        } else {
            url = RequestUtils.MAGNETOMETER_UPDATE;
            params.put("pid", pid);
        }
        params.put("isCall", isCall);
        RequestUtils.request(url, context, params).enqueue(new ResponseCallback<BaseResponse>((Activity) context) {
            @Override
            protected void onSuccess(int code, String content) {
                ToastUtil.toastShort(context, "修改成功");
            }

            @Override
            protected void onFailure(int code, String message) {
                super.onFailure(code, message);
                ToastUtil.toastShort(context, message);
            }
        });
    }

    public void setPush(Context context, String type) {
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(context));
        String url = null;
        if ("C00".equals(pid.substring(0, 3))) {
            url = RequestUtils.ZIGBEE_UPDATE;
            params.put("devId", pid);
        } else {
            url = RequestUtils.MAGNETOMETER_UPDATE;
            params.put("pid", pid);
        }
        params.put("alertType", type);
        RequestUtils.request(url, context, params).enqueue(new ResponseCallback<BaseResponse>((Activity) context) {
            @Override
            protected void onSuccess(int code, String content) {

            }
        });
    }

    public void accessXinJia(final Context context, String inputCode) {
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(context));
        params.put("accountId", 1000);
        params.put("sdsId", pid);
        params.put("settingCode", inputCode);
        RequestUtils.request(RequestUtils.MAGNETIC_ACCESS, context, params).enqueue(new ResponseCallback<BaseResponse>((Activity) context) {
            @Override
            protected void onSuccess(int code, String content) {
                super.onSuccess(code, content);
                ToastUtil.toastLong(context, content);
            }

            @Override
            protected void onFailure(int code, String message) {
                super.onFailure(code, message);
                ToastUtil.toastLong(context, message);
            }
        });
    }
}
