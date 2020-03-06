package com.deelock.wifilock.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.ble.ble.BleCallBack;
import com.ble.ble.BleService;
import com.deelock.state.BleListPullFind;
import com.deelock.state.WiFiFind;
import com.deelock.wifilock.application.CustomApplication;
import com.deelock.wifilock.bluetooth.BleActivity;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import io.reactivex.disposables.CompositeDisposable;
import okhttp3.Headers;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BluetoothUtil {

    private static BluetoothAdapter bluetoothAdapter = null;

    private static BluetoothGatt bluetoothGatt;


    public static boolean isConnected = false;
    private static boolean isOpen = false;

    private static BleService mLeService;

    private static BluetoothLeScanner scanner;
    private static ScanCallback scanCallback;

    private static String currentMac;

    //监听器
    private static CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public static ArrayList<ScanResult> scanResults = new ArrayList<>();   //扫描蓝牙结果集

//    private static UUID my_serviceUuid = UUID.fromString("00001000-0000-1000-8000-00805f9b34fb");   //主服务uuid
//    private static UUID my_writeUuid = UUID.fromString("00001001-0000-1000-8000-00805f9b34fb");    //read | write_no_response | write
//    private static UUID my_notifyUuid = UUID.fromString("00001002-0000-1000-8000-00805f9b34fb");   //read | notify

    private static int detectLinkTime = 20;   //重连时间，单位（秒）

    private static boolean isFromBind = false;

    private static Context context = null;

    /**
     * 用于当通过蓝牙将wifi密码发送到蓝牙模块时，wifi会有一个等待绑定的时间，由于没有发送数据给蓝牙，所以recv_1、recv_2、recv_3是为空
     * 无法在返回过程中做出recv_1!=null的判断，这里单独为绑定wifi做一个布尔型数据
     */
    public static boolean isWifi = false;

    //用于判断是否返回绑定wifi会成功
    public static boolean isWifiYes = false;

    /**
     * 当门锁与手机非用户主动断开连接时，监听重连，重新连接时间超过规定时间，中断连接
     */
    private static Thread detectLinkStateThread;
    private static long startTime;   //监测起始时间

    private static void detectLink() {
        if (detectLinkStateThread == null) {
            detectLinkStateThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (bluetoothAdapter.isEnabled()) {
                        if (mLeService != null) {
                            while ((SystemClock.currentThreadTimeMillis() - startTime) / 1000 < detectLinkTime) {
                                if (isFromBind) {
                                    isFromBind = false;
                                    break;
                                }
                                if (isConnected) {
                                    break;
                                }
                            }
                            if (!isConnected) {
                                if (conn != null) {
                                    if (mLeService != null) {
                                        if (mac != null) {
                                            mLeService.disconnect(mac.toUpperCase());
                                            CustomApplication.mContext.unbindService(conn);
                                        }
                                        mLeService = null;
                                    }
                                }
                            }
                            currentMac = null;
                            detectLinkStateThread = null;
                        }
                    } else {
                        if (!isConnected) {
                            if (conn != null) {
                                if (mLeService != null) {
                                    if (mac != null) {
                                        mLeService.disconnect(mac.toUpperCase());
                                        CustomApplication.mContext.unbindService(conn);
                                    }
                                    mLeService = null;
                                }
                            }
                        }
                        currentMac = null;
                        detectLinkStateThread = null;
                    }
                }
            });
            detectLinkStateThread.start();
        }
    }

    /**
     * 开启蓝牙
     *
     * @return false开启失败，true开启成功
     */
    public static boolean openBluetooth() {
        @SuppressLint("WrongConstant") BluetoothManager bluetoothManager = (BluetoothManager) CustomApplication.mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if (!CustomApplication.mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(CustomApplication.mContext, "当前设备不支持BLE", Toast.LENGTH_LONG).show();
            return false;
        }
        if (bluetoothManager == null) {
            Toast.makeText(CustomApplication.mContext, "当前设备不支持蓝牙", Toast.LENGTH_LONG).show();
            return false;
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(CustomApplication.mContext, "当前设备不支持蓝牙", Toast.LENGTH_LONG).show();
            return false;
        }
        isOpen = bluetoothAdapter.isEnabled() || bluetoothAdapter.enable();
        return isOpen;
    }

    /**
     * 断开蓝牙连接,不关闭蓝牙(暂不断开,以后放弃该方法)
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void closeBluetooth() {
//        if (bluetoothAdapter != null) {
//            if (bluetoothAdapter.isEnabled()) {
//                if (bluetoothGatt != null) {
//                    if (isConnected) {
////                        bluetoothGatt.disconnect();
//                    }
//                }
//                bluetoothAdapter.cancelDiscovery();
//                if (scanner != null && scanCallback != null) {
//                    scanner.stopScan(scanCallback);
//                    scanner = null;
//                    scanCallback = null;
//                }
//            }
//            isOpen = false;
//        }
    }

    /**
     * 中断蓝牙连接
     */
    public static void disConnect() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                if (mLeService != null) {
                    if (mac != null) {
                        mLeService.disconnect(mac.toUpperCase());
                    }
                    mLeService = null;
                    if (conn != null) {
                        CustomApplication.mContext.unbindService(conn);
                    }
                }
                currentMac = null;
                BleActivity.CanIUseBluetooth = false;
                isConnected = false;
            }
        }
    }

    /**
     * 关闭蓝牙
     */
    public static void stopBluetooth() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                if (mLeService != null) {
                    if (conn != null) {
//                        CustomApplication.mContext.unbindService(conn);
                    }
                    if (mac != null) {
                        mLeService.disconnect(mac.toUpperCase());
                    }
                    mLeService = null;
                }
