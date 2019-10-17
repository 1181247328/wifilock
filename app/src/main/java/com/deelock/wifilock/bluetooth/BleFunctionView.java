package com.deelock.wifilock.bluetooth;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.deelock.wifilock.R;

public class BleFunctionView extends FrameLayout {

    public static final int FUN_VOLUME = 0;   //设备音量
    public static final int FUN_WEISUI = 1;   //防尾随
    public static final int FUN_REMOTE = 2;   //远程下发密码
    public static final int FUN_FANSUO = 3;   //电子反锁
    public static final int FUN_RESTORE = 4;  //恢复出厂
    public static final int FUN_WIFI = 5;   //关闭wifi

    private int STATE_VIEW = -1;

    private int STATE_SEC = -1;

    private View view_volume = null;
    private View view_weisui = null;
    private View view_remote = null;
    private View view_fansuo = null;
    private View view_restore = null;
    private View view_wifi = null;

    public TextView text1, text2, text3, text4, text5, text6, text7, text8;
    public ImageView img1, img2, img3, img4, img5, img6, img7, img8;
    public Button btn1, btn2, btn3, btn4, restore, openWifi, closeWifi;
    private funBleViewListener bleViewListener;

    public BleFunctionView(@NonNull Context context) {
        this(context, null);
    }

