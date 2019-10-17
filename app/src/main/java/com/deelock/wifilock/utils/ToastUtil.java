package com.deelock.wifilock.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by binChuan on 2017\9\14 0014.
 */

public class ToastUtil {

    /**
     * Toast实例，用于对本页出现的所有Toast进行处理
     */
    private static Toast myToast;

    /**
     * 此处是一个封装的Toast方法，可以取消掉上一次未完成的，直接进行下一次Toast
     * @param context context
     * @param text 需要toast的内容
     */
    public static void toastShort(Context context, String text){
        if (myToast != null) {
            myToast.cancel();
            myToast=Toast.makeText(context,text,Toast.LENGTH_SHORT);
        }else{
            myToast=Toast.makeText(context,text,Toast.LENGTH_SHORT);
        }
        myToast.show();
    }

    public static void toastLong(Context context, String text){
        if (myToast != null) {
            myToast.cancel();
            myToast=Toast.makeText(context,text,Toast.LENGTH_LONG);
        }else{
            myToast=Toast.makeText(context,text,Toast.LENGTH_LONG);
        }
        myToast.show();
    }

    public static void showFailDetail(Context context, int code){
        switch (code){
            case 0:
                toastShort(context, "失败");
                break;
            case 2:
                toastShort(context, "请求成功，但没有更多数据");
                break;
            case 100:
                toastShort(context, "未登录");
                break;
            case 101:
                toastShort(context, "用户不存在");
                break;
            case 102:
                toastShort(context, "密码错误");
                break;
            case 103:
                toastShort(context, "新旧密码不一致");
                break;
            case 200:
                toastShort(context, "没有操作权限");
                break;
            case 300:
                toastShort(context, "参数缺失");
                break;
            case 301:
                toastShort(context, "参数格式问题");
                break;
            case 302:
                toastShort(context, "参数类型问题");
                break;
            case 303:
                toastShort(context, "参数长度问题");
                break;
            case 400:
                toastShort(context, "token验证失败，请重新密码登录刷新token");
                break;
            case 401:
                toastShort(context, "token失效，请重新密码登录刷新token");
                break;
            case 402:
                toastShort(context, "客户端请求指令已过期或超时");
                break;
            case 403:
                toastShort(context, "短信验证码验证失败");
                break;
            case 404:
                toastShort(context, "获取短信验证码超过次数");
                break;
        }
    }
}

