package com.deelock.wifilock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deelock.wifilock.R;

public class AddInfoDialog extends Dialog {

    private String TYPE_WIFI = "A001";   //wifi锁
    private String TYPE_HOME = "A002";   //民宿锁
    private String TYPE_BLE = "A003";    //蓝牙锁

    private ImageView iv;
    private TextView tv1;
    private TextView tv2;
    private ImageView del;
    private LinearLayout cardll, printrl, pwdrl;
    private InfoClickListener listener;

    public AddInfoDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        Window window = getWindow();
        assert window != null;
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        window.setGravity(Gravity.CENTER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.window_add);
        initView();
        initAllThing();
    }

    private void initView() {
        iv = findViewById(R.id.first_iv);
        tv1 = findViewById(R.id.first_tv);
        tv2 = findViewById(R.id.secondTv);
        del = findViewById(R.id.window_del);
        cardll = findViewById(R.id.card_rl);
        printrl = findViewById(R.id.print_rl);
        pwdrl = findViewById(R.id.password_rl);
    }

    private void initAllThing() {
        printrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.addFprint();
            }
        });

        pwdrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.addPwd();
            }
        });

        cardll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.addCard();
            }
        });

        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public void setInfoListener(InfoClickListener listener) {
        this.listener = listener;
    }

    public void setCurrentView(String type) {
//        if (TYPE_HOME.equals(type)) {
//            iv.setImageResource(R.mipmap.add_landlord_pwd);
//            tv1.setText("添加房东密码");
//            tv2.setText("添加租客密码");
//        }
//        if (TYPE_BLE.equals(type)) {
////            cardll.setVisibility(View.VISIBLE);
//        }
    }


    public interface InfoClickListener {
        void addPwd();

        void addFprint();

        void addCard();
    }
}
