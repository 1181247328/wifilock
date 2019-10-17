package com.deelock.wifilock.ui.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.deelock.wifilock.R;
import com.deelock.wifilock.adapter.ViewPagerFragmentAdapter;
import com.deelock.wifilock.bluetooth.BleAddFprintActivity;
import com.deelock.wifilock.databinding.ActivityStepBinding;
import com.deelock.wifilock.entity.CircleState;
import com.deelock.wifilock.entity.FPrintList;
import com.deelock.wifilock.entity.Order;
import com.deelock.wifilock.entity.Pid;
import com.deelock.wifilock.entity.UserFPrint;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.overwrite.FixedSpeedScroller;
import com.deelock.wifilock.ui.dialog.NoticeDialog;
import com.deelock.wifilock.ui.dialog.ProgressFragmentDialog;
import com.deelock.wifilock.ui.fragment.InputInfoFragment;
import com.deelock.wifilock.ui.fragment.InputPrintFragment;
import com.deelock.wifilock.ui.fragment.LinkLockFragment;
import com.deelock.wifilock.ui.fragment.PrintSucceedFragment;
import com.deelock.wifilock.ui.fragment.VerifyManagerFragment;
import com.deelock.wifilock.utils.BluetoothUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by binChuan on 2017\9\25 0025.
 */

public class FPrintStepActivity extends AppActivity {

    ActivityStepBinding binding;
    ViewPagerFragmentAdapter adapter;
    List<Fragment> fragments;
    InputInfoFragment inputInfoFragment;
    LinkLockFragment linkLockFragment;
    VerifyManagerFragment verifyManagerFragment;
    InputPrintFragment inputPrintFragment;
    PrintSucceedFragment printSucceedFragment;
    FixedSpeedScroller scroller;

    List<UserFPrint> receivedData;

    int flag;
    String sdlId;
    String authId;
    int state;//轮询状态
    int manager;//管理员指纹个数
    boolean isFull;
    boolean operateAble = true;//是否获已经得管理员指纹
    Disposable disposable;
    String orderId;
    CompositeDisposable comDisposable = new CompositeDisposable();
    NoticeDialog dialog;
    private ProgressFragmentDialog pfd;
    private String mac;

