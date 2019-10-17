package com.deelock.wifilock.network;

import android.content.Context;

import com.deelock.wifilock.utils.AES7P256;
import com.deelock.wifilock.utils.MD5Util;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ZLibUtils;
import com.google.gson.Gson;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by forgive for on 2018\3\20 0020.
 */

public class ApiMethods {

    public static void ApiSubscribe(Observable observable, Observer observer) {
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void getResponse(RObserver<OResponse> observer, Context context, String url, Map params) {
        int check = 1;
        String token = SPUtil.getToken(context);
        String accountMark = SPUtil.getKey(context);
        String sign = MD5Util.string2MD5(params);
        byte[] zip = ZLibUtils.compress(new Gson().toJson(params));
        String content = AES7P256.encrypt(zip, accountMark);
        ApiSubscribe(Api.getService().oRequest(url, sign, token, check, content), observer);
    }
}
