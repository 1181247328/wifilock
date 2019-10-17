package com.deelock.wifilock.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.deelock.wifilock.constants.Constants;

/**
 * Created by Administrator on 2017\11\9 0009.
 */

public abstract class CommonAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    Context context;
    boolean clickAble = true;

    protected void openActivityForMoment(final Class<?> clz, final Bundle bundle){
        if (!clickAble){
            return;
        }
        clickAble = false;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(Constants.START_ACTIVITY_DELAY);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                Intent intent = new Intent();
//                intent.setClass(context, clz);
//                if (bundle != null) {
//                    intent.putExtras(bundle);
//                }
//                context.startActivity(intent);
//                clickAble = true;
//            }
//        }).start();
        Intent intent = new Intent();
        intent.setClass(context, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
        clickAble = true;
    }

    protected void openActivity(final Class<?> clz, final Bundle bundle){
        Intent intent = new Intent();
        intent.setClass(context, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
    }
}
