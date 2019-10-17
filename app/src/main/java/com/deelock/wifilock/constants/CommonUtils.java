package com.deelock.wifilock.constants;

import android.content.Context;

import com.deelock.wifilock.utils.SPUtil;

/**
 * Created by forgive for on 2017\12\22 0022.
 */

public class CommonUtils {

    public static boolean isGestureSet(Context context){
        if (SPUtil.getGesture(context) == null){
            return false;
        }
        return true;
    }
}
