package com.deelock.wifilock.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.ActivityChangeWifiBinding;
import com.deelock.wifilock.entity.Bind;
import com.deelock.wifilock.entity.LockDetail;
import com.deelock.wifilock.event.LinkWifiEvent;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.dialog.NoticeDialog;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.StatusBarUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.demo_activity.EspWifiAdminSimple;
import com.espressif.iot.esptouch.task.__IEsptouchTask;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by binChuan on 2017\9\11 0011.
 */

public class ChangeWifiActivity extends BaseActivity {

    private ActivityChangeWifiBinding binding;
    private EspWifiAdminSimple mWifiAdmin;

    public LocationClient mLocationClient = null;
    public MyLocationListener myListener;

    double latitude = 0;
    double longitude = 0;
    boolean sendAble;
    private ProgressDialog mProgressDialog;
    Disposable disposable;
    String wifi;

    // LockDetail lockDetail;

    int count;

    NoticeDialog dialog;

    final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 120;

    private static String TAG = "link";
    private String type = null;

    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_wifi);
        StatusBarUtil.StatusBarLightMode(this);
    }

    @Override
    protected void doBusiness() {
        String ssid = getIntent().getStringExtra("ssid");
        type = getIntent().getStringExtra("type");
        if (ssid != null) {
            binding.workWifiTv.setText("设备当前工作WiFi：" + ssid);
        }

        mWifiAdmin = new EspWifiAdminSimple(this);
        mLocationClient = new LocationClient(this);
        //声明LocationClient类
        myListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myListener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            } else {
                mLocationClient.start();
            }
        } else {
            mLocationClient.start();
        }

        setWifiPasswordInputFilter(binding.passwordTv);

        dialog = new NoticeDialog(this, R.style.dialog);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationClient.start();
                } else {

                }
            }
        }
    }

    @Override
    protected void requestData() {

    }

    @Override
    protected void setEvent() {
        binding.setEvent(new LinkWifiEvent() {
            @Override
            public void back() {
                finish();
            }

            @Override
            public void link() {
                String password = binding.getPassword();
                if (password == null || password.length() < 8) {
                    ToastUtil.toastShort(getApplicationContext(), "WiFi密码不能少于8位！");
                    return;
                }
                new EsptouchAsyncTask3().execute(wifi, getBSSID(), binding.getPassword(), "1");
                requestBind();
            }
        });

        dialog.setEvent(new NoticeDialog.Event() {
            @Override
            public void delete() {
                dialog.dismiss();
                finish();
            }
        });
    }

    /**
     * 设置显示wifi名
     */
    private void showSsid() {
        wifi = getSSID();
        binding.setSsid("切换WiFi：" + wifi);
    }

    /**
     * 百度sdk 获取经纬度
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            latitude = location.getLatitude();    //获取纬度信息
            longitude = location.getLongitude();    //获取经度信息
            sendAble = true;
        }
    }

    private void requestBind() {
        final Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        if (latitude - 4.9E-324 == 0 || longitude - 4.9E-324 == 0) {
            params.put("longitude", 0);
            params.put("latitude", 0);
        } else {
            DecimalFormat df = new DecimalFormat("0.00000");
            params.put("longitude", df.format(longitude));
            params.put("latitude", df.format(latitude));
        }
        if ("000".equals(type)) {
            params.put("devType", type);
        } else {
            params.put("type", type);
        }
        params.put("ssid", wifi);
        params.put("phoneNumber", SPUtil.getPhoneNumber(this));

        count = 0;
        Observer observer = new Observer<Long>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(@NonNull Long aLong) {
                if (!isNetworkAvailable()) {
                    disposable.dispose();
                    mProgressDialog.dismiss();
                    return;
                }

                count++;
                String url = null;
                if ("000".equals(type)) {
                    url = RequestUtils.GATEWAY_BIND;
                } else {
                    url = RequestUtils.BIND;
                }
                RequestUtils.request(url, ChangeWifiActivity.this, params).enqueue(
                        new ResponseCallback<BaseResponse>(ChangeWifiActivity.this) {
                            @Override
                            protected void onSuccess(int code, String content) {
                                super.onSuccess(code, content);
                                if (code == 1) {
                                    disposable.dispose();
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                    }
                                    ToastUtil.toastShort(getApplicationContext(), "配置成功");
                                    finish();
                                } else if (code == 2 && count > 45) {
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                        ToastUtil.toastShort(getApplicationContext(), "配置超时");
                                    }
                                    disposable.dispose();
                                }
                            }

                            @Override
                            protected void onFailure(BaseResponse response) {
                                super.onFailure(response);
                                if (mProgressDialog != null) {
                                    mProgressDialog.dismiss();
                                }
                                processLog(response);
                                if (disposable != null) {
                                    disposable.dispose();
                                }
                            }
                        }
                );
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        Observable.interval(0, 2, TimeUnit.SECONDS).subscribe(observer);
    }

    private void processLog(BaseResponse response) {
        Bind bind = new Gson().fromJson(response.getContent(this), Bind.class);
        if (response.code == -1006) {
            String number = bind.getPhoneNumber();
            String productId = bind.getProductId();
            if (!TextUtils.isEmpty(number)) {
                dialog.setNotice("设备" + productId + "，已被手机号为" + number + "的用户绑定");
                dialog.show();
                return;
            }

            String uid = bind.getUid();
            if (!TextUtils.isEmpty(uid)) {
                ToastUtil.toastLong(getApplicationContext(), "设备已被用户" + uid + "绑定");
                dialog.setNotice("设备已被用户" + uid + "绑定");
                dialog.show();
            }
        } else if (response.code == -1005) {
            dialog.setNotice("该设备未注册或非法！");
            dialog.show();
        }
    }

    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {

        private IEsptouchTask mEsptouchTask;
        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(ChangeWifiActivity.this);
            mProgressDialog.setMessage("正在连接WiFi...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                        if (disposable != null) {
                            disposable.dispose();
                        }
                    }
                }
            });
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (__IEsptouchTask.DEBUG) {
                            Log.i(TAG, "progress dialog is canceled");
                        }
                        if (disposable != null) {
                            disposable.dispose();
                        }
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                    }
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    "Waiting...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            mProgressDialog.show();
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            synchronized (mLock) {
                // !!!NOTICE
                String apSsid = wifi;
                String apBssid = params[1];
                String apPassword = params[2];
                String taskResultCountStr = params[3];
//                taskResultCount = Integer.parseInt(taskResultCountStr);
                taskResultCount = 1;
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, ChangeWifiActivity.this);
                mEsptouchTask.setEsptouchListener(listener);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
//            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
//            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Confirm");
            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                int count = 0;
                // max results to be displayed, if it is more than maxDisplayCount,
                // just show the count of redundant ones
                final int maxDisplayCount = 5;
                // the task received some results including cancelled while
                // executing before receiving enough results
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    for (IEsptouchResult resultInList : result) {
                        sb.append("Esptouch success, bssid = "
                                + resultInList.getBssid()
                                + ",InetAddress = "
                                + resultInList.getInetAddress()
                                .getHostAddress() + "\n");
                        count++;
                        if (count >= maxDisplayCount) {
                            break;
                        }
                    }
                    if (count < result.size()) {
                        sb.append("\nthere's " + (result.size() - count) + " more result(s) without showing\n");
                    }
                    mProgressDialog.setMessage("配置WiFi成功，正在连接服务器...");
//                    mProgressDialog.dismiss();
                } else {
                    mProgressDialog.setMessage("配置WiFi失败");
//                    mProgressDialog.dismiss();
                }
            }
        }
    }

    private String getSSID() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String wifi = wifiInfo.getSSID().replace("\"", "");
        return wifi;
    }

    private String getBSSID() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String wifi = wifiInfo.getBSSID().replace("\"", "");
        return wifi;
    }

    private IEsptouchListener listener = new IEsptouchListener() {

        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
            onEsptoucResultAddedPerform(result);
        }
    };

    private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                String text = result.getBssid() + " is connected to the wifi";
//                ToastUtil.toastShort(getApplicationContext(), text);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showSsid();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationClient.unRegisterLocationListener(myListener);
        mLocationClient.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }
}
