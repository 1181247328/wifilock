package com.deelock.wifilock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.deelock.wifilock.R;


/**
 * 检查蓝牙门锁是否连接wifi/检查手机app是否连接蓝牙门锁/判断密码添加状态
 */
public class BleNetDialog extends Dialog {

    private ImageView imageView;

    public BleNetDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_ble_tips);
        setCanceledOnTouchOutside(false);
        imageView = findViewById(R.id.ble_tips_image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public void setState(int state) {
        if (state == 1) {
            //提示用户蓝牙不在身边
            imageView.setImageResource(R.drawable.ble_tips);
        }
        if (state == 2) {
            //提示用户绑定wifi
            imageView.setImageResource(R.drawable.ble_tips_3);
        }
        if (state == 3) {
            //提示当前密码状态
            imageView.setImageResource(R.drawable.notify_password_state);
        }
    }
}
