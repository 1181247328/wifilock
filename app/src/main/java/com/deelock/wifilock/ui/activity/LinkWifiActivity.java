package com.deelock.wifilock.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.deelock.wifilock.R;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.databinding.ActivityLinkWifiBinding;
import com.deelock.wifilock.event.LinkWifiEvent;
import com.deelock.wifilock.utils.StatusBarUtil;
import com.deelock.wifilock.utils.ToastUtil;

/**
 * Created by binChuan on 2017\9\11 0011.
 */

public class LinkWifiActivity extends BaseActivity<ActivityLinkWifiBinding>
        implements LinkWifiEvent {

    private String lock_type;
    private String wifi_name;    //wifi名称
    private String wifi_mac;     //wifi的mac地址，即bssid
    private double longitude = 0;  //经度
    private double latitude = 0;   //纬度
    String mac;
    String deviceId;

    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();

    @Override
    protected int initLayout() {
        return R.layout.activity_link_wifi;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        StatusBarUtil.StatusBarLightMode(this);
        binding.setEvent(this);
        lock_type = getIntent().getStringExtra("type");
        mac = getIntent().getStringExtra("mac");
        deviceId = getIntent().getStringExtra("deviceId");

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setIgnoreKillProcess(true);
        option.SetIgnoreCacheException(false);
        mLocationClient.setLocOption(option);
        requestPermissionAndLocate();
    }

    @Override
    public void back() {
        finish();
    }

    /**
     * 点击下一步
     */
    @Override
    public void link() {
        if ("当前设备暂不支持WIFI".equals(wifi_name) || "请打开WIFI".equals(wifi_name)) {
            ToastUtil.toastShort(this, "请确认WIFI已开启");
            return;
        }
        String wifi_pwd = binding.passwordTv.getText().toString().trim();
        if (TextUtils.isEmpty(wifi_pwd) || wifi_pwd.length() < 8) {
            ToastUtil.toastShort(this, "WiFi密码不能少于8位！");
            return;
        }
        Intent intent;
        if ("000".equals(lock_type)) {
            //绑定网关设备
            intent = new Intent(this, BindGateWayActivity.class);
        } else {
            intent = new Intent(this, BindLockActivity.class);
        }
        intent.putExtra("wifi", wifi_name);
        intent.putExtra("bssid", wifi_mac);
        intent.putExtra("password", wifi_pwd);
        intent.putExtra("type", lock_type);
        intent.putExtra("longitude", longitude);
        intent.putExtra("latitude", latitude);
        intent.putExtra("mac", mac);
        intent.putExtra("deviceId", deviceId);
        startActivity(intent);
        if (mac != null) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCurrentWifi();
    }

    /**
     * 获取当前手机连接的wifi信息
     */
    private void getCurrentWifi() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager == null) {
            wifi_name = "当前设备暂不支持WIFI";
            binding.setSsid(wifi_name);
        } else {
            if (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
                wifi_name = "请打开WIFI";
                binding.setSsid(wifi_name);
                return;
            }
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            wifi_name = wifiInfo.getSSID().replace("\"", "");
            binding.setSsid("已连接：" + wifi_name);
            wifi_mac = wifiInfo.getBSSID();
        }
    }

    /**
     * 请求定位权限,并且开始定位
     */
    private void requestPermissionAndLocate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 120);
            } else {
                getCurrentWifi();
                mLocationClient.start();
            }
        }
        getCurrentWifi();
        mLocationClient.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 120) {
            if (permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentWifi();
                    mLocationClient.start();
                } else {
                    ToastUtil.toastShort(this, "请开启定位权限，否则功能无法正常使用");
                }
            }
        }
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            latitude = location.getLatitude();    //获取纬度信息
            longitude = location.getLongitude();    //获取经度信息
            mLocationClient.stop();
        }
    }
}
