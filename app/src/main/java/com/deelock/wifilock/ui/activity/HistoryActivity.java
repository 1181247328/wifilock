package com.deelock.wifilock.ui.activity;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.deelock.wifilock.event.HistoryEvent;
import com.deelock.wifilock.overwrite.HistoryRecyclerView;
import com.deelock.wifilock.utils.ToastUtil;
import com.google.gson.Gson;
import com.deelock.wifilock.R;
import com.deelock.wifilock.adapter.HistoryAdapter;
import com.deelock.wifilock.databinding.ActivityHistoryBinding;
import com.deelock.wifilock.entity.HistoryList;
import com.deelock.wifilock.entity.LockRecord;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.utils.SPUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by binChuan on 2017\9\28 0028.
 */

public class HistoryActivity extends BaseActivity {

    ActivityHistoryBinding binding;
    HistoryAdapter adapter;
    List<LockRecord> receivedData;
    List<LockRecord> data;
    Calendar startCalendar;
    Calendar endCalendar;
    TimePickerView timeStart;
    Date date;
    SimpleDateFormat sf;

    String sdlId;
    long maxDtft;
    SimpleDateFormat mf;

    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_history);
    }

    @Override
    protected void doBusiness() {
        sf = new SimpleDateFormat("yyyy/MM/dd");
        mf = new SimpleDateFormat("yyyy/MM/ddHH:mm");
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();

        date = new Date(System.currentTimeMillis());
        try {
            date = sf.parse(sf.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        startCalendar.setTime(date);
        endCalendar.setTime(date);
        initTimePicker();

        sdlId = getIntent().getStringExtra("sdlId");
        receivedData = new ArrayList<>();
        data = new ArrayList<>();
//        data.addAll(SPUtil.getList(this, "lockRecord", LockRecord.class));
        adapter = new HistoryAdapter(this, data, sdlId);
        ((SimpleItemAnimator)binding.rv.getItemAnimator()).setSupportsChangeAnimations(false);
        binding.rv.setAdapter(adapter);
        binding.rv.setLayoutManager(new LinearLayoutManager(this));

        if (SPUtil.isFirstTimeIntoHistoryPage(this)){
            binding.guideRl.setVisibility(View.VISIBLE);
            SPUtil.setFirstTimeIntoHistoryPage(this);
        }
    }

    @Override
    protected void requestData() {
        getHistory(true);
    }

    @Override
    protected void setEvent() {
        binding.rv.setMode(PullToRefreshBase.Mode.BOTH);
        binding.rv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<HistoryRecyclerView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<HistoryRecyclerView> refreshView) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getHistory(true);
                    }
                }).start();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<HistoryRecyclerView> refreshView) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getHistory(false);
                    }
                }).start();
            }
        });

        adapter.setEvent(new HistoryAdapter.Event() {
            @Override
            public void deleteOne(final int position) {
                if (!isNetworkAvailable()){
                    return;
                }

                Map params = new HashMap<>();
                params.put("timestamp", TimeUtil.getTime());
                params.put("uid", SPUtil.getUid(HistoryActivity.this));
                params.put("pid", data.get(position).getPid());
                params.put("sdlId", sdlId);
                RequestUtils.request(RequestUtils.DELETE_ONE, HistoryActivity.this, params).enqueue(
                        new ResponseCallback<BaseResponse>(HistoryActivity.this) {
                            @Override
                            protected void onSuccess(int code, String content) {
                                super.onSuccess(code, content);
                                processOne(position);
                            }
                        }
                );
            }

            @Override
            public void deleteDay(final int position) {
                if (!isNetworkAvailable()){
                    return;
                }

                long begin = getDate(position);
                long end = begin + 24*3600l;
                Map params = new HashMap<>();
                params.put("timestamp", TimeUtil.getTime());
                params.put("uid", SPUtil.getUid(HistoryActivity.this));
                params.put("sdlId", sdlId);
                params.put("timeBegin", begin);
                params.put("timeEnd", end);
                RequestUtils.request(RequestUtils.DELETE_DAY, HistoryActivity.this, params).enqueue(
                        new ResponseCallback<BaseResponse>(HistoryActivity.this) {
                            @Override
                            protected void onSuccess(int code, String content) {
                                super.onSuccess(code, content);
                                processDay(position);
                            }
                        }
                );
            }
        });

        binding.setEvent(new HistoryEvent() {
            @Override
            public void back() {
                finish();
            }

            @Override
            public void search() {
                timeStart.setDate(startCalendar);
                timeStart.show();
            }

            @Override
            public void closeGuide() {
                binding.guideRl.setVisibility(View.GONE);
            }
        });
    }

    private void initTimePicker() {
        startCalendar.set(GregorianCalendar.MONTH, startCalendar.get(GregorianCalendar.MONTH) - 6);
        timeStart = new TimePickerBuilder(this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date2, View v) {//选中事件回调
                date = date2;
                getHistoryByTime(date.getTime());
            }
        })
                .setType(new boolean[]{true, true, true, false, false, false})//默认全部显示
                .setCancelText("取消")//取消按钮文字
                .setSubmitText("确定")//确认按钮文字
                .setContentTextSize(18)//滚轮文字大小
                .setTitleSize(18)//标题文字大小
