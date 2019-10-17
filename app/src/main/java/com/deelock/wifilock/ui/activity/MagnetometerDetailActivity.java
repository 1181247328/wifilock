package com.deelock.wifilock.ui.activity;

import android.content.Intent;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.entity.MagDetail;
import com.deelock.wifilock.presenter.MagnetometerDetailPresenter;
import com.deelock.wifilock.view.IMagnetometerDetailView;

/**
 * Created by forgive for on 2018\1\15 0015.
 */

public class MagnetometerDetailActivity extends BaseActivity implements IMagnetometerDetailView, View.OnClickListener {


    //widget
    private ImageButton back_ib;
    private RelativeLayout name_rl;
    private TextView name_tv;
    private RelativeLayout about_rl;
    private RelativeLayout change_wifi_rl;
    private TextView wifi_tv;
    private RelativeLayout push_rl;
    private TextView open_tv;
    private TextView close_tv;
    private SwitchCompat warning_sc;
    private LinearLayout delete_ll;

    private RelativeLayout access_rl;

    private MagnetometerDetailPresenter presenter;
    private MagDetail detail;

    @Override
    protected void bindActivity() {
        setContentView(R.layout.activity_magnetometer_detail);
    }

    @Override
    protected void findView() {
        back_ib = f(R.id.back_ib);
        name_rl = f(R.id.name_rl);
        name_tv = f(R.id.name_tv);
        about_rl = f(R.id.about_rl);
        change_wifi_rl = f(R.id.change_wifi_rl);
        wifi_tv = f(R.id.wifi_tv);
        push_rl = f(R.id.push_rl);
        open_tv = f(R.id.open_tv);
        close_tv = f(R.id.close_tv);
        warning_sc = f(R.id.warning_sc);
        delete_ll = f(R.id.delete_ll);
        access_rl = f(R.id.magnetometer_access);
    }

    @Override
    protected void doBusiness() {
        detail = getIntent().getParcelableExtra("device");
        presenter = new MagnetometerDetailPresenter(this, this, detail);
        if ("C0".equals(detail.getPid().substring(0, 2))) {
            change_wifi_rl.setVisibility(View.GONE);
        }
    }

    @Override
    protected void setEvent() {
        back_ib.setOnClickListener(this);
        name_rl.setOnClickListener(this);
        about_rl.setOnClickListener(this);
        change_wifi_rl.setOnClickListener(this);
        push_rl.setOnClickListener(this);
        warning_sc.setOnClickListener(this);
        delete_ll.setOnClickListener(this);
        access_rl.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.warning_sc) {
            presenter.warning(warning_sc.isChecked());
        }
        presenter.onClick(v.getId());
    }

    @Override
    public void setName(String name) {
        name_tv.setText(name);
        Intent intent = new Intent();
        intent.putExtra("name", name);
        setResult(200, intent);
    }

    @Override
    public void setWiFi(String WiFi) {
        wifi_tv.setText(WiFi);
    }

    @Override
    public void setPush(String open, String close) {
        open_tv.setText(open);
        close_tv.setText(close);
    }

    @Override
    public void setWarning(boolean isChecked) {
        warning_sc.setChecked(isChecked);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_OK) {
            wifi_tv.setText(data.getStringExtra("wifi"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
