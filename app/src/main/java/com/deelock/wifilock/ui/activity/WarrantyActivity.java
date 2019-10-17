package com.deelock.wifilock.ui.activity;

import android.databinding.DataBindingUtil;
import android.view.View;

import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.ActivityWarrantyBinding;
import com.deelock.wifilock.entity.LockDetail;
import com.deelock.wifilock.network.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by forgive for on 2017\12\15 0015.
 */

public class WarrantyActivity extends BaseActivity {

    ActivityWarrantyBinding binding;
    LockDetail localData;
    SimpleDateFormat sf;
    Calendar calendar;
    ConcurrentHashMap<String, String> map;

    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_warranty);
    }

    @Override
    protected void doBusiness() {
        map = new ConcurrentHashMap<>();
        map.put("A001", "T600网络版");
        sf = new SimpleDateFormat("yyyy年MM月dd号");
        localData = getIntent().getParcelableExtra("lockDetail");
        if (localData != null){
            if (localData.getProductId().substring(0, 4).equals("A001")){
                binding.typeTv.setText("T600网络版");
            }
            setRestTime(localData);
        }
    }

    @Override
    protected void requestData() {

    }

    @Override
    protected void setEvent() {
        binding.setClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setRestTime(LockDetail l){
        binding.bindTv.setText(sf.format(l.getTimeRegister() * 1000l));
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(l.getTimeRegister() * 1000l);
        calendar.set(GregorianCalendar.YEAR, calendar.get(GregorianCalendar.YEAR) + 3);
        binding.restTv.setText(sf.format(calendar.getTime()));

        binding.restDayTv.setText((calendar.getTimeInMillis() - TimeUtil.getTime()*1000) / (3600*24*1000)+"天");
    }
}
