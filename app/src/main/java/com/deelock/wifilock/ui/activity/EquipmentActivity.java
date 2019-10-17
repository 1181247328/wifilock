package com.deelock.wifilock.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

import com.deelock.wifilock.R;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.constants.Constants;
import com.deelock.wifilock.databinding.ActivityEquipmentBinding;
import com.deelock.wifilock.entity.FPrintList;
import com.deelock.wifilock.entity.LockDetail;
import com.deelock.wifilock.entity.LockState;
import com.deelock.wifilock.entity.Message;
import com.deelock.wifilock.entity.PushList;
import com.deelock.wifilock.entity.UserFPrint;
import com.deelock.wifilock.event.EquipmentEvent;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.dialog.AddInfoDialog;
import com.deelock.wifilock.ui.dialog.PrintDialog;
import com.deelock.wifilock.utils.DensityUtil;
import com.deelock.wifilock.utils.GsonUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class EquipmentActivity extends BaseActivity<ActivityEquipmentBinding>
        implements AddInfoDialog.InfoClickListener, EquipmentEvent {

    private LockState lockState;
    //private String lock_type;
    private AddInfoDialog addInfoDialog;
    private PrintDialog printDialog;
    private FingerprintManager manager;
    private LockDetail lockDetail = null;
    private Intent mIntent;
    private Disposable disposable;
    private CompositeDisposable comDisposable;
    private boolean canAddPassword = false;   //是否可以添加密码
    private boolean isSafe = true;
    private RotateAnimation animation;

    @Override
    protected int initLayout() {
        return R.layout.activity_equipment;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        binding.setEvent(this);
        comDisposable = new CompositeDisposable();
        lockState = getIntent().getParcelableExtra("lockState");
        //lock_type = lockState.getPid().substring(0, 3);
        addInfoDialog = new AddInfoDialog(this, R.style.mydialog);
        //addInfoDialog.setCurrentView(lock_type);
        addInfoDialog.setInfoListener(this);
        printDialog = new PrintDialog(this);
        binding.titleTv.setText(lockState.getNickName());
        int isOnline = lockState.getIsOnline();
        if (isOnline == 0) {
            binding.safeTv.setText("设备已离线");
            binding.safeTv.setTextColor(0xffff8239);
            binding.waterWv.setNetState(false);
        } else {
            binding.safeTv.setText("安全守护中");
            binding.safeTv.setTextColor(0xff43e3c9);
            binding.waterWv.setNetState(true);
        }
        int screenWidth = DensityUtil.getScreenWidth(this);
        int daySize = 30 * screenWidth / 750;
        String day = String.valueOf(TimeUtil.getDay(lockState.getTimeBind()));
        String time = new StringBuffer("第  天").insert(2, day).toString();
        SpannableStringBuilder sb = new SpannableStringBuilder(time);
        AbsoluteSizeSpan as = new AbsoluteSizeSpan(daySize);
        sb.setSpan(as, 2, 2 + day.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(0xff4c4c4c), 2, 2 + day.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        binding.circleOuterIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSafe) {
                    Intent intent = new Intent("com.deelock.wifilock.unSafe");
                    intent.putExtra("type", 1);
                    LocalBroadcastManager.getInstance(EquipmentActivity.this).sendBroadcast(intent);
                    finish();
                }
            }
        });
        binding.dayTv.setText(sb);
        requestMessage();
    }

    private void setAnimation() {
        animation = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(5000);
        binding.circleOuterIv.setAnimation(animation);
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestFprint();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setAnimation();
        requestDetailInfo();
        if (lockState.getIsOnline() == 1) {
            Observable.interval(5, TimeUnit.SECONDS).subscribe(new Observer<Long>() {
                @Override
                public void onSubscribe(Disposable d) {
                    disposable = d;
                }

                @Override
                public void onNext(Long aLong) {
                    requestMessage();
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {
                    comDisposable.clear();
                }
            });
            comDisposable.add(disposable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        comDisposable.clear();
        if (animation != null) {
            animation.cancel();
            animation = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.waterWv.setCloseView();
    }

    /**
     * 请求门锁详情
     */
    private void requestDetailInfo() {
        HashMap<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params.put("uid", SPUtil.getUid(this));
        params.put("pid", lockState.getPid());
        RequestUtils.request(RequestUtils.LOCK_DETAIL_INFO, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        lockDetail = GsonUtil.json2Bean(content, LockDetail.class);
                        binding.titleTv.setText(lockDetail.getNickName());
                        int signal;
                        if (lockDetail.getSignalStrength() == null) {
                            signal = 100;
                        } else {
                            signal = Integer.parseInt(lockDetail.getSignalStrength(), 16);
                        }
                        if (signal > 30) {
                            binding.signalTv.setText("强");
                            binding.signalIv.setImageResource(R.mipmap.signal_full);
                        } else if (signal > 15) {
                            binding.signalTv.setText("中");
                            binding.signalIv.setImageResource(R.mipmap.signal_common);
                        } else {
                            binding.signalTv.setText("弱");
                            binding.signalIv.setImageResource(R.mipmap.signal_low);
                        }
                        if (!TextUtils.isEmpty(lockDetail.getPower())) {
                            int power = Integer.parseInt(lockDetail.getPower(), 16);
                            binding.powerTv.setText(getString(R.string.power_notify, power));
                            if (power < 70 && power >= 20) {
                                binding.batteryIv.setImageResource(R.mipmap.power_3);
                            } else if (power < 20 && power >= 10) {
                                binding.batteryIv.setImageResource(R.mipmap.power_2);
                            } else if (power < 10) {
                                binding.batteryIv.setImageResource(R.mipmap.power_1);
                            }
                        }
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        comDisposable.clear();
                    }
                }
        );
    }

    /**
     * 请求门锁所有指纹
     */
    private void requestFprint() {
        HashMap<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", lockState.getPid());
        RequestUtils.request(RequestUtils.ALL_FPRINT, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        FPrintList printList = GsonUtil.json2Bean(content, FPrintList.class);
                        List<UserFPrint> userFPrints = printList.getList();
                        canAddPassword = userFPrints.size() != 0;
                    }
                }
        );
    }

    /**
     * 请求当前账号下所有设备的消息
     */
    private void requestMessage() {
        HashMap<String, String> params1 = new HashMap<>();
        params1.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params1.put("uid", SPUtil.getUid(this));
        params1.put("count", String.valueOf(100));
        params1.put("minTime", String.valueOf(-1));
        params1.put("maxTime", String.valueOf(-1));
        RequestUtils.request(RequestUtils.MESSAGE, this, params1).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        PushList pushList = GsonUtil.json2Bean(content, PushList.class);
                        List<Message> messages = pushList.getList();
                        for (Message m : messages) {
                            if (lockState.getPid().equals(m.getDevId())) {
                                isSafe = false;
                                binding.safeTv.setText("存在风险");
                                binding.safeTv.setTextColor(0xffff8239);
                                binding.circleInnerIv.setImageResource(R.mipmap.circle_inner_unsafe);
                                binding.circleOuterIv.setImageResource(R.mipmap.circle_outer_unsafe);
                                binding.waterWv.setNetState(false);
                                return;
                            }
                        }
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        comDisposable.dispose();
                    }
                }
        );
    }

    /**
     * 添加密码
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void addPwd() {
        addInfoDialog.dismiss();
        //if ("A001".equals(lock_type)) {
        if (lockDetail.getIsAllowPwd() == 0) {
            ToastUtil.toastShort(this, "请在门锁上进行开启远程下发密码操作");
            return;
        }
        if (!canAddPassword) {
            ToastUtil.toastShort(getApplicationContext(), "请先添加指纹");
        } else {
            mIntent = new Intent(EquipmentActivity.this, AddPasswordActivity.class);
            mIntent.putExtra("sdlId", lockState.getPid());
            if (!Constants.isVerifiedGesture) {
                checkFingerPrint(mIntent);
            } else {
                startActivity(mIntent);
            }
        }
        // }
//        } else if ("A002".equals(lock_type)) {
//            Intent intent = new Intent(this, HsCreateActivity.class);
//            intent.putExtra("sdlId", lockState.getPid());
//            startActivity(intent);
//        }
    }

    /**
     * 添加指纹
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void addFprint() {
        addInfoDialog.dismiss();
//        if ("A002".equals(lock_type)) {
//            Intent intent = new Intent(EquipmentActivity.this, PasswordStepActivity.class);
//            intent.putExtra("sdlId", lockState.getPid());
//            intent.putExtra("flag", 4);
//            intent.putExtra("authId", "11111111111111111111111111111111");
//            startActivity(intent);
//        } else if ("A001".equals(lock_type)) {
        mIntent = new Intent(EquipmentActivity.this, AddFPrintActivity.class);
        mIntent.putExtra("sdlId", lockState.getPid());
        if (!Constants.isVerifiedGesture) {
            checkFingerPrint(mIntent);
        } else {
            startActivity(mIntent);
        }
//        }
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkFingerPrint(Intent intent) {
        boolean hardwareDetected = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                manager = (FingerprintManager) this.getSystemService(Context.FINGERPRINT_SERVICE);
                if (manager != null) {
                    hardwareDetected = manager.isHardwareDetected();
                }
                if (!hardwareDetected) {
                    startActivity(intent);
                    return;
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_FINGERPRINT},
                            150);
                } else {
                    useFingerPrint(intent);
                }

            } catch (Exception e) {
                startActivity(intent);
            }
        } else {
            startActivity(intent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void useFingerPrint(final Intent intent) {
        if (!manager.hasEnrolledFingerprints()) {
            Toast.makeText(this, "没有录入指纹", Toast.LENGTH_SHORT).show();
            startActivity(intent);
            return;
        }
        printDialog.setEvent(new PrintDialog.Event() {
            @Override
            public void select() {
                printDialog.dismiss();
                startActivity(intent);
            }
        });
        printDialog.show();
        CancellationSignal mCancellationSignal = new CancellationSignal();
        FingerprintManager.AuthenticationCallback mSelfCancelled = new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                //但多次指纹密码验证错误后，进入此方法；并且，不能短时间内调用指纹验证
                Toast.makeText(EquipmentActivity.this, errString, Toast.LENGTH_SHORT).show();
                printDialog.dismiss();
                startActivity(intent);
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                Toast.makeText(EquipmentActivity.this, helpString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                Toast.makeText(EquipmentActivity.this, "指纹识别成功", Toast.LENGTH_SHORT).show();
                Constants.isVerifiedGesture = true;
                startActivity(intent);
                printDialog.dismiss();
            }

            @Override
            public void onAuthenticationFailed() {
                printDialog.setNotice("请再试一次");
            }
        };
        manager.authenticate(null, mCancellationSignal, 0, mSelfCancelled, null);
    }

    /**
     * 添加NFC卡片
     */
    @Override
    public void addCard() {
        //下一版本实现
    }

    /**
     * 退出当前界面
     */
    @Override
    public void back() {
        finish();
    }

    /**
     * 点击添加按钮（添加指纹、密码）
     */
    @Override
    public void add() {
        addInfoDialog.show();
    }

    /**
     * 点击用户管理
     */
    @Override
    public void manageAccount() {
        Intent intent = new Intent(this, UserManageActivity.class);
        intent.putExtra("sdlId", lockState.getPid());
        startActivity(intent);
    }

    /**
     * 点击设置按钮
     */
    @Override
    public void detail() {
        if (lockDetail == null) {
            return;
        }
        Intent intent;
//        if ("A002".equals(lock_type)) {
//            intent = new Intent(this, HsDetailActivity.class);
//            intent.putExtra("lockDetail", lockDetail);
//            intent.putExtra("sdlId", lockState.getPid());
//        } else {
        intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra("devId", lockState.getPid());
        intent.putExtra("nickname", lockDetail.getNickName());
        intent.putExtra("hardVersion", lockDetail.getHardVersion());
        intent.putExtra("softVersion", lockDetail.getSoftVersion());
        intent.putExtra("wifiName", lockDetail.getSsid());
        intent.putExtra("remotePw", lockDetail.getIsAllowPwd());
//        }
        startActivityForResult(intent, 2);
    }

    /**
     * 点击历史记录按钮
     */
    @Override
    public void history() {
        if (lockDetail != null) {
            Intent intent = new Intent(this, HistoryActivity.class);
            intent.putExtra("sdlId", lockState.getPid());
            startActivity(intent);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @android.support.annotation.NonNull
            String[] permissions, @android.support.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 150) {
            if (permissions[0].equals(Manifest.permission.USE_FINGERPRINT)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    useFingerPrint(mIntent);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        String name = data.getStringExtra("name");
        if (requestCode == 2) {
            if (!TextUtils.isEmpty(name)) {
                binding.titleTv.setText(name);
            }
        }
    }

}