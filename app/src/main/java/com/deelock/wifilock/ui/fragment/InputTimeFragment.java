package com.deelock.wifilock.ui.fragment;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.FragmentInputTimeBinding;
import com.deelock.wifilock.event.InputTimeEvent;
import com.deelock.wifilock.utils.StatusBarUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by binChuan on 2017\9\27 0027.
 */

public class InputTimeFragment extends BaseFragment {

    FragmentInputTimeBinding binding;
    Calendar startCalendar;
    Calendar endSelectCalendar;
    Calendar endCalendar;
    TimePickerView timeStart;
    Date startDate, endDate;
    boolean end;
    SimpleDateFormat sf;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_input_time, container, false);
        StatusBarUtil.StatusBarLightMode(this.getActivity());
        doBusiness();
        return binding.getRoot();
    }

    private void doBusiness() {
        sf = new SimpleDateFormat("yyyy/MM/dd\nHH:00");
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        endSelectCalendar = Calendar.getInstance();
        startDate = new Date();
        startDate.setTime(startDate.getTime() - startDate.getTime() % 3600000l);
        endDate = new Date(startDate.getTime() + 86400000l);
        endCalendar.set(GregorianCalendar.YEAR, startCalendar.get(GregorianCalendar.YEAR) + 3);
        endSelectCalendar.setTime(endDate);
        binding.timeStartTv.setText(sf.format(startDate));
        binding.timeEndTv.setText(sf.format(endDate));
        initTimePicker();
    }

    private void initTimePicker() {
        timeStart = new TimePickerBuilder(getContext(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date2, View v) {//选中事件回调
//                        String time = getTime(date2);
//                        button3.setText(time);
                if (end) {
                    endDate.setTime(date2.getTime() - date2.getTime() % 3600000l);
                    binding.timeEndTv.setText(sf.format(date2));
                } else {
                    startDate.setTime(date2.getTime() - date2.getTime() % 3600000l);
                    binding.timeStartTv.setText(sf.format(date2));
                }
            }
        })
                .setType(new boolean[]{true, true, true, true, false, false})//默认全部显示
                .setCancelText("取消")//取消按钮文字
                .setSubmitText("确定")//确认按钮文字
                .setContentTextSize(18)//滚轮文字大小
                .setTitleSize(18)//标题文字大小
//                        .setTitleText("请选择时间")//标题文字
                .setOutSideCancelable(true)//点击屏幕，点在控件外部范围时，是否取消显示
                .isCyclic(false)//是否循环滚动
                .setTextColorCenter(Color.BLACK)//设置选中项的颜色
                .setTitleColor(Color.BLACK)//标题文字颜色
                .setSubmitColor(Color.BLUE)//确定按钮文字颜色
                .setCancelColor(Color.BLUE)//取消按钮文字颜色
//                        .setTitleBgColor(0xFF666666)//标题背景颜色 Night mode
//                        .setBgColor(0xFF333333)//滚轮背景颜色 Night mode
                .setRangDate(startCalendar, endCalendar)//默认是1900-2100年
//                        .operateDataForFirstTime(selectedDate)// 如果不设置的话，默认是系统时间*/
                .setLabel("", "", "", ":00", "", "")
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .isDialog(false)//是否显示为对话框样式
                .build();

        binding.setEvent(new InputTimeEvent() {
            @Override
            public void back() {
                getActivity().finish();
            }

            @Override
            public void timeStart() {
                timeStart.setDate(startCalendar);//注：根据需求来决定是否使用该方法（一般是精确到秒的情况），
                // 此项可以在弹出选择器的时候重新设置当前时间，避免在初始化之后由于时间已经设定，导致选中时间与当前时间不匹配的问题。
                binding.timeStartBtn.setBackgroundResource(R.drawable.bg_input_time_left_solid);
                binding.timeEndBtn.setBackgroundResource(R.drawable.bg_input_time_right_stroke);
                binding.timeEndBtn.setTextColor(0xff01bff2);
                binding.timeStartBtn.setTextColor(0xffffffff);
                timeStart.show();
                end = false;
            }

            @Override
            public void timeEnd() {
                timeStart.setDate(endSelectCalendar);//注：根据需求来决定是否使用该方法（一般是精确到秒的情况），
                // 此项可以在弹出选择器的时候重新设置当前时间，避免在初始化之后由于时间已经设定，导致选中时间与当前时间不匹配的问题。
                binding.timeStartBtn.setBackgroundResource(R.drawable.bg_input_time_left_stroke);
                binding.timeEndBtn.setBackgroundResource(R.drawable.bg_input_time_right_solid);
                binding.timeEndBtn.setTextColor(0xffffffff);
                binding.timeStartBtn.setTextColor(0xff01bff2);
                timeStart.show();
                end = true;
            }

            @Override
            public void link() {
                if (event == null) {
                    return;
                }
                String nickName = binding.phoneNumberEt.getText().toString();

                boolean once = true;
                if (binding.periodRb.isChecked()) {
                    once = false;
                }

                if (TextUtils.isEmpty(nickName)) {
                    ToastUtil.toastShort(getContext(), "请输入昵称");
                    return;
                }

                if (startDate.getTime() > endDate.getTime()) {
                    ToastUtil.toastShort(getContext(), "起始时间不能大于结束时间");
                    return;
                }

                event.next(startDate.getTime() / 1000l, endDate.getTime() / 1000l, nickName, once);
            }
        });
    }

    public interface Event {
        void next(long start, long end, String nickname, boolean once);
    }

    public Event event;

    public void setEvent(Event event) {
        this.event = event;
    }
}
