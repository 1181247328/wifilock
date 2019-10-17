package com.deelock.wifilock.network;

import android.content.Context;
import android.util.Log;

import com.deelock.wifilock.utils.AES7P256;
import com.deelock.wifilock.utils.SPUtil;

/**
 * Created by binChuan on 2017\9\11 0011.
 */

public class BaseResponse {

    public int code;

    public String msg;

    public Object content;

    public String getContent(Context context) {
        if (content == null){
            return "";
        }
        String token = SPUtil.getKey(context);
        return AES7P256.getData(content.toString(), token);
    }
}
