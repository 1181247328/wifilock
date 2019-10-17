package com.deelock.wifilock.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.entity.MagDetail;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.presenter.MagnetometerPresenter;
import com.deelock.wifilock.utils.DensityUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.deelock.wifilock.view.IMagnetometerView;

/**
 * Created by forgive for on 2018\1\12 0012.
 */

public class MagnetometerActivity extends BaseActivity implements View.OnClickListener, IMagnetometerView {

    //widget
    private ImageButton back_ib;
    private TextView title_tv;
    private TextView day_tv;
    private ImageView magnetometer_left_iv;
    private ImageView magnetometer_right_iv;
    private TextView open_state_tv;
    private ImageView circle_outer_iv;
    private ImageView signal_iv;
    private TextView signal_tv;
    private ImageView warning_iv;
    private TextView warning_tv;
    private ImageView power_iv;
    private TextView power_tv;
    private LinearLayout detail_ll;
    private LinearLayout history_ll;

    private MagnetometerPresenter presenter;
    private int moveDistance;
    private String deviceId;

    private Handler mHandler = new Handler();

    private RotateAnimation animation;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            open_state_tv.setText("- 监测中 -");
            open_state_tv.setTextSize(12);
            open_state_tv.setTextColor(getResources().getColor(R.color.device_state));
        }
    };

    @Override
    protected void bindActivity() {
        setContentView(R.layout.activity_magnetometer);
    }

    @Override
    protected void findView() {
        back_ib = f(R.id.back_ib);
        title_tv = f(R.id.title_tv);
        day_tv = f(R.id.day_tv);
        magnetometer_left_iv = f(R.id.magnetometer_left_iv);
        magnetometer_right_iv = f(R.id.magnetometer_right_iv);
        open_state_tv = f(R.id.open_state_tv);
        circle_outer_iv = f(R.id.circle_outer_iv);
        signal_iv = f(R.id.signal_iv);
        signal_tv = f(R.id.signal_tv);
        warning_iv = f(R.id.warning_iv);
        warning_tv = f(R.id.warning_tv);
        power_iv = f(R.id.power_iv);
        power_tv = f(R.id.power_tv);
        detail_ll = f(R.id.detail_ll);
        history_ll = f(R.id.history_ll);
    }

    @Override
    protected void doBusiness() {
        deviceId = getIntent().getStringExtra("DeviceId");
        animation = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(5000);

        moveDistance = DensityUtil.getScreenWidth(this) * 32 / 750;

        if ("B002".equals(deviceId.substring(0, 4))) {
            //红外
            magnetometer_left_iv.setImageResource(R.mipmap.infra);
            magnetometer_right_iv.setVisibility(View.GONE);
            open_state_tv.setText("- 监测中 -");
        } else {
            //门磁
            open_state_tv.setText("- 已关门 -");
        }
        presenter = new MagnetometerPresenter(this, this, deviceId);
    }

    @Override
    protected void setEvent() {
        back_ib.setOnClickListener(this);
        circle_outer_iv.setOnClickListener(this);
        detail_ll.setOnClickListener(this);
        history_ll.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_ib:
                finish();
                break;
            case R.id.circle_outer_iv:

                break;
            case R.id.detail_ll:
                if (presenter.getDetail() == null) {
                    ToastUtil.toastShort(this, "请稍后");
                    return;
                }
                Intent intent;
                if ("B002".equals(presenter.getDetail().getPid().substring(0, 4))) {
                    intent = new Intent(this, InfraDetailActivity.class);
                } else {
                    intent = new Intent(this, MagnetometerDetailActivity.class);
                }
                Bundle bundle = new Bundle();
                bundle.putParcelable("device", presenter.getDetail());
                intent.putExtras(bundle);
                startActivityForResult(intent, 100);
                break;
            case R.id.history_ll:
                Intent intent1 = new Intent(this, MagHistoryActivity.class);
                intent1.putExtra("deviceId", deviceId);
                startActivity(intent1);
                break;
        }
    }

    @Override
    public void onWifiClose() {
        magnetometer_left_iv.clearAnimation();
        magnetometer_right_iv.clearAnimation();
        TranslateAnimation animationLeft = new TranslateAnimation(-moveDistance, 0, 0, 0);
        TranslateAnimation animationRight = new TranslateAnimation(moveDistance, 0, 0, 0);
        animationLeft.setDuration(1500);
        animationRight.setDuration(1500);
        animationLeft.setFillAfter(true);
        animationRight.setFillAfter(true);
        magnetometer_left_iv.startAnimation(animationLeft);
        magnetometer_right_iv.startAnimation(animationRight);
        open_state_tv.setText("- 已关门 -");
    }

    @Override
    public void onWifiOpen() {
        magnetometer_left_iv.clearAnimation();
        magnetometer_right_iv.clearAnimation();
        TranslateAnimation animationLeft = new TranslateAnimation(0, -moveDistance, 0, 0);
        TranslateAnimation animationRight = new TranslateAnimation(0, moveDistance, 0, 0);
        animationLeft.setDuration(1500);
        animationRight.setDuration(1500);
        animationLeft.setFillAfter(true);
        animationRight.setFillAfter(true);
        magnetometer_left_iv.startAnimation(animationLeft);
        magnetometer_right_iv.startAnimation(animationRight);
        open_state_tv.setText("- 已开门 -");
    }

    @Override
    public void onTouch() {
        open_state_tv.setTextSize(16);
        open_state_tv.setTextColor(Color.RED);
        open_state_tv.setText("- 有人通过 -");
        mHandler.postDelayed(runnable, 5000);
    }

    @Override
    public void initViewData(MagDetail detail) {
        if ("B002".equals(deviceId.substring(0, 4))) {
            if ("智能门磁".equals(detail.getNickName()) || TextUtils.isEmpty(detail.getNickName())) {
                title_tv.setText("人体红外");
            } else {
                title_tv.setText(detail.getNickName());
            }
        } else {
            if (TextUtils.isEmpty(detail.getNickName())) {
                title_tv.setText("智能门磁");
            } else {
                title_tv.setText(detail.getNickName());
            }
        }

        //设置天数
        int day = (int) TimeUtil.getDay(detail.getTimeBind());
        int screenWidth = DensityUtil.getScreenWidth(this);
        int daySize = 30 * screenWidth / 750;
        AbsoluteSizeSpan as = new AbsoluteSizeSpan(daySize);
        String string = getString(R.string.gateway_day_bind, day);
        SpannableString day_bind = new SpannableString(string);
        ForegroundColorSpan bgSpan = new ForegroundColorSpan(getResources().getColor(R.color.day_bind));
        day_bind.setSpan(as, 2, 2 + String.valueOf(day).length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        day_bind.setSpan(bgSpan, 2, 2 + String.valueOf(day).length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        day_tv.setText(day_bind);

        //设置信号
        String signal = detail.getSignalStrength() == null ? "100" : detail.getSignalStrength();
        int s = Integer.parseInt(signal, 16);
        if (s > 30) {
            signal_tv.setText("信号强");
            signal_iv.setImageResource(R.mipmap.signal_full);
        } else if (s > 15) {
            signal_tv.setText("信号中");
            signal_iv.setImageResource(R.mipmap.signal_common);
        } else {
            signal_tv.setText("信号弱");
            signal_iv.setImageResource(R.mipmap.signal_low);
        }

        //设置电量
        String power = detail.getPower() == null ? "100" : detail.getPower();
        int p = Integer.parseInt(power, 16);
        String powerInfo = "电量" + p + "%";
        if (p > 70) {
            power_iv.setImageResource(R.mipmap.power_4);
        } else if (p > 20) {
            power_iv.setImageResource(R.mipmap.power_3);
        } else if (p > 10) {
            power_iv.setImageResource(R.mipmap.power_2);
        } else {
            power_iv.setImageResource(R.mipmap.power_1);
        }
        power_tv.setText(powerInfo);

        //设置设备是否警戒
        int isCall = detail.getIsCall();
        warning_iv.setImageResource(isCall == 0 ? R.mipmap.warning_close : R.mipmap.warning_open);
        warning_tv.setText(isCall == 0 ? "未警戒" : "已警戒");
    }

    @Override
    protected void onResume() {
        super.onResume();
        circle_outer_iv.startAnimation(animation);
        presenter.getDeviceInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        animation.cancel();
        presenter.onDestroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