//                        .setTitleText("请选择时间")//标题文字
                .setOutSideCancelable(true)//点击屏幕，点在控件外部范围时，是否取消显示
                .isCyclic(false)//是否循环滚动
                .setTitleBgColor(Color.LTGRAY)
                .setTextColorCenter(Color.BLACK)//设置选中项的颜色
                .setTitleColor(Color.BLACK)//标题文字颜色
                .setSubmitColor(Color.BLUE)//确定按钮文字颜色
                .setCancelColor(Color.BLUE)//取消按钮文字颜色
//                        .setTitleBgColor(0xFF666666)//标题背景颜色 Night mode
//                        .setBgColor(0xFF333333)//滚轮背景颜色 Night mode
                .setRangDate(startCalendar, endCalendar)//默认是1900-2100年
//                        .operateDataForFirstTime(selectedDate)// 如果不设置的话，默认是系统时间*/
                .setLabel("","","","","","")
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .isDialog(false)//是否显示为对话框样式
                .build();
    }

    private void getHistory(final boolean refresh){
        if (!isNetworkAvailable()){
            return;
        }

        long end;
        if (refresh){
            end = -1L;
        } else {
            end = maxDtft - 1L;
        }
        Map params = new HashMap<>();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("pid", sdlId);
        params.put("count", 20);
        params.put("minDtft", -1L);
        params.put("maxDtft", end);
        RequestUtils.request(RequestUtils.HISTORY, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        if (refresh){
                            receivedData.clear();
                            receivedData.addAll(new Gson().fromJson(content, HistoryList.class).getList());
                            sortData(refresh);
                            updateUi();
                        } else {
                            int size = receivedData.size();
                            receivedData.addAll(new Gson().fromJson(content, HistoryList.class).getList());
                            if (receivedData.size() != size){
                                sortData(refresh);
                                updateUi();
                            } else {
                                ToastUtil.toastShort(HistoryActivity.this, "没有更多数据！");
                            }
                        }
                    }

                    @Override
                    protected void onFinish() {
                        super.onFinish();
                        binding.rv.onRefreshComplete();
                    }
                }
        );
    }

    private void updateUi(){
        adapter.notifyDataSetChanged();
        if (data.size() > 0){
            maxDtft = data.get(data.size() - 1).getDtft();
        }
        SPUtil.setList(HistoryActivity.this, "lockRecord", data);
    }

    private void getHistoryByTime(long start){
        if (!isNetworkAvailable()) {
            return;
        }

        Map params = new HashMap<>();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("pid", sdlId);
        params.put("count", 20);
        params.put("minDtft", start * 1000l);
        params.put("maxDtft", start * 1000l + 86400000000l);
        RequestUtils.request(RequestUtils.HISTORY, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        receivedData.clear();
                        receivedData.addAll(new Gson().fromJson(content, HistoryList.class).getList());
                        sortData(true);
                        adapter.notifyDataSetChanged();
                        if (data.size() > 0){
                            maxDtft = data.get(data.size() - 1).getDtft();
                        }
                    }
                }
        );
    }

    /**
     *
     * 排序
     */
    private void sortData(boolean refresh){
        for (int i = 0; i < receivedData.size(); i++){
            for (int j = i + 1; j < receivedData.size() - 1; j++){
                if (receivedData.get(i).getDtft() < receivedData.get(j).getDtft()){
                    receivedData.add(i, receivedData.remove(j));
                }
            }
        }

        if (refresh){
            operateDataForFirstTime();
        } else {
            operateData();
        }
    }

    /**
     * 添加日期
     */
    public void operateDataForFirstTime(){
        data.clear();
        if (receivedData.size() > 0){
            for (LockRecord l : receivedData){
                l.setTime(getTime(l.getDtft()));
            }
            LockRecord l = new LockRecord();
            l.setType(2);
            l.setTime(receivedData.get(0).getTime());
            data.add(l);
            data.add(receivedData.get(0));
            for (int i = 1; i < receivedData.size(); i++){
                String currDate = receivedData.get(i).getTime().substring(0, 10);
                String lastDate = receivedData.get(i - 1).getTime().substring(0, 10);
                if (!currDate.equals(lastDate)){
                    l = new LockRecord();
                    l.setTime(currDate);
                    l.setType(2);
                    data.add(l);
                }
                data.add(receivedData.get(i));
            }
        }
    }

    /**
     * 处理数据
     */
    private void operateData(){
        String time = data.get(data.size() - 1).getTime().substring(0, 10);
        data.clear();
        if (receivedData.size() > 0){
            for (LockRecord l : receivedData){
                l.setTime(getTime(l.getDtft()));
            }
            LockRecord l;
            if (!receivedData.get(0).getTime().substring(0, 10).equals(time)){
                l = new LockRecord();
                l.setType(2);
                l.setTime(receivedData.get(0).getTime());
                data.add(l);
            }
            data.add(receivedData.get(0));
            for (int i = 1; i < receivedData.size(); i++){
                String currDate = receivedData.get(i).getTime().substring(0, 10);
                String lastDate = receivedData.get(i - 1).getTime().substring(0, 10);
                if (!currDate.equals(lastDate)){
                    l = new LockRecord();
                    l.setTime(currDate);
                    l.setType(2);
                    data.add(l);
                }
                data.add(receivedData.get(i));
            }
        } else {
            data.clear();
        }
    }

    /**
     *
     * 获取timeBegin
     * @param p
     * @return
     */
    private long getDate(int p){
        String date = data.get(p).getTime().substring(0, 10) + "00:00";
        Date d;
        long dtft = 0;
        try {
            d = mf.parse(date);
            long l = d.getTime();
            String str = String.valueOf(l);
            dtft = Long.parseLong(str.substring(0, 10));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dtft;
    }

    /**
     * 处理删除单条历史纪录请求成功后的数据变化和视图
     * @param p
     */
    private void processOne(int p){
        data.remove(p);
        if (data.get(p - 1).getType() == 2){
            if (data.size() == p || data.get(p).getType() == 2){
                data.remove(p - 1);
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     *
     * 处理删除某天历史记录请求成功后的数据变化和视图
     * @param p
     */
    private void processDay(int p){
        data.remove(p);
        while (p < data.size() && data.get(p).getType() != 2){
            data.remove(p);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     *
     * 比较data p1位置和p2位置的日期是否相同
     * @param p1
     * @param p2
     * @return
     */
    private boolean compareDayFromTime(int p1, int p2){
        if (data.get(p1).getTime().substring(0, 10).equals(data.get(p2).getTime().substring(0, 10)))
            return true;
        return false;
    }

    /**
     * 获取时间
     * @param dtft
     * @return
     */
    private String getTime(long dtft){
        long time = Long.parseLong(String.valueOf(dtft).substring(0, 13));
        return mf.format(new Date(time));
//        return mf.format(new Date());
    }
}
