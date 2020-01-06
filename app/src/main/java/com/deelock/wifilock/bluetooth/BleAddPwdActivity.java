package com.deelock.wifilock.bluetooth;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.deelock.wifilock.R;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.databinding.ActivityBleAddPwdBinding;
import com.deelock.wifilock.utils.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;

import static com.mob.MobSDK.getContext;

//TODO 绑定成功之后跳转的管理员密码界面
public class BleAddPwdActivity extends BaseActivity<ActivityBleAddPwdBinding> {

    List<Button> buttons;
    char[] password;
    private boolean isFromBind;
    private boolean isShortPw;
    private String deviceId;
    private String authId;
    private String type;
    private String mac;
    private String shareName;
    private long start_time, end_time;
    private boolean isUpdate, isFollow;
    private String openName;
    private String pid;

    @Override
    protected int initLayout() {
        return R.layout.activity_ble_add_pwd;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        StatusBarUtil.StatusBarLightMode(this);
        isFromBind = getIntent().getBooleanExtra("isFromBind", false);
        isShortPw = getIntent().getBooleanExtra("isShortPw", false);
        isUpdate = getIntent().getBooleanExtra("isUpdate", false);
        start_time = getIntent().getLongExtra("start", 0L);
        end_time = getIntent().getLongExtra("end", 0L);
        deviceId = getIntent().getStringExtra("deviceId");
        shareName = getIntent().getStringExtra("shareName");
        authId = getIntent().getStringExtra("authId");
        type = getIntent().getStringExtra("type");
        mac = getIntent().getStringExtra("mac");
        isFollow = getIntent().getBooleanExtra("isFollow", false);
        openName = getIntent().getStringExtra("openName");
        pid = getIntent().getStringExtra("pid");

        password = new char[6];
        buttons = new ArrayList<>();
        buttons.add(binding.bleAddPwd1);
        buttons.add(binding.bleAddPwd2);
        buttons.add(binding.bleAddPwd3);
        buttons.add(binding.bleAddPwd4);
        buttons.add(binding.bleAddPwd5);
        buttons.add(binding.bleAddPwd6);
        buttons.add(binding.bleAddPwd7);
        buttons.add(binding.bleAddPwd8);
        buttons.add(binding.bleAddPwd9);
        buttons.add(binding.bleAddPwd0);
        buttons.add(binding.bleAddPwdC);

        if (isFromBind) {
            binding.bleAddPwdTitle.setText("管理员密码");
        } else {
            binding.bleAddPwdTitle.setText("添加密码");
            binding.bleAddPwdTips.setText("随机产生");
            binding.bleAddPwdTips.setTextColor(getResources().getColor(R.color.device_state));
            binding.bleAddPwdTips.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shengchengsuijishu();
                }
            });
        }

        binding.bleAddPwdBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        AssetManager mgr = getContext().getAssets();
        Typeface tf = Typeface.createFromAsset(mgr, "fonts/BAUHAUS_0.TTF");

        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setTypeface(tf);
            buttons.get(i).setOnClickListener(listener);
        }

        binding.bleAddPwdX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 5; i > -1; i--) {
                    if (password[i] == '\0') {
                        continue;
                    }
                    password[i] = '\0';
                    binding.bleAddPwdPa.setText(password);
                    return;
                }
            }
        });

        binding.bleAddPwdSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 6; i++) {
                    if (password[i] == '\0') {
                        return;
                    }
                }
                binding.bleAddPwdPa.setText(password);
                Bundle bundle = new Bundle();
                bundle.putBoolean("isFromBind", isFromBind);
                bundle.putString("pwd", String.valueOf(password));
                bundle.putBoolean("isShortPw", isShortPw);
                bundle.putString("deviceId", deviceId);
                bundle.putLong("start_time", start_time);
                bundle.putLong("end_time", end_time);
                bundle.putBoolean("isUpdate", isUpdate);
                bundle.putString("shareName", shareName);
                bundle.putString("authId", authId);
                bundle.putString("type", type);
                bundle.putString("mac", mac);
                bundle.putBoolean("isFollow", isFollow);
                bundle.putString("openName", openName);
                bundle.putString("pid", pid);
                //TODO 首次添加管理员密码蓝牙初使化,等待10秒发送
                bundle.putString("pass", "1");
                openView(BlePwdNameActivity.class, bundle);
                finish();
                password = new char[]{'\0', '\0', '\0', '\0', '\0', '\0'};
            }
        });
    }

    /**
     * 生成随机数
     */
    private void shengchengsuijishu() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                password = new char[]{'\0', '\0', '\0', '\0', '\0', '\0'};
//                int a = (int) (Math.random() * 10);
//                //未完待续
//            }
//        }).start();
        int a = (int) (Math.random() * (999999 - 100000) + 100000);
        password = String.valueOf(a).toCharArray();
        binding.bleAddPwdPa.setText(password);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.buttonC_btn) {
                password = new char[]{'\0', '\0', '\0', '\0', '\0', '\0'};
                binding.bleAddPwdPa.setText(password);
                return;
            }
            for (int i = 0; i < 6; i++) {
                if (password[i] != '\0') {
                    continue;
                }
                switch (v.getId()) {
                    case R.id.ble_add_pwd_0:
                        password[i] = '0';
                        break;
                    case R.id.ble_add_pwd_1:
                        password[i] = '1';
                        break;
                    case R.id.ble_add_pwd_2:
                        password[i] = '2';
                        break;
                    case R.id.ble_add_pwd_3:
                        password[i] = '3';
                        break;
                    case R.id.ble_add_pwd_4:
                        password[i] = '4';
                        break;
                    case R.id.ble_add_pwd_5:
                        password[i] = '5';
                        break;
                    case R.id.ble_add_pwd_6:
                        password[i] = '6';
                        break;
                    case R.id.ble_add_pwd_7:
                        password[i] = '7';
                        break;
                    case R.id.ble_add_pwd_8:
                        password[i] = '8';
                        break;
                    case R.id.ble_add_pwd_9:
                        password[i] = '9';
                        break;
                    default:
                        break;
                }
                binding.bleAddPwdPa.setText(password);
                return;
            }
        }
    };

}
