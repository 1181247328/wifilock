package com.deelock.wifilock.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.deelock.bleradio.DeelockRadio;
import com.deelock.wifilock.R;
import com.deelock.wifilock.adapter.ViewPagerFragmentAdapter;
import com.deelock.wifilock.databinding.ActivityMainBinding;
import com.deelock.wifilock.entity.Login;
import com.deelock.wifilock.entity.Version;
import com.deelock.wifilock.event.MainEvent;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.fragment.EquipmentFragment;
import com.deelock.wifilock.ui.fragment.MessageFragment;
import com.deelock.wifilock.ui.fragment.MineFragment;
import com.deelock.wifilock.utils.DensityUtil;
import com.deelock.wifilock.utils.DownloadAppUtils;
import com.deelock.wifilock.utils.GsonUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppActivity {

    private String TAG = MainActivity.class.getSimpleName();

    ActivityMainBinding binding;
    ViewPagerFragmentAdapter adapter;

    private final int LEFT = 0;
    private final int CENTER = 1;
    private final int RIGHT = 2;

    private int lastFragment = 1;

    ScaleAnimation scaleAnimation;

    LocalBroadcastManager broadcastManager;
    BroadcastReceiver receiver;

    SimpleDateFormat sf;

    Version version;
    double apkSize;
    String apkUrl;
    AlertDialog dialog;
    int REQUESTPERMISSION = 110;

    @Override
    protected void bindActivity() {
        intentFilter();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    /**
     * 监听蓝牙的广播(动态注册)
     */
    private void intentFilter() {
        //实例化广播接收者
        DeelockRadio deelockRadio = new DeelockRadio();
        //打开的扭带
        IntentFilter filter = new IntentFilter();
        //要监听的状态(这里是蓝牙的开关)
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //开启广播接收者
        registerReceiver(deelockRadio, filter);
    }

    @Override
    protected void doBusiness() {
        MessageFragment messageFragment = new MessageFragment();
        EquipmentFragment equipmentFragment = new EquipmentFragment();
        MineFragment mineFragment = new MineFragment();
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(messageFragment);
        fragments.add(equipmentFragment);
        fragments.add(mineFragment);

        adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragments);
        binding.vp.setAdapter(adapter);
        binding.vp.setCurrentItem(1, false);

        scaleAnimation = new ScaleAnimation(0.5f, 1.25f, 0.5f, 1.25f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                if (input < 0.75f) {
                    return input / 0.75f;
                } else {
                    return 1 - (input - 0.75f) / 0.75f;
                }
            }
        });
        scaleAnimation.setDuration(260);

        broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter("com.deelock.wifilock.unSafe");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int type = intent.getIntExtra("type", 0);
                if (type == 1) {
                    if (lastFragment != LEFT) {
                        showFragment(LEFT);
                    }
                }
            }
        };
        broadcastManager.registerReceiver(receiver, intentFilter);

        boolean isShowNotify = SPUtil.getIsShowNotify(this);  //通知是否显示，false为不再显示
        if (!isShowNotify) {
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            boolean enabled = managerCompat.areNotificationsEnabled();   //应用是否拥有通知权限，4.4以下默认返回true
            if (!enabled) {
                new MaterialDialog.Builder(this)
                        .title(R.string.app_name)
                        .content(R.string.main_notify)
                        .checkBoxPromptRes(R.string.no_more_display, false, new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                SPUtil.setIsShowNotify(MainActivity.this, isChecked);
                            }
                        })
                        .canceledOnTouchOutside(false)
                        .positiveText(R.string.sure)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@android.support.annotation.NonNull MaterialDialog dialog,
                                                @android.support.annotation.NonNull DialogAction which) {
                                Intent intent = new Intent();
                                intent.setAction
                                        (Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.addCategory(Intent.CATEGORY_DEFAULT);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .negativeText(R.string.cancel)
                        .show();
            }
        }

        int isNeedKey = SPUtil.getIntData(this, "main_test");
        if (isNeedKey != 2 && isNeedKey != -1) {
            handler.sendEmptyMessageDelayed(101, 300);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            boolean jpushDid = TextUtils.isEmpty(JPushInterface.getRegistrationID(getApplicationContext()));
            if (jpushDid) {
                sendEmptyMessageDelayed(101, 1000);
            } else {
                login();
            }
        }
    };

    private void login() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("phoneNumber", SPUtil.getPhoneNumber(MainActivity.this));
        String mainKey = SPUtil.getStringData(MainActivity.this, "main_key");
        String aes = mainKey.substring(0, mainKey.indexOf("AES"));
        params.put("pwd", aes);
        params.put("did", "10" + JPushInterface.getRegistrationID(getApplicationContext()));
        params.put("height", DensityUtil.getScreenHeight(MainActivity.this));
        params.put("width", DensityUtil.getScreenWidth(MainActivity.this));
        params.put("model", android.os.Build.MODEL);
        RequestUtils.requestUnLogged(RequestUtils.LOGIN, MainActivity.this, params).enqueue(
                new ResponseCallback<BaseResponse>(MainActivity.this) {

                    @Override
                    protected void onSuccess(int code, final String content) {
                        super.onSuccess(code, content);
                        SPUtil.saveData(MainActivity.this, "main_test", -1);
                        SPUtil.saveData(MainActivity.this, "main_key", "");
                        Login login = GsonUtil.json2Bean(content, Login.class);
                        SPUtil.setLoginInfo(MainActivity.this, login);
                    }
                }
        );
    }

    @Override
    protected void requestData() {
        sf = new SimpleDateFormat("yyyyMMdd");
        String date = sf.format(System.currentTimeMillis());
        if (Integer.parseInt(date.substring(6, 8)) % 2 == 0) {
            if (SPUtil.getDailyCheck(this, date)) {
                final int versionCode = getLockVersionCode();
                Map params = new HashMap();
                params.put("token", SPUtil.getToken(this));
                params.put("timestamp", TimeUtil.getTime());
                params.put("uid", SPUtil.getUid(this));
                params.put("type", 10);
                params.put("versionCount", versionCode);
                RequestUtils.request(RequestUtils.VERSION, this, params).enqueue(
                        new ResponseCallback<BaseResponse>(this) {
                            @SuppressLint("CheckResult")
                            @Override
                            protected void onSuccess(int code, String content) {
                                super.onSuccess(code, content);
                                if (content.length() < 5) {
                                    return;
                                }
                                String r = content;
                                String u = null;
                                if (content.indexOf("http") != -1) {
                                    u = content.substring(content.indexOf("http"), content.length() - 2);
                                    apkUrl = u;
                                }
                                version = new Gson().fromJson(r, Version.class);
                                if (version.getVersionCount() > versionCode) {
                                    final String finalU = u;
                                    Observable.create(new ObservableOnSubscribe<Double>() {
                                        @Override
                                        public void subscribe(@NonNull ObservableEmitter<Double> e) throws Exception {
                                            HttpURLConnection conn = null;
                                            URL url = null;
                                            try {
                                                url = new URL(finalU);
                                                conn = (HttpURLConnection) url.openConnection();
                                                conn.setRequestProperty("Accept-Encoding", "identity");
                                                conn.setConnectTimeout(10000);
                                                conn.connect();
                                            } catch (MalformedURLException ee) {
                                                ee.printStackTrace();
                                            } catch (IOException eee) {
                                                eee.printStackTrace();
                                            }
                                            apkSize = conn.getContentLength() / 1048576.0;
                                            conn.disconnect();
                                            if (apkSize > 0) {
                                                e.onNext(apkSize);
                                            }
                                        }
                                    }).subscribeOn(Schedulers.newThread())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Consumer<Double>() {
                                                @Override
                                                public void accept(Double aDouble) throws Exception {
                                                    createDialog();
                                                }
                                            });
                                }
                            }
                        }
                );
            }
        }
    }

    @Override
    protected void setEvent() {
        binding.setEvent(new MainEvent() {
            @Override
            public void clickLeft() {
                if (lastFragment != LEFT) {
                    showFragment(LEFT);
                }
            }

            @Override
            public void clickCenter() {
                if (lastFragment != CENTER) {
                    showFragment(CENTER);
                }
            }

            @Override
            public void clickRight() {
                if (lastFragment != RIGHT) {
                    showFragment(RIGHT);
                }
            }
        });
    }

    private int getLockVersionCode() {
        // 获取packagemanager的实例
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int version = packInfo.versionCode;
        Log.e("main_版本", version + "");
        return version;
    }

    private void createDialog() {
        showDialog(0);
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(dialog);
            Field negativeButton = mAlertController.getClass().getDeclaredField("mButtonNegative");
            Field positionButton = mAlertController.getClass().getDeclaredField("mButtonPositive");
            negativeButton.setAccessible(true);
            positionButton.setAccessible(true);
            Button button1 = (Button) negativeButton.get(mAlertController);
            Button button2 = (Button) positionButton.get(mAlertController);
            button1.setTextColor(0xff717171);
            button2.setTextColor(0xff01bff2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void showFragment(int fragment) {
        switch (lastFragment) {
            case LEFT:
                binding.messageIv.setImageResource(R.mipmap.message_icon_gray);
                binding.messageTv.setTextColor(0xff777777);
                break;
            case CENTER:
                binding.equipmentIv.setImageResource(R.mipmap.equipment_icon_gray);
                binding.equipmentTv.setTextColor(0xff777777);
                break;
            case RIGHT:
                binding.mineIv.setImageResource(R.mipmap.mine_icon_gray);
                binding.mineTv.setTextColor(0xff777777);
                break;
            default:
                break;
        }

        switch (fragment) {
            case LEFT:
                binding.vp.setCurrentItem(0, false);
                binding.messageIv.setImageResource(R.mipmap.message_icon);
                binding.messageTv.setTextColor(0xff333333);
                binding.messageLl.startAnimation(scaleAnimation);
                break;
            case CENTER:
                binding.vp.setCurrentItem(1, false);
                binding.equipmentIv.setImageResource(R.mipmap.equipment_icon);
                binding.equipmentTv.setTextColor(0xff333333);
                binding.equipmentLl.startAnimation(scaleAnimation);
                break;
            case RIGHT:
                binding.vp.setCurrentItem(2, false);
                binding.mineIv.setImageResource(R.mipmap.mine_icon);
                binding.mineTv.setTextColor(0xff333333);
                binding.mineLl.startAnimation(scaleAnimation);
                break;
            default:
                break;
        }
        lastFragment = fragment;
    }

    private long exitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finishAffinity();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void askPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUESTPERMISSION);
        } else {
            DownloadAppUtils.downloadForWebView(MainActivity.this, apkUrl);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUESTPERMISSION) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    DownloadAppUtils.downloadForWebView(getApplicationContext(), apkUrl);
                } else {
                    //提示没有权限，安装不了咯
                }
            }
        }
    }

    @Override
    protected AlertDialog onCreateDialog(int id) {
        DecimalFormat df = new DecimalFormat(".##");
        Double d = Double.valueOf(df.format(apkSize));
        String title = "新版本 V" + version.getVersion();
        String message = "安装包大小 " + d + "MB.\n" + version.getMessage().replace("；", "；\n");
        dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("立即下载",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                askPermission();
                            }
                        })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
        return dialog;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "---启动---");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "---恢复---");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "---暂停---");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "---停止---");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "---重启---");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        removePairDevice();
    }

    private static void removePairDevice() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
            mBluetoothAdapter.enable();
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
        if (mBluetoothAdapter != null) {
            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                unpairDevice(device);
            }
        }
    }

    private static void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e("蓝牙清除缓存异常", e.getMessage());
        }
    }
}