//                bluetoothAdapter.disable();
            }
            isOpen = false;
            isConnected = false;
        }
    }

    /**
     * 检测定位权限，部分手机在无定位权限，未开启定位信息按钮情况下，不能扫描到蓝牙设备
     *
     * @return false无权限（应停止蓝牙相关操作），true已获取权限（开始蓝牙扫描）
     */
    public static boolean checkPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ContextCompat
                .checkSelfPermission(CustomApplication.mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 通过蓝牙名字连接蓝牙（该方法仅支持android 21及以上,即5.0及以上版本）
     *
     * @param deviceName 蓝牙名称（Deelock1377）
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void connectByName(final String deviceName) {
        if (!isOpen) {
            Toast.makeText(CustomApplication.mContext, "蓝牙未开启，无法使用", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                scanner = bluetoothAdapter.getBluetoothLeScanner();
                scanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);
                        needOpenLocatopn = false;
                        int size = scanResults.size();
                        Log.e("BluetoothUtil", "---已得到的蓝牙数量---" + size
                                + "---扫描到的蓝牙名称---" + result.getDevice().getName()
                                + "---扫描到的蓝牙地址---" + result.getDevice().getAddress()
                                + "---rssi----" + result.getRssi());
                        if (size == 0) {
                            String name = result.getDevice().getName();
                            if (name != null) {
                                String realName = name.trim();
                                if (realName.length() >= 7 && realName.substring(0, 7).equals(deviceName) && result.getRssi() < 0) {
                                    scanResults.add(result);
                                }
                            }
                            EventBus.getDefault().post(new BleListPullFind());
                        } else {
                            int a;
                            for (a = 0; a < size; a++) {
                                boolean equals = scanResults.get(a).getDevice().getAddress().equals(result.getDevice().getAddress());
                                if (equals) {
                                    //先删除这个重复的数据
                                    scanResults.remove(a);
                                    scanResults.add(result);
                                    break;
                                }
                            }
                            if (a == size) {
                                String name = result.getDevice().getName();
                                if (name != null) {
                                    String realName = name.trim();
                                    if (realName.length() >= 7 && realName.substring(0, 7).equals(deviceName) && result.getRssi() < 0) {
                                        scanResults.add(result);
                                    }
                                }
                            }
                            EventBus.getDefault().post(new BleListPullFind());
                        }
                        Collections.sort(scanResults, new Comparator<ScanResult>() {
                            @Override
                            public int compare(ScanResult o1, ScanResult o2) {
                                return o1.getRssi() - o2.getRssi();
                            }
                        });
                        EventBus.getDefault().post(new BleListPullFind());
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                        isConnected = false;
                        Log.e("main", "---onScanFailed");
                    }
                };
                scanner.startScan(scanCallback);
            } else {
                Toast.makeText(CustomApplication.mContext, "当前手机版本不支持该功能", Toast.LENGTH_LONG).show();
            }
        } catch (
                Exception e)

        {
            SystemClock.sleep(100);
            connectByName(deviceName);
        }
    }

    /**
     * 开始连接蓝牙设备
     *
     * @param device 扫描到的信号最好的蓝牙设备
     */
