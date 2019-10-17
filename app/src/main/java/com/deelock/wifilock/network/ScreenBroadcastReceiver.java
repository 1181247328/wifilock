package com.deelock.wifilock.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.deelock.wifilock.constants.Constants;

/**
 * Created by forgive for on 2017\12\20 0020.
 */

public class ScreenBroadcastReceiver extends BroadcastReceiver {
    private String action = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        action = intent.getAction();
        // ----------------开屏时调用---------------
        if (Intent.ACTION_SCREEN_ON.equals(action)) {

        }
        // ----------------锁屏时调用---------------
        else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            Constants.isVerifiedGesture = false;
        }
        // ----------------解锁时调用---------------
        else if (Intent.ACTION_USER_PRESENT.equals(action)) {
            Constants.isVerifiedGesture = false;
        }
        // ----------------按home键时调用---------------
        else if(action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
            String reason = intent.getStringExtra("reason");
            if (reason !=null) {
                //短按home键
                if (reason.equals("homekey")) {
                    // 短按home键
                    Constants.isVerifiedGesture = false;
                }
                // 长按home键
                else if(reason.equals("recentapps")){
                    //mScreenStateListener.onHomePresseLong();//自己去写长按方法与短按类似
                }
            }

        }
    }
}
