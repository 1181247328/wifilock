package com.deelock.state;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.deelock.wifilock.R;
import com.deelock.wifilock.bluetooth.BleBindActivity;
import com.deelock.wifilock.utils.BluetoothUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * ble设备扫描集合
 */
public class BleListActivity extends BaseActivity {

    private String TAG = BleListActivity.class.getSimpleName();

    private int REQUEST_LOCATION = 100;   //定位权限请求码

    //上下文
    private Context context = null;

    //事件分发总线
    private EventBus eventBus = EventBus.getDefault();

    //中间逻辑层
    private LockState lockState = LockState.getLockState();

    //下拉刷新器
    @BindView(R.id.srl_plant_pull)
    public SwipeRefreshLayout srlPlantPull = null;

    //返回
    @BindView(R.id.rl_collection_return)
    public RelativeLayout rlCollectionReturn = null;

    //数据集合
    @BindView(R.id.lv_ble_list)
    public ListView lvBleList = null;

    private BleListAdapter bleListAdapter = null;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //加载布局
        setContentView(R.layout.activity_blelist);

        StaturBar.setStatusBar(this, R.color.colorPrimary, false);

        lockState.changeStatusBarTextImgColor(this, true);

        initAgo();

        initView();

        initAdapter();

        initListener();

        initBack();
    }

    private void initAgo() {
        context = this;
        ButterKnife.bind(this);
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
    }

    private void initView() {

    }

    private void initAdapter() {
        bleListAdapter = new BleListAdapter(context);
        lvBleList.setAdapter(bleListAdapter);
    }

    private void initListener() {
        //下拉触发返回监听
        srlPlantPull.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onRefresh() {
                //清除现有的ble
                BluetoothUtil.scanResults.clear();
                //刷新集合器
                bleListAdapter.notifyDataSetChanged();
                //关闭扫描
                BluetoothUtil.stopScan();
                //开始扫描
                openBluetooth();
            }
        });

        /**
         * 防止上拉list时出现焦点与下拉事件冲突
         */
        lvBleList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                View firstView = view.getChildAt(firstVisibleItem);
                // 当firstVisibleItem是第0位。如果firstView==null说明列表为空，需要刷新;或者top==0说明已经到达列表顶部, 也需要刷新
                if (firstVisibleItem == 0 && (firstView == null || firstView.getTop() == view.getPaddingTop())) {
                    srlPlantPull.setEnabled(true);
                } else {
                    srlPlantPull.setEnabled(false);
                }
            }
        });

        //listview点击事件
        lvBleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                initIntent(BleBindActivity.class, (bleListAdapter.getCount() - 1) - position);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initBack() {
        initBle();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initBle() {
        boolean checkPermission = BluetoothUtil.checkPermission();   //检查定位权限
        if (checkPermission) {
            openBluetooth();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        }
    }

    /**
     * 开启蓝牙搜索
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openBluetooth() {
        boolean isOpen = BluetoothUtil.openBluetooth();    //检查蓝牙是否开启，没开启则开启蓝牙
        Log.e("main", "---" + isOpen);
        if (isOpen) {
            Log.e("main", "---1");
            //TODO 通过名字去查找
            srlPlantPull.setRefreshing(true);
            BluetoothUtil.connectByName("Deelock");  //通过名字连接门锁设备
        } else {
            Toast.makeText(this, "蓝牙开启失败，设备搜索失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void initIntent(Class<?> activity, int position) {
        Intent intent = new Intent(context, activity);
        Log.e(TAG, "---点击的位置为---" + position);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    @OnClick({R.id.rl_collection_return})
    public void onClick(View v) {
        switch (v.getId()) {
            //返回
            case R.id.rl_collection_return:
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "---销毁---");
        if (BluetoothUtil.scanResults != null) {
            BluetoothUtil.scanResults.clear();
        }
        eventBus.unregister(this);
    }

    /**
     * 刷新Ble集合器
     *
     * @param s
     */
    @Subscribe
    public void onEventMainThread(BleListPullFind s) {
        Log.e(TAG, "---刷新");
        srlPlantPull.setRefreshing(false);
        if (bleListAdapter != null) {
            bleListAdapter.notifyDataSetChanged();
        }
    }
}