    private boolean isFromBind;
    private int count;
    private int type;
    private long lastClickTime;
    private boolean needEvaluate;
    private boolean isLastOrder;
    private String name;

    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_step);
    }

    /**
     * flag : 1 添加新指纹 2 添加用户指纹
     */
    @Override
    protected void doBusiness() {
        flag = getIntent().getIntExtra("flag", 0);
        sdlId = getIntent().getStringExtra("sdlId");
        manager = getIntent().getIntExtra("manager", 0);
        authId = getIntent().getStringExtra("authId");
        isFull = getIntent().getBooleanExtra("isFull", false);
        isFromBind = getIntent().getBooleanExtra("isFromBind", false);
        needEvaluate = getIntent().getBooleanExtra("needEvaluate", false);
        name = getIntent().getStringExtra("name");
        mac = getIntent().getStringExtra("mac");

        fragments = new ArrayList<>();
        receivedData = new ArrayList<>();

        if (flag == 1) {
            inputInfoFragment = new InputInfoFragment();
            fragments.add(inputInfoFragment);
        }

        linkLockFragment = new LinkLockFragment();
        verifyManagerFragment = new VerifyManagerFragment();
        inputPrintFragment = new InputPrintFragment();
        printSucceedFragment = new PrintSucceedFragment();

        fragments.add(linkLockFragment);
        fragments.add(verifyManagerFragment);
        fragments.add(inputPrintFragment);
        fragments.add(printSucceedFragment);

        scroller = new FixedSpeedScroller(this);
        adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragments);
        binding.vp.setAdapter(adapter);
        setViewPagerScrollSpeed();
    }

    @Override
    protected void requestData() {
        if (!isNetworkAvailable()) {
            return;
        }

        Map params = new HashMap<>();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", sdlId);
        RequestUtils.request(RequestUtils.ALL_FPRINT, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        receivedData.addAll(new Gson().fromJson(content, FPrintList.class).getList());
                        manager = 0;
                        for (UserFPrint p : receivedData) {
                            if (p.getType() == 1) {
                                manager++;
                            }
                        }
                        linkLockFragment.limitView(receivedData.size(), manager);
                    }
                }
        );
    }

    @Override
    protected void setEvent() {
        if (flag == 1) {
            inputInfoFragment.setEvent(new InputInfoFragment.Event() {
                @Override
                public void next(String name, String number) {
                    authUser(name, number);
                }
            });
        }

        linkLockFragment.setEvent(new LinkLockFragment.Event() {
            @Override
            public void back() {
                if (isFromBind) {
                    Intent intent = new Intent(FPrintStepActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                finish();
            }

            @Override
            public void configure(int type) {
                if (!operateAble) {
                    return;
                }
                FPrintStepActivity.this.type = type;
                getOrderId(type);
            }
        });

        verifyManagerFragment.setEvent(new VerifyManagerFragment.Event() {
            @Override
            public void back() {
                binding.vp.setCurrentItem(binding.vp.getCurrentItem() - 1);
            }
        });

        inputPrintFragment.setEvent(new InputPrintFragment.Event() {
            @Override
            public void back() {
                binding.vp.setCurrentItem(binding.vp.getCurrentItem() - 1);
            }
        });

        printSucceedFragment.setEvent(new PrintSucceedFragment.Event() {
            @Override
            public void save(String name) {
                if (!clickAble) {
                    return;
                }
                clickAble = false;
                addPrint(name);
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        linkLockFragment.startAnima();
    }

    /**
     * 获取轮询状态
     */
    private void initCircle() {
        count = 0;
        Observable.interval(0, 2, TimeUnit.SECONDS).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(@NonNull Long aLong) {
                count++;
                if (count > 24) {
                    Order order = new Order();
                    SPUtil.setEntity(FPrintStepActivity.this, "order", order);
                    comDisposable.clear();
                    dismissDialog();
                    return;
                }
                getCircleState();
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
        comDisposable.add(disposable);
    }

    /**
     * 请求增加授权用户
     *
     * @param name
     * @param number
     */
    private void authUser(String name, String number) {
        if (!isNetworkAvailable()) {
            return;
        }

        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", sdlId);
        params.put("name", name);
        params.put("isPush", 1);
        if (!TextUtils.isEmpty(number)) {
            params.put("phoneNumber", number);
        }
        String url;
//        if ("A003".equals(sdlId.substring(0, 4))) {
//            url = RequestUtils.ADD_AUTH_USER_1;
//        } else {
//            url = RequestUtils.ADD_AUTH_USER;
//        }
        url = RequestUtils.ADD_AUTH_USER;
        RequestUtils.request(url, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        authId = new Gson().fromJson(content, Pid.class).getPid();
                        if (isFull) {
                            ToastUtil.toastShort(getApplicationContext(), "指纹个数已达到上限");
                            finish();
                            return;
                        }
                        if (mac != null) {
                            Intent intent = new Intent(FPrintStepActivity.this, BleAddFprintActivity.class);
                            intent.putExtra("authId", authId);
                            intent.putExtra("mac", mac);
                            intent.putExtra("deviceId", sdlId);
                            startActivity(intent);
                            finish();
                        } else {
                            binding.vp.setCurrentItem(1);
                        }
                    }
                });
    }

    /**
     * 获取轮询编号 如果上次获取的orderId未到1分钟，则继续发送刚才的orderId
     *
     * @param type
     */
    private void getOrderId(int type) {

        if (!isNetworkAvailable()) {
            return;
        }

        isLastOrder = false;
        Order o = SPUtil.getEntity(this, "order", Order.class);
        if (o == null || o.getAuthId() == null) {
            requestOrderId(type);
            return;
        }
        String aId = o.getAuthId();
        orderId = o.getOrderId();
        long time = o.getTime();
        if (!aId.equals(authId) && System.currentTimeMillis() / 1000L - time < 47) {
            authId = aId;
            showIsContinueLast(o.getName());
            return;
        }
        if (System.currentTimeMillis() / 1000L - time >= 47) {
            requestOrderId(type);
            return;
        }
        isLastOrder = true;
        showDialog();
        initCircle();
    }

    private void showIsContinueLast(String name) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("正在为" + name + "添加指纹，是否继续？")
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isLastOrder = true;
                                showDialog();
                                initCircle();
                            }
                        });
        builder.show();
    }

    private void requestOrderId(int type) {
        showDialog();
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", sdlId);
        params.put("type", type);
        params.put("authId", authId);
        RequestUtils.request(RequestUtils.ORDER_ID, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        String key = content.substring(2, 9);
                        if (key.equals("orderId")) {
                            Order order = new Order();
                            order.setOrderId(content.substring(12, content.length() - 2));
                            order.setTime(TimeUtil.getTime());
                            order.setAuthId(authId);
                            orderId = order.getOrderId();
                            order.setTime(System.currentTimeMillis() / 1000L);
                            order.setName(name);
                            initCircle();
                            isLastOrder = false;
                            SPUtil.setEntity(FPrintStepActivity.this, "order", order);
                        }
                    }

                    @Override
                    protected void onFailure(BaseResponse response) {
                        super.onFailure(response);
                        isLastOrder = false;
                        if (response.code == -203) {
                            ToastUtil.toastShort(getApplicationContext(), "配置超时");
                            dismissDialog();
                        }
                    }
                });
    }

    /**
     * 获取轮询状态
     */
    private void getCircleState() {
        if (!isNetworkAvailable()) {
            return;
        }
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("orderId", orderId);
        RequestUtils.request(RequestUtils.CIRCLE_STATE, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        int s = new Gson().fromJson(content, CircleState.class).getState();
                        if (s != state) {
                            state = s;
                            Order order = SPUtil.getEntity(FPrintStepActivity.this, "order", Order.class);
                            switch (state) {
                                case -4:
                                case -3:
                                case -2:
                                case -1:
                                    comDisposable.clear();
                                    dismissDialog();
                                    if (flag == 1) {
                                        binding.vp.setCurrentItem(1);
                                    } else {
                                        binding.vp.setCurrentItem(0);
                                    }
                                    linkLockFragment.limitView(receivedData.size(), manager);
                                    ToastUtil.toastLong(FPrintStepActivity.this, "添加失败");
                                    order = new Order();
                                    SPUtil.setEntity(FPrintStepActivity.this, "order", order);
                                    break;
                                case 1:
                                    setDialogText("请输入第一次指纹");
                                    order.setTime(System.currentTimeMillis());
                                    SPUtil.setEntity(FPrintStepActivity.this, "order", order);
                                    if (receivedData.size() == 0) {
                                        setDialogText("请输入第一次指纹");
                                        inputPrintFragment.setStep(1);
                                        if (binding.vp.getCurrentItem() != fragments.size() - 2) {
                                            binding.vp.setCurrentItem(fragments.size() - 2);
                                        }
                                    } else {
                                        setDialogText("请输入管理员指纹");
                                        count = -10;
                                        if (binding.vp.getCurrentItem() != fragments.size() - 3) {
                                            binding.vp.setCurrentItem(fragments.size() - 3);
                                        }
                                    }
                                    count = 0;
                                    break;
                                case 2:
                                    order.setTime(System.currentTimeMillis());
                                    SPUtil.setEntity(FPrintStepActivity.this, "order", order);
                                    setDialogText("请输入第一次指纹");
                                    count = -10;
                                    inputPrintFragment.setStep(1);
                                    if (binding.vp.getCurrentItem() != fragments.size() - 2) {
                                        binding.vp.setCurrentItem(fragments.size() - 2);
                                    }
                                    break;
                                case 91:
                                    count = -8;
                                    order.setTime(System.currentTimeMillis());
                                    SPUtil.setEntity(FPrintStepActivity.this, "order", order);
                                    inputPrintFragment.setStep(2);
                                    setDialogText("请输入第二次指纹");
                                    if (binding.vp.getCurrentItem() != fragments.size() - 2) {
                                        binding.vp.setCurrentItem(fragments.size() - 2);
                                    }
                                    break;
                                case 92:
                                    count = -5;
                                    order.setTime(System.currentTimeMillis());
                                    SPUtil.setEntity(FPrintStepActivity.this, "order", order);
                                    inputPrintFragment.setStep(3);
                                    setDialogText("请输入第三次指纹");
                                    if (binding.vp.getCurrentItem() != fragments.size() - 2) {
                                        binding.vp.setCurrentItem(fragments.size() - 2);
                                    }
                                    break;
                                case 93:
                                    count = -3;
                                    order.setTime(System.currentTimeMillis());
                                    SPUtil.setEntity(FPrintStepActivity.this, "order", order);
                                    inputPrintFragment.setStep(4);
                                    setDialogText("请输入第四次指纹");
                                    if (binding.vp.getCurrentItem() != fragments.size() - 2) {
                                        binding.vp.setCurrentItem(fragments.size() - 2);
                                    }
                                    break;
                                case 94:
                                    count = 0;
                                    order.setTime(System.currentTimeMillis());
                                    SPUtil.setEntity(FPrintStepActivity.this, "order", order);
                                    inputPrintFragment.setStep(5);
                                    setDialogText("请输入第五次指纹");
                                    if (binding.vp.getCurrentItem() != fragments.size() - 2) {
                                        binding.vp.setCurrentItem(fragments.size() - 2);
                                    }
                                    break;
                                case 3:
                                    dismissDialog();
                                    isLastOrder = false;
                                    order = new Order();
                                    SPUtil.setEntity(FPrintStepActivity.this, "order", order);
                                    binding.vp.setCurrentItem(fragments.size() - 1);
                                    comDisposable.clear();
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    @Override
                    protected void onFailure(BaseResponse response) {
                        super.onFailure(response);
                        comDisposable.clear();
                        dismissDialog();
                        Order order = new Order();
                        SPUtil.setEntity(FPrintStepActivity.this, "order", order);
                    }
                });
    }

    /**
     * 添加指纹
     *
     * @param name
     */
    private void addPrint(String name) {
        if (!isNetworkAvailable()) {
            return;
        }

        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", sdlId);
        params.put("authId", authId);
        params.put("openName", name);
        params.put("orderId", orderId);
        RequestUtils.request(RequestUtils.ADD_PRINT, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        ToastUtil.toastShort(getApplicationContext(), "添加成功");
                        if (receivedData.size() == 0) {
                            Intent intent = new Intent(FPrintStepActivity.this, PasswordStepActivity.class);
                            intent.putExtra("flag", 4);
                            intent.putExtra("sdlId", sdlId);
                            intent.putExtra("authId", authId);
                            intent.putExtra("needEvaluate", needEvaluate);
                            if (receivedData.size() == 0) {
                                intent.putExtra("isFirst", true);
                            }
                            startActivity(intent);
                            finish();
                        }
                        finish();
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        showDialog(0);
                    }
                });
    }

    private void setViewPagerScrollSpeed() {
        try {
            Field mScroller = null;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mScroller.set(binding.vp, scroller);
        } catch (NoSuchFieldException e) {

        } catch (IllegalArgumentException e) {

        } catch (IllegalAccessException e) {

        }
    }

    @Override
    protected NoticeDialog onCreateDialog(int id) {
        dialog = new NoticeDialog(this, R.style.dialog);
        dialog.setNotice("设置昵称失败！");

        dialog.setEvent(new NoticeDialog.Event() {
            @Override
            public void delete() {
                dialog.dismiss();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        return dialog;
    }

    /**
     * 显示progressDialog
     */
    private void showDialog() {
        try {
            pfd = new ProgressFragmentDialog("正在请求中...");
            pfd.show(getSupportFragmentManager(), "progressdialog");
        } catch (Exception e) {

        }
    }

    private void setDialogText(String text) {
        if (pfd == null) {
            return;
        }
        pfd.setContent(text);
    }

    /**
     * 关闭progressDialog
     */
    private void dismissDialog() {
        if (pfd == null) {
            return;
        }
        try {
            pfd.dismiss();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @android.support.annotation.NonNull String[] permissions, @android.support.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 30) {
            if (permissions[0].equals(Manifest.permission.READ_CONTACTS)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setData(ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(intent, 30);
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
        Uri contactData = data.getData();
        Cursor c = managedQuery(contactData, null, null, null, null);
        if (c.moveToFirst()) {
            String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
            String phoneNumber = null;
            if (hasPhone.equalsIgnoreCase("1")) {
                hasPhone = "true";
            } else {
                hasPhone = "false";
            }
            if (Boolean.parseBoolean(hasPhone)) {
                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
                                + contactId,
                        null,
                        null);
                while (phones.moveToNext()) {
                    phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
                phones.close();
                StringBuilder sb = new StringBuilder("");
                for (int i = 0; i < phoneNumber.length(); i++) {
                    char cc = phoneNumber.charAt(i);
                    if (cc > 47 && cc < 58) {
                        if (sb.length() < 1 && cc != 49) {
                            continue;
                        }
                        sb.append(cc);
                    }
                }
                inputInfoFragment.setPhoneNumber(sb.toString());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        comDisposable.clear();
    }
}