    public BleFunctionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BleFunctionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (view_volume == null) {
            view_volume = View.inflate(context, R.layout.dialog_ble_volume, null);

            text1 = view_volume.findViewById(R.id.fun_ble_text1);
            text2 = view_volume.findViewById(R.id.fun_ble_text2);
            text3 = view_volume.findViewById(R.id.fun_ble_text3);
            text4 = view_volume.findViewById(R.id.fun_ble_text4);

            img1 = view_volume.findViewById(R.id.fun_ble_img1);
            img2 = view_volume.findViewById(R.id.fun_ble_img2);
            img3 = view_volume.findViewById(R.id.fun_ble_img3);
            img4 = view_volume.findViewById(R.id.fun_ble_img4);

            addView(view_volume);
        }
        if (view_weisui == null) {
            view_weisui = View.inflate(context, R.layout.dialog_ble_weisui, null);

            text5 = view_weisui.findViewById(R.id.fun_ble_text5);
            text6 = view_weisui.findViewById(R.id.fun_ble_text6);
            text7 = view_weisui.findViewById(R.id.fun_ble_text7);
            text8 = view_weisui.findViewById(R.id.fun_ble_text8);

            img5 = view_weisui.findViewById(R.id.fun_ble_img5);
            img6 = view_weisui.findViewById(R.id.fun_ble_img6);
            img7 = view_weisui.findViewById(R.id.fun_ble_img7);
            img8 = view_weisui.findViewById(R.id.fun_ble_img8);

            addView(view_weisui);
        }
        if (view_remote == null) {
            view_remote = View.inflate(context, R.layout.dialog_ble_remote, null);

            btn3 = view_remote.findViewById(R.id.fun_ble_btn3);
            btn4 = view_remote.findViewById(R.id.fun_ble_btn4);

            addView(view_remote);
        }
        if (view_fansuo == null) {
            view_fansuo = View.inflate(context, R.layout.dialog_ble_fansuo, null);

            btn1 = view_fansuo.findViewById(R.id.fun_ble_btn1);
            btn2 = view_fansuo.findViewById(R.id.fun_ble_btn2);

            addView(view_fansuo);
        }
        if (view_restore == null) {
            view_restore = View.inflate(context, R.layout.dialog_ble_restore, null);
            restore = view_restore.findViewById(R.id.ble_restore_btn);
            addView(view_restore);
        }
        if (view_wifi == null) {
            view_wifi = View.inflate(context, R.layout.dialog_ble_wifi, null);
            openWifi = view_wifi.findViewById(R.id.fun_ble_open_wifi);
            closeWifi = view_wifi.findViewById(R.id.fun_ble_close_wifi);
            addView(view_wifi);
        }
        setListener();
    }

    private void setListener() {
//        防尾随
        img5.setOnClickListener(listener);
        img6.setOnClickListener(listener);
        img7.setOnClickListener(listener);
        img8.setOnClickListener(listener);
        text5.setOnClickListener(listener);
        text6.setOnClickListener(listener);
        text7.setOnClickListener(listener);
        text8.setOnClickListener(listener);

//        远程下发密码
        btn3.setOnClickListener(listener);
        btn4.setOnClickListener(listener);

//        电子反锁
        btn1.setOnClickListener(listener);
        btn2.setOnClickListener(listener);

//        恢复出厂设置
        restore.setOnClickListener(listener);

//        关闭wifi
        openWifi.setOnClickListener(listener);
        closeWifi.setOnClickListener(listener);
    }

    private OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fun_ble_text5:
                    STATE_SEC = 2;
                    bleViewListener.weisui_2();
                    break;
                case R.id.fun_ble_img5:
                    STATE_SEC = 2;
                    bleViewListener.weisui_2();
                    break;

                case R.id.fun_ble_text6:
                    STATE_SEC = 3;
                    bleViewListener.weisui_3();
                    break;
                case R.id.fun_ble_img6:
                    STATE_SEC = 3;
                    bleViewListener.weisui_3();
                    break;

                case R.id.fun_ble_text7:
                    STATE_SEC = 4;
                    bleViewListener.weisui_4();
                    break;
                case R.id.fun_ble_img7:
                    STATE_SEC = 4;
                    bleViewListener.weisui_4();
                    break;

                case R.id.fun_ble_text8:
                    STATE_SEC = 5;
                    bleViewListener.weisui_5();
                    break;
                case R.id.fun_ble_img8:
                    STATE_SEC = 5;
                    bleViewListener.weisui_5();
                    break;

                case R.id.fun_ble_btn3:
                    bleViewListener.remote_start();
                    break;
                case R.id.fun_ble_btn4:
                    bleViewListener.remote_close();
                    break;

                case R.id.fun_ble_btn1:
                    bleViewListener.fansuo_start();
                    break;
                case R.id.fun_ble_btn2:
                    bleViewListener.fansuo_close();
                    break;

                case R.id.ble_restore_btn:
                    bleViewListener.restore();
                    break;

                case R.id.fun_ble_open_wifi:
                    bleViewListener.openWifi();
                    break;
                case R.id.fun_ble_close_wifi:
                    bleViewListener.closeWifi();
                    break;
            }
            if (STATE_SEC != -1) {
                setCurrentSec(STATE_SEC);
            }
        }
    };

    public void setCurrentSec(int sec) {
        text5.setSelected(sec == 2);
        text6.setSelected(sec == 3);
        text7.setSelected(sec == 4);
        text8.setSelected(sec == 5);
        img5.setSelected(sec == 2);
        img6.setSelected(sec == 3);
        img7.setSelected(sec == 4);
        img8.setSelected(sec == 5);

        img5.setScaleType(sec == 2 ? ImageView.ScaleType.FIT_XY : ImageView.ScaleType.CENTER);
        img6.setScaleType(sec == 3 ? ImageView.ScaleType.FIT_XY : ImageView.ScaleType.CENTER);
        img7.setScaleType(sec == 4 ? ImageView.ScaleType.FIT_XY : ImageView.ScaleType.CENTER);
        img8.setScaleType(sec == 5 ? ImageView.ScaleType.FIT_XY : ImageView.ScaleType.CENTER);
        STATE_SEC = -1;
    }

    public void setRemotePW(int a) {
        btn3.setClickable(a == 0);
        btn4.setClickable(a != 0);
        if (a == 0) {
            //关远闭程下发
            btn3.setTextColor(Color.rgb(255, 255, 255));
            btn4.setTextColor(Color.rgb(62, 195, 247));
            btn4.setBackgroundResource(R.drawable.bottom_dialog_close);
            btn3.setBackgroundResource(R.drawable.login_select_bg);
        } else if (a == 1) {
            //开启远程下发
            btn4.setTextColor(Color.rgb(255, 255, 255));
            btn3.setTextColor(Color.rgb(62, 195, 247));
            btn3.setBackgroundResource(R.drawable.bottom_dialog_close);
            btn4.setBackgroundResource(R.drawable.login_select_bg);
        }
    }

    public void setElect(int a) {
        btn1.setClickable(a == 0);
        btn2.setClickable(a != 0);
        if (a == 0) {
            //关闭电子反锁
            btn1.setTextColor(Color.rgb(255, 255, 255));
            btn1.setBackgroundResource(R.drawable.login_select_bg);
            btn2.setBackgroundResource(R.drawable.bottom_dialog_close);
            btn2.setTextColor(Color.rgb(62, 195, 247));
        } else if (a == 1) {
            //开启电子反锁
            btn1.setTextColor(Color.rgb(62, 195, 247));
            btn1.setBackgroundResource(R.drawable.bottom_dialog_close);
            btn2.setTextColor(Color.rgb(255, 255, 255));
            btn2.setBackgroundResource(R.drawable.login_select_bg);
        }
    }

    public void setWifiState(int a) {
        openWifi.setClickable(a == 0);
        closeWifi.setClickable(a != 0);
        if (a == 0) {
            //关闭wifi
            openWifi.setTextColor(Color.rgb(255, 255, 255));
            openWifi.setBackgroundResource(R.drawable.login_select_bg);
            closeWifi.setBackgroundResource(R.drawable.bottom_dialog_close);
            closeWifi.setTextColor(Color.rgb(62, 195, 247));
        } else if (a == 1) {
            //开启wifi
            openWifi.setTextColor(Color.rgb(62, 195, 247));
            openWifi.setBackgroundResource(R.drawable.bottom_dialog_close);
            closeWifi.setTextColor(Color.rgb(255, 255, 255));
            closeWifi.setBackgroundResource(R.drawable.login_select_bg);
        }
    }

    public void setCurrentView(int page) {
        STATE_VIEW = page;
        showView();
    }

    private void showView() {
        view_volume.setVisibility(STATE_VIEW == FUN_VOLUME ? VISIBLE : GONE);
        view_weisui.setVisibility(STATE_VIEW == FUN_WEISUI ? VISIBLE : GONE);
        view_remote.setVisibility(STATE_VIEW == FUN_REMOTE ? VISIBLE : GONE);
        view_fansuo.setVisibility(STATE_VIEW == FUN_FANSUO ? VISIBLE : GONE);
        view_restore.setVisibility(STATE_VIEW == FUN_RESTORE ? VISIBLE : GONE);
        view_wifi.setVisibility(STATE_VIEW == FUN_WIFI ? VISIBLE : GONE);
    }

    public void setInterface(funBleViewListener listener) {
        bleViewListener = listener;
    }

    public interface funBleViewListener {
        void weisui_2();   //尾随2秒

        void weisui_3();   //尾随3秒

        void weisui_4();   //尾随4秒

        void weisui_5();   //尾随5秒

        void remote_start();   //开启远程下发密码

        void remote_close();  //关闭远程下发密码

        void fansuo_start();   //开启电子反锁

        void fansuo_close();   //关闭电子反锁

        void restore();   //恢复门锁出厂设置

        void openWifi();  //开启wifi

        void closeWifi();   //关闭wifi
    }
}