//    public static void startConnect(BluetoothDevice device) {
    public static void startConnect(String device) {
        boolean isLe = false;
        if (mLeService != null) {
            isLe = true;
            mLeService.disconnectAll();
            mLeService = null;
        }
        removePairDevice();
        Log.e("main", "---mLeService---" + isLe);
        isFromBind = true;
        mac = device;
        currentMac = mac;
        boolean isBind = CustomApplication.mContext.bindService(intent, conn, CustomApplication.mContext.BIND_AUTO_CREATE);
        Log.e("main", "---startConnect---" + isBind + "---" + mac);
//        Log.e("main", "---判断当前这个服务是否存在后台---" + isServiceRunning());
    }

    /**
     * 停止扫描
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void stopScan() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            if (scanner != null && scanCallback != null) {
                scanner.stopScan(scanCallback);
            }
        }
    }

    /**
     * 根据蓝牙地址连接蓝牙设备
     *
     * @param bleMac 蓝牙mac地址，在首次扫描绑定中获取并存储(取消，不再使用)
     */
    public static void connectByMac(String bleMac) {
        Log.e("main---根据地址连接1---", bleMac + "---" + (mLeService != null) + "----" + bleMac.equals(currentMac));
//        if (mLeService != null && bleMac.equals(currentMac)) {
        if (mLeService != null) {
            return;
        }
        removePairDevice();
        if (mLeService != null) {
            mLeService.disconnectAll();
            mLeService = null;
            CustomApplication.mContext.unbindService(conn);
        }
        isConnected = false;
        BleActivity.CanIUseBluetooth = false;
        mac = bleMac;
        Log.e("main---根据地址连接---", bleMac);
        currentMac = mac;
        boolean bindService = CustomApplication.mContext.bindService(intent, conn, CustomApplication.mContext.BIND_AUTO_CREATE);
        Log.e("main---根据地址连接2---", bindService + "");
    }

    private static void removePairDevice() {
        if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
            bluetoothAdapter.enable();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
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

        }
    }

    private static final Intent intent = new Intent(CustomApplication.mContext, BleService.class);

    /**
     * 在蓝牙主界面中第一次通过蓝牙扫描mac相同时停止扫描
     *
     * @param bleMac
     */
    public static void connectByScan(final String bleMac) {
        if (mLeService != null && bleMac != null && bleMac.equals(currentMac)) {
            return;
        }
        if (mLeService != null) {
            mLeService.disconnectAll();
            mLeService = null;
            CustomApplication.mContext.unbindService(conn);
        }
        isConnected = false;
        BleActivity.CanIUseBluetooth = false;
        mac = bleMac;
        currentMac = mac;
        CustomApplication.mContext.bindService(intent, conn, CustomApplication.mContext.BIND_AUTO_CREATE);
    }

    /**
     * 接收到蓝牙的第一段数据
     */
    private static String recv_1 = null;
    /**
     * 接收到蓝牙的第二段数据
     */
    private static String recv_2 = null;
    /**
     * 接收到蓝牙的第三段数据
     */
    private static String recv_3 = null;

    /**
     * 向蓝牙设备发送指令
     *
     * @param code 指令内容，在广播接收器中生成
     */
    public static void writeCode(String code) {
        removePairDevice();
        if (mLeService != null) {
            Log.e("main_发送---", code);
            Log.e("main_发送_2---", code.length() + "");
            if (code.length() == 104) {
                String send_1 = code.substring(0, 40);
                String send_2 = code.substring(40, 80);
                String send_3 = code.substring(80);
                boolean isSendOne = send(mac.toUpperCase(), send_1, false);
                Log.e("main", "---1---" + isSendOne);
                SystemClock.sleep(50);
                boolean isSendTwo = send(mac.toUpperCase(), send_2, false);
                Log.e("main", "---2---" + isSendTwo);
                SystemClock.sleep(50);
                boolean isSendThree = send(mac.toUpperCase(), send_3, false);
                Log.e("main", "---3---" + isSendThree);
//                String send_2 = code.substring(40, 80);
//                String send_3 = code.substring(80);
//                DisposableObserver orderObserver = getWriteOneOrderObserver();
//                Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
//                        .take(1).observeOn(AndroidSchedulers.mainThread()).subscribe(orderObserver);
//                mCompositeDisposable.add(orderObserver);
            } else {
                boolean isCode = send(mac.toUpperCase(), code, false);
                Log.e("main", "---4---" + isCode);
            }
            recv_order = null;
        }
    }

    //      SystemClock.sleep(20);
    private static synchronized boolean send(String address, String value, boolean isMessage) {
        return mLeService.send(address, value, isMessage);
    }

    public static String recv_order = null;
    public static boolean needOpenLocatopn = true;    //部分手机需要开启定位按钮
    public static String mac = null;
    public static String isAddFprint = null;
    public static String isBindWifi = null;
    public static String action = null;

    public static void clearInfo() {
        isAddFprint = null;
        action = null;
        recv_order = null;
        isBindWifi = null;
    }

    /**
     * 转发蓝牙设备发送的指令
     *
     * @param order   指令
     * @param context 上下文
     * @param event   外部实现接口
     */
    public static void requestResult(String order, final Context context, final BleEvent event) {
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(context));
        map.put("lockCmd", order);
        RequestUtils.request(RequestUtils.BLE_FORWARD, context, map)
                .enqueue(new ResponseCallback<BaseResponse>((Activity) context) {

                    @Override
                    protected void onSuccess(BaseResponse response, Headers headers) {
                        super.onSuccess(response, headers);
                        event.success(response.code, response.msg, response.getContent(context));
                    }

                    @Override
                    protected void onFailure(BaseResponse response) {
                        super.onFailure(response);
                        event.fail(response.code, response.msg, response.getContent(context));
                    }
                });

    }

    public interface BleEvent {
        void success(int code, String message, String content);

        void fail(int code, String message, String content);

    }

    private static ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLeService = ((BleService.LocalBinder) service).getService(mBleCallBack);
            mLeService.initialize();
            mLeService.setDecode(false);
            if (mac != null) {
                SystemClock.sleep(300);
                boolean isConnected = mLeService.connect(mac.toUpperCase(), true);
                Log.e("main", "---开始连接---" + mac.toUpperCase() + "---" + isConnected);
            } else {
                Log.e("main", "---mac为空---");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("main", "---onServiceDisconnected---" + name);
            mLeService = null;
        }
    };

    private static BleCallBack mBleCallBack = new BleCallBack() {

        @Override
        public void onConnected(String s) {
            Log.e("main", "onConnected" + "---" + s);
            super.onConnected(s);
        }

        @Override
        public void onDisconnected(String s) {
            Log.e("main", "onDisconnected---" + s);
            isConnected = false;
            BleActivity.CanIUseBluetooth = false;
            if (mLeService != null) {
                startTime = SystemClock.currentThreadTimeMillis();
                detectLink();
            }
        }

        @Override
        public void onCharacteristicWrite(String s, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            Log.e("main", "onCharacteristicWrite---" + s + "---" +
                    InputUtil.bytesToHexString(bluetoothGattCharacteristic.getValue()) + "---" + i);
            super.onCharacteristicWrite(s, bluetoothGattCharacteristic, i);
        }

        @Override
        public void onServicesDiscovered(String s) {
            SystemClock.sleep(300);
            boolean isService = mLeService.enableNotification(mac.toUpperCase());
            Log.e("main", "onServicesDiscovered" + "---" + s + "---" + isService);
        }

        @Override
        public void onDescriptorWrite(String s, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            writeCode("deedee");
            Log.e("main", "onDescriptorWrite---" + s + "---" +
                    InputUtil.bytesToHexString(bluetoothGattDescriptor.getValue()) + "---" + i);
            isConnected = true;
            BleActivity.CanIUseBluetooth = true;
        }

        @Override
        public void onCharacteristicChanged(String s, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            Log.e("main", "onCharacteristicChanged" + "---" + s);
            String recv = InputUtil.bytesToHexString(bluetoothGattCharacteristic.getValue());
            Log.e("main_接收", recv);
            if ("A1D".equals(recv.substring(0, 3)) || "A2D".equals(recv.substring(0, 3))) {
                recv_1 = recv;
            } else if (recv.length() >= 20 && "CC".equals(recv.substring(18, 20))) {
                recv_3 = recv.substring(0, 20);
            } else {
                recv_2 = recv;
            }
            if (isWifi && recv_1 != null && recv_2 != null && recv_3 != null) {
                String recv_4 = recv_1 + recv_2 + recv_3;
                Log.e("main", "---" + recv_1 + "---" + recv_2 + "---" + recv_3);
                isWifi = false;
                EventBus.getDefault().post(new WiFiFind(recv_4));
                return;
            }
            if (recv_1 != null && recv_2 != null && recv_3 != null && recv_order == null) {
                String recv_4 = recv_1 + recv_2 + recv_3;
                Log.e("main_完整接收", recv_4);
                if (recv_4.length() == 100) {
                    recv_order = recv_4;
                    recv_1 = null;
                    recv_2 = null;
                    recv_3 = null;
                }
            } else {
                //TODO
            }
        }
    };
}
