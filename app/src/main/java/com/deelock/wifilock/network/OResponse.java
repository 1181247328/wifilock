package com.deelock.wifilock.network;

import android.content.Context;
import android.util.Log;

import com.deelock.wifilock.entity.LockStateList;
import com.deelock.wifilock.utils.AES7P256;
import com.deelock.wifilock.utils.SPUtil;
import com.google.gson.Gson;

/**
 * Created by forgive for on 2018\3\22 0022.
 */

public class OResponse {

    public int code;

    public String msg;

    public Object content;

    public String getContent(Context context) {
        if (content == null){
            return null;
        }
        String token = SPUtil.getKey(context);
        return AES7P256.getData(content.toString(), token);
    }

    public LockStateList getLockStateList(Context context){
        String result = getContent(context);
        return new Gson().fromJson(result, LockStateList.class);
    }
}
